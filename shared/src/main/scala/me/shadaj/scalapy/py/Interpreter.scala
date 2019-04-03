package me.shadaj.scalapy.py

class VariableReference(val variable: String) {
  override def toString(): String = variable
  // todo cleanup

  override def finalize(): Unit = {
    interpreter.eval(s"del $variable")
  }
}

trait Interpreter {
  def eval(string: String): Unit
  def set(string: String, value: PyValue): Unit

  def load(variable: String): PyValue

  def getVariableReference(value: PyValue): VariableReference

  def valueFromBoolean(v: Boolean): PyValue
  def valueFromLong(v: Long): PyValue
  def valueFromDouble(v: Double): PyValue
  def valueFromString(v: String): PyValue
  // def valueFromSeq(elements: Seq[PyValue]): PyValue

  def noneValue: PyValue
}

trait PyValue {
  def getString: String
  def getLong: Long
  def getDouble: Double
  def getBoolean: Boolean
  def getTuple: Seq[PyValue]
  def getSeq: Seq[PyValue]

  def getStringified: String

  import scala.collection.mutable
  def getMap: mutable.Map[PyValue, PyValue]
}
