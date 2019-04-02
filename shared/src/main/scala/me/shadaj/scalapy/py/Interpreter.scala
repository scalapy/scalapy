package me.shadaj.scalapy.py

trait Interpreter {
  def eval(string: String): Unit
  def set(string: String, value: PyValue): Unit

  def load(variable: String): PyValue

  def valueFromBoolean(v: Boolean): PyValue
  def valueFromLong(v: Long): PyValue
  def valueFromDouble(v: Double): PyValue
  def valueFromString(v: String): PyValue

  def noneValue: PyValue
}

trait PyValue {
  def getString: String
  def getLong: Long
  def getDouble: Double
  def getBoolean: Boolean
  def getSeq: Seq[PyValue]

  def getAny: Any
}
