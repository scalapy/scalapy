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

  private var cleaned = false

  def cleanup(): Unit = {
    if (!cleaned) {
      cleaned = true
      interpreter.eval(s"del $variable")
    }
  }

  override def finalize(): Unit = {
    cleanup()
  }
}

object VariableReference {
  import scala.collection.mutable
  private[py] var allocatedReferences: List[List[VariableReference]] = List.empty
}

class PythonException(s: String) extends Exception(s) 

trait PyValue {
  if (PyValue.allocatedValues.nonEmpty) {
    PyValue.allocatedValues = (this :: PyValue.allocatedValues.head) :: PyValue.allocatedValues.tail
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
