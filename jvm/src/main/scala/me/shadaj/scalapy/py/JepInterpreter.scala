package me.shadaj.scalapy.py

import jep._
import jep.python.PyObject

import scala.collection.JavaConverters._

class JepInterpreter extends Interpreter {
  val underlying = new Jep

  override def eval(code: String): Unit = {
    underlying.eval(code)
  }

  override def set(variable: String, value: PyValue): Unit = {
    underlying.set(variable, value.asInstanceOf[JepJavaPyValue].value)
  }

  private var counter = 0
  def getVariableReference(value: PyValue): VariableReference = {
    val variableName = "spy_o_" + counter
    counter += 1
    try {
      underlying.set(variableName, value.asInstanceOf[JepPyValue].pyObject)
    } catch {
      case _ => underlying.set(variableName, value.asInstanceOf[JepPyValue].value)
    }
    
    new VariableReference(variableName)
  }

  def valueFromAny(v: Any) = valueFromJepAny(v)
  
  def valueFromBoolean(v: Boolean) = valueFromJepAny(v)
  def valueFromLong(v: Long) = valueFromJepAny(v)
  def valueFromDouble(v: Double): PyValue = valueFromJepAny(v)
  def valueFromString(v: String): PyValue = valueFromJepAny(v)

  def createList(seq: Seq[PyValue]): PyValue = {
    valueFromJepAny(seq.view.map {
      case v: JepJavaPyValue => v.value
      case v: JepPythonPyValue => v.pyObject
    }.toArray)
  }

  def createTuple(seq: Seq[PyValue]): PyValue = {
    callGlobal("tuple", valueFromJepAny(seq.view.map {
      case v: JepJavaPyValue => v.value
      case v: JepPythonPyValue => v.pyObject
    }.asJava))
  }

  def noneValue: PyValue = valueFromJepAny(null)

  val stringifiedNone = underlying.getValue("str(None)", classOf[String])

  def valueFromJepAny(value: Any): PyValue = value match {
    case v: PyValue => v
    case o => new JepJavaPyValue(o)
  }

  override def load(code: String): PyValue = {
    try {
      val pyObject = underlying.getValue(code, classOf[PyObject])
      pyObject.incref()
      new JepPythonPyValue(pyObject)
    } catch {
      case _: JepException | _: NullPointerException =>
        new JepJavaPyValue(underlying.getValue(code))
    }
  }

  def binaryAdd(a: PyValue, b: PyValue): PyValue = call(a, "__add__", Seq(b))
  def binarySub(a: PyValue, b: PyValue): PyValue = call(a, "__sub__", Seq(b))
  def binaryMul(a: PyValue, b: PyValue): PyValue = call(a, "__mul__", Seq(b))
  def binaryDiv(a: PyValue, b: PyValue): PyValue = call(a, "__div__", Seq(b))
  def binaryMod(a: PyValue, b: PyValue): PyValue = call(a, "__mod__", Seq(b))

  def callGlobal(method: String, args: PyValue*): PyValue = {
    val res = "tmp_call_global_res"
    val argsLoaded = args.zipWithIndex.map { case (arg: JepPyValue, i) =>
      val argName = s"tmp_call_global_arg_$i"
      arg.loadInto(argName)
      argName
    }

    underlying.eval(s"$res = $method(${argsLoaded.mkString(",")})")
    val ret = load(res)
    argsLoaded.foreach(v => underlying.eval(s"del $v"))
    underlying.eval(s"del $res")
    ret
  }

  def call(on: PyValue, method: String, args: Seq[PyValue]): PyValue = {
    val res = "tmp_call_res"
    val onValue = "tmp_call_on"

    on.asInstanceOf[JepPyValue].loadInto(onValue)
    val argsLoaded = args.zipWithIndex.map { case (arg: JepPyValue, i) =>
      val argName = s"tmp_call_arg_$i"
      arg.loadInto(argName)
      argName
    }

    underlying.eval(s"$res = $onValue.$method(${argsLoaded.mkString(",")})")
    val ret = load(res)
    underlying.eval(s"del $onValue")
    argsLoaded.foreach(v => underlying.eval(s"del $v"))
    underlying.eval(s"del $res")
    ret
  }

  def select(on: PyValue, value: String): PyValue = {
    val res = "tmp_select_res"
    val onValue = "tmp_select_on"

    on.asInstanceOf[JepPyValue].loadInto(onValue)

    underlying.eval(s"$res = $onValue.$value")
    val ret = load(res)
    underlying.eval(s"del $onValue")
    underlying.eval(s"del $res")
    ret
  }

