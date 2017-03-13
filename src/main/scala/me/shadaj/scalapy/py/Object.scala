package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics

import scala.collection.mutable

class Object private[py](val varId: Int)(implicit jep: Jep) extends Ref(s"spy_o_$varId") {
  private var cleaned = false

  if (Object.allocatedObjects.nonEmpty) {
    Object.allocatedObjects.head += this
  }

  def value: Any = jep.getValue(s"$expr")

  def asRef: Ref = Ref(expr)

  override def toObject(implicit jep: Jep): Object = this

  override def toString: String = {
    jep.getValue(s"str($expr)").asInstanceOf[String]
  }

  override def finalize(): Unit = {
    if (!cleaned) {
      jep.eval(s"del $expr")
      cleaned = true
    }
  }
}

object Object {
  private var nextCounter: Int = 0
  private[py] var allocatedObjects: List[mutable.Queue[Object]] = List.empty

  def empty(implicit jep: Jep): Object = {
    val variableName = nextCounter
    nextCounter += 1

    val ret = new DynamicObject(variableName)

    ret
  }

  def apply(stringToEval: String)(implicit jep: Jep): Object = {
    val variableName = nextCounter
    nextCounter += 1

    jep.eval(s"spy_o_$variableName = $stringToEval")

    val ret = new DynamicObject(variableName)
    ret
  }

  implicit def from[T](v: T)(implicit writer: ObjectWriter[T], jep: Jep): Object = {
    writer.write(v).toObject
  }
}
