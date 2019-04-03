package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.collection.mutable

class Object(val variableId: Int) { self =>
  final val expr = s"spy_o_$variableId"

  private var cleaned = false

  if (Object.allocatedObjects.nonEmpty) {
    Object.allocatedObjects.head += this
  }/* else if (isNative) {
    println(s"Warning: the object $this was allocated into a global space, which means it will not be garbage collected in Scala Native")
  }*/

  def value: PyValue = interpreter.load(expr)

  override def toString: String = {
    interpreter.load(s"str($expr)").getString
  }

  override def finalize(): Unit = {
    if (!cleaned) {
      interpreter.eval(s"del $expr")
      cleaned = true
    }
  }

  def as[T: ObjectReader]: T = implicitly[ObjectReader[T]].read(new ValueAndRequestObject(interpreter.load(expr)) {
    override def getObject: Object = self
  })
}

object Object {
  private var nextCounter: Int = 0
  private[py] var allocatedObjects: List[mutable.Queue[Object]] = List.empty

  def empty: Object = {
    val variableName = nextCounter
    nextCounter += 1

    new DynamicObject(variableName)
  }

  def apply(stringToEval: String): Object = {
    val variableName = nextCounter
    nextCounter += 1

    interpreter.eval(s"spy_o_$variableName = $stringToEval")

    new DynamicObject(variableName)
  }

  /**
   * Constructs a Python value by populating a generated variable, usually via Jep calls.
   * @param populateVariable a function that populates a variable given its name and the Jep instance
   */
  def apply(populateVariable: String => Unit): Object = {
    val ret = Object.empty
    populateVariable(ret.expr)
    
    ret
  }

  def populateWith(v: PyValue): Object = {
    apply { variable =>
      interpreter.set(variable, v)
    }
  }

  implicit def from[T](v: T)(implicit writer: ObjectWriter[T]): Object = {
    writer.write(v).left.map(Object.populateWith).merge
  }
}
