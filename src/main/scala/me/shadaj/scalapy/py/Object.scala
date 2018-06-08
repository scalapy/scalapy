package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics
import scala.collection.mutable
import scala.reflect.ClassTag

class Object private[py](val varId: Int)(implicit jep: Jep) {
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

  def to[T: ObjectReader]: T = implicitly[ObjectReader[T]].read(jep.getValue(expr))(jep)

  def as[T <: ObjectFacade: ClassTag](implicit classTag: ClassTag[T]): T = {
    classTag.runtimeClass.getConstructor(classOf[Object], classOf[Jep]).newInstance(this, jep).asInstanceOf[T]
  }
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

  implicit def from[T](v: T)(implicit writer: ObjectWriter[T], jep: Jep): Object = {
    Object { (variable, jep) =>
      jep.set(variable, writer.write(v)(jep))
    }
  }
}
