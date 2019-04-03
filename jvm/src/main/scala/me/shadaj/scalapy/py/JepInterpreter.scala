package me.shadaj.scalapy.py

import jep.Jep
import jep.python.PyObject

import jep.NDArray

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

  def valueFromSeq(seq: Seq[PyValue]): PyValue = {
    valueFromJepAny(seq.view.map {
      case v: JepJavaPyValue => v.value
      case v: JepPythonPyValue => v.pyObject
    }.toArray)
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
      case _ => new JepJavaPyValue(underlying.getValue(code))
    }
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
}

class JepJavaPyValue(val value: Any) extends JepPyValue {
  private var _pyObject: PyObject = null

  def pyObject = {
    if (_pyObject == null) {
      val temp = "tmp_v_to_obj"
      interpreter.underlying.set(temp, value)
      _pyObject = interpreter.underlying.getValue(temp, classOf[PyObject])
      _pyObject.incref()
    }

    _pyObject
  }

  def getStringified = if (value == null) interpreter.stringifiedNone else value.toString()

  def cleanup() = {
    if (_pyObject != null) {
      _pyObject.decref()
    }
  }
}

class JepPythonPyValue(val pyObject: PyObject) extends JepPyValue {
  lazy val value = {
    val temp = "tmp_obj_to_v"
    interpreter.underlying.set(temp, pyObject)
    interpreter.underlying.getValue(temp)
  }

  def getStringified = if (pyObject == null) interpreter.stringifiedNone else pyObject.toString()

  def cleanup() = {
    pyObject.decref()
  }
}
