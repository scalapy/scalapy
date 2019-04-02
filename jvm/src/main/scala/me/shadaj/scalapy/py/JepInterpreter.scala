package me.shadaj.scalapy.py

import jep.Jep

import jep.NDArray

class JepInterpreter extends Interpreter {
  val underlying = new Jep

  override def eval(code: String): Unit = {
    underlying.eval(code)
  }

  override def set(variable: String, value: PyValue): Unit = {
    underlying.set(variable, value.asInstanceOf[JepJavaPyValue].value)
  }

  def valueFromAny(v: Any) = valueFromJepAny(v)
  
  def valueFromBoolean(v: Boolean) = valueFromJepAny(v)
  def valueFromLong(v: Long) = valueFromJepAny(v)
  def valueFromDouble(v: Double): PyValue = valueFromJepAny(v)
  def valueFromString(v: String): PyValue = valueFromJepAny(v)

  def noneValue: PyValue = valueFromJepAny(null)

  def valueFromJepAny(value: Any): PyValue = value match {
    case v: PyValue => v
    case o => new JepJavaPyValue(o)
  }

  override def load(code: String): PyValue = {
    valueFromJepAny(underlying.getValue(code))
  }
}

class JepJavaPyValue(val value: Any) extends PyValue {
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
