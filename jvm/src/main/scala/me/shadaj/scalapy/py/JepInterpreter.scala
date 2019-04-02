package me.shadaj.scalapy.py

import jep.Jep

import jep.NDArray

class JepInterpreter extends Interpreter {
  val underlying = new Jep

  override def eval(code: String): Unit = {
    underlying.eval(code)
  }

  override def set(variable: String, value: PyValue): Unit = {
    underlying.set(variable, value.getAny)
  }

  def valueFromAny(v: Any) = valueFromJepAny(v)
  
  def valueFromBoolean(v: Boolean) = valueFromJepAny(v)
  def valueFromLong(v: Long) = valueFromJepAny(v)
  def valueFromDouble(v: Double): PyValue = valueFromJepAny(v)
  def valueFromString(v: String): PyValue = valueFromJepAny(v)

  def noneValue: PyValue = valueFromJepAny(null)

  private def valueFromJepAny(value: Any): PyValue = {
    if (value.isInstanceOf[PyValue]) value.asInstanceOf[PyValue] else {
      new PyValue {
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
          this.getAny match {
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

        def getSeq: Seq[PyValue] = {
          getAny match {
            case arr: Array[_] =>
              arr.view.map(valueFromJepAny)
            case arrList: java.util.List[_] =>
              arrList.toArray.view.map(valueFromJepAny)
            case ndArr: NDArray[Array[_]] =>
              ndArr.getData.view.map(valueFromJepAny)
          }
        }

        def getAny: Any = value
      }
    }
  }

  override def load(code: String): PyValue = {
    valueFromJepAny(underlying.getValue(code))
  }
}
