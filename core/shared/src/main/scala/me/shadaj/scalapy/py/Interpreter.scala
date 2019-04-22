package me.shadaj.scalapy.py

class VariableReference(val variable: String) {
  // For tracing down variable reference creation
  // try {
  //   throw new Exception()
  // } catch {
  //   case e => e.printStackTrace()
  // }

  if (VariableReference.allocatedReferences.nonEmpty) {
    VariableReference.allocatedReferences = (this :: VariableReference.allocatedReferences.head) :: VariableReference.allocatedReferences.tail
  } else if (Platform.isNative) {
    println(s"Warning: the reference $variable was allocated into a global space, which means it will not be garbage collected in Scala Native")
  }

  override def toString(): String = variable

  def cleanup(): Unit = {
    interpreter.eval(s"del $variable")
  }

  override def finalize(): Unit = {
    cleanup()
  }
}

object VariableReference {
  import scala.collection.mutable
  private[py] var allocatedReferences: List[List[VariableReference]] = List.empty
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
  def createList(elements: Seq[PyValue]): PyValue
  def createTuple(elements: Seq[PyValue]): PyValue

  def noneValue: PyValue

  def unaryNeg(a: PyValue): PyValue
  def unaryPos(a: PyValue): PyValue

  def binaryAdd(a: PyValue, b: PyValue): PyValue
  def binarySub(a: PyValue, b: PyValue): PyValue
  def binaryMul(a: PyValue, b: PyValue): PyValue
  def binaryDiv(a: PyValue, b: PyValue): PyValue
  def binaryMod(a: PyValue, b: PyValue): PyValue

  def callGlobal(name: String, args: PyValue*): PyValue
  def call(on: PyValue, method: String, args: Seq[PyValue]): PyValue
  def select(on: PyValue, value: String): PyValue
  def update(on: PyValue, value: String, newValue: PyValue): Unit
  def selectList(on: PyValue, index: Int): PyValue
  def selectDictionary(on: PyValue, key: PyValue): PyValue
}

trait PyValue {
  if (PyValue.allocatedValues.nonEmpty) {
    PyValue.allocatedValues = (this :: PyValue.allocatedValues.head) :: PyValue.allocatedValues.tail
  } else if (Platform.isNative) {
    println(s"Warning: the value ${this.getStringified} was allocated into a global space, which means it will not be garbage collected in Scala Native")
  }

  def getString: String
  def getLong: Long
  def getDouble: Double
  def getBoolean: Boolean
  def getTuple: Seq[PyValue]
  def getSeq: Seq[PyValue]

  def getStringified: String

  import scala.collection.mutable
  def getMap: mutable.Map[PyValue, PyValue]

  def cleanup(): Unit

  override def finalize(): Unit = cleanup()
}

object PyValue {
  import scala.collection.mutable
  private[py] var allocatedValues: List[List[PyValue]] = List.empty
}