  def selectList(on: PyValue, index: Int): PyValue = {
    val res = "tmp_select_res"
    val onValue = "tmp_select_on"

    on.asInstanceOf[JepPyValue].loadInto(onValue)

    underlying.eval(s"$res = $onValue[$index]")
    val ret = load(res)
    underlying.eval(s"del $onValue")
    underlying.eval(s"del $res")
    ret
  }

  def selectDictionary(on: PyValue, key: PyValue): PyValue = {
    val res = "tmp_select_res"
    val onValue = "tmp_select_on"
    val keyValue = "tmp_select_key"

    on.asInstanceOf[JepPyValue].loadInto(onValue)
    key.asInstanceOf[JepPyValue].loadInto(keyValue)

    underlying.eval(s"$res = $onValue[$keyValue]")
    val ret = load(res)
    underlying.eval(s"del $onValue")
    underlying.eval(s"del $keyValue")
    underlying.eval(s"del $res")
    ret
  }

  def binaryOp(op: String, a: PyValue, b: PyValue): PyValue = {
    val res = "tmp_binary_res"
    val aValue = "tmp_binary_a"
    val bValue = "tmp_binary_b"

    a.asInstanceOf[JepPyValue].loadInto(aValue)
    b.asInstanceOf[JepPyValue].loadInto(bValue)

    underlying.eval(s"$res = $aValue $op $bValue")
    val ret = load(res)
    underlying.eval(s"del $aValue")
    underlying.eval(s"del $bValue")
    underlying.eval(s"del $res")
    ret
  }
}

trait JepPyValue extends PyValue {
  def value: Any
  def pyObject: PyObject
  
  override def equals(o: Any) = {
    o != null && o.isInstanceOf[JepJavaPyValue] &&
      value == o.asInstanceOf[JepJavaPyValue].value
  }
  
  override def hashCode() = value.hashCode()
  
  def getString: String = value.asInstanceOf[String]
  
  def getDouble: Double = value match {
    case v: Byte => v
    case v: Int => v
    case v: Float => v
    case v: Long => v
    case v: Double => v
  }
  
  def getLong: Long = value match {
    case v: Byte => v
    case v: Int => v
    case v: Long => v
  }
  
  def getBoolean: Boolean = {
    value match {
      case b: Boolean =>
        b
      case s: String =>
        s == "True"
      case i: Int if i == 0 || i == 1 =>
        i == 1
      case o =>
        throw new IllegalArgumentException(s"Unknown boolean type for value $o")
    }
  }

  def getTuple = getSeq

  def getSeq: Seq[PyValue] = {
    value match {
      case arr: Array[_] =>
        arr.view.map(interpreter.valueFromJepAny)
      case arrList: java.util.List[_] =>
        arrList.toArray.view.map(interpreter.valueFromJepAny)
      case ndArr: NDArray[Array[_]] =>
        ndArr.getData.view.map(interpreter.valueFromJepAny)
    }
  }

  import scala.collection.mutable
  import scala.collection.JavaConverters._
  def getMap: mutable.Map[PyValue, PyValue] = {
    value.asInstanceOf[java.util.Map[Any, Any]].asScala.map { kv => 
      (interpreter.valueFromJepAny(kv._1), interpreter.valueFromJepAny(kv._2))
    }
  }

  def loadInto(variable: String): Unit
}

// Jep value stored as a PyObject reference
class JepPythonPyValue(val pyObject: PyObject) extends JepPyValue {
  lazy val value = {
    val temp = "tmp_obj_to_v"
    loadInto(temp)
    val ret = interpreter.underlying.getValue(temp)
    interpreter.eval("del tmp_obj_to_v")
    ret
  }

  def getStringified = if (pyObject == null) interpreter.stringifiedNone else pyObject.toString()

  def loadInto(variable: String): Unit = {
    interpreter.underlying.set(variable, pyObject)
  }

  def cleanup() = {
    pyObject.decref()
  }
}

// Jep value stored as a Java value
class JepJavaPyValue(val value: Any) extends JepPyValue {
  private var _pyObject: PyObject = null

  def pyObject = {
    if (_pyObject == null) {
      val temp = "tmp_v_to_obj"
      loadInto(temp)
      _pyObject = interpreter.underlying.getValue(temp, classOf[PyObject])
      _pyObject.incref()
      interpreter.eval("del tmp_v_to_obj")
    }

    _pyObject
  }

  def getStringified = if (value == null) interpreter.stringifiedNone else {
    val temp = "tmp_v_to_str"
    loadInto(temp)
    interpreter.eval("tmp_v_to_str = str(tmp_v_to_str)")
    val ret: String = interpreter.underlying.getValue(temp, classOf[String])
    interpreter.eval("del tmp_v_to_str")
    ret
  }

  def loadInto(variable: String): Unit = {
    interpreter.underlying.set(variable, value)
  }

  def cleanup() = {
    if (_pyObject != null) {
      _pyObject.decref()
    }
  }
}
