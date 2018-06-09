package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics
import scala.collection.mutable

class Object private[py](val varId: Int)(implicit jep: Jep) { self =>
  final val expr = s"spy_o_$varId"

  private var cleaned = false

  if (Object.allocatedObjects.nonEmpty) {
    Object.allocatedObjects.head += this
  }

  def value: Any = jep.getValue(expr)

  override def toString: String = {
    jep.getValue(s"str($expr)").asInstanceOf[String]
  }

  override def finalize(): Unit = {
    if (!cleaned) {
      jep.eval(s"del $expr")
      cleaned = true
    }
  }

  def as[T: ObjectReader]: T = implicitly[ObjectReader[T]].read(new ValueAndRequestObject(jep.getValue(expr)) {
    override def getObject: Object = self
  })(jep)
}

object Object {
  private var nextCounter: Int = 0
  private[py] var allocatedObjects: List[mutable.Queue[Object]] = List.empty

  def empty(implicit jep: Jep): Object = {
    val variableName = nextCounter
    nextCounter += 1

    new DynamicObject(variableName)
  }

  def apply(stringToEval: String)(implicit jep: Jep): Object = {
    val variableName = nextCounter
    nextCounter += 1

    jep.eval(s"spy_o_$variableName = $stringToEval")

    new DynamicObject(variableName)
  }

  /**
   * Constructs a Python value by populating a generated variable, usually via Jep calls.
   * @param populateVariable a function that populates a variable given its name and the Jep instance
   */
  def apply(populateVariable: (String, Jep) => Unit)(implicit jep: Jep): Object = {
    val ret = Object.empty
    populateVariable(ret.expr, jep)
    
    ret
  }

  def populateWith(v: Any)(implicit jep: Jep): Object = {
    apply { (variable, j) =>
      j.set(variable, v)
    }
  }

  implicit def from[T](v: T)(implicit writer: ObjectWriter[T], jep: Jep): Object = {
    writer.write(v)(jep).left.map(Object.populateWith).merge
  }
}
