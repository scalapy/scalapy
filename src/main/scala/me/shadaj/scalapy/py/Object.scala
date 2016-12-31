package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics

class Object private[py](val varId: Int)(implicit jep: Jep) extends Ref(s"spy_o_$varId") {
  override def applyDynamic(method: String)(params: Ref*): Object = {
    Object(s"$expr.$method(${params.map(_.expr).mkString(",")})")
  }

  override def selectDynamic(value: String): Object = {
    Object(s"$expr.$value")
  }

  override def arrayAccess(key: Ref): Object = {
    Object(s"$expr[${key.expr}]")
  }

  override def +(that: Ref): Object = {
    Object(s"$expr + (${that.expr})")
  }

  override def -(that: Ref): Object = {
    Object(s"$expr - (${that.expr})")
  }

  override def *(that: Ref): Object = {
    Object(s"$expr * (${that.expr})")
  }

  override def /(that: Ref): Object = {
    Object(s"$expr / (${that.expr})")
  }

  override def %(that: Ref): Object = {
    Object(s"$expr % (${that.expr})")
  }

  def value: Any = jep.getValue(s"$expr")

  def asRef: Ref = Ref(expr)

  override def toString: String = {
    jep.getValue(s"$expr").toString
  }

  override def finalize(): Unit = {
    jep.eval(s"del $expr")
  }
}

object Object {
  private var nextCounter: Int = 0

  def empty(implicit jep: Jep): Object = {
    val variableName = nextCounter
    nextCounter += 1

    new Object(variableName)
  }

  def apply(stringToEval: String)(implicit jep: Jep): Object = {
    val variableName = nextCounter
    nextCounter += 1

    jep.eval(s"spy_o_$variableName = $stringToEval")
    new Object(variableName)
  }

  implicit def from[T](v: T)(implicit writer: ObjectWriter[T], jep: Jep): Object = {
    writer.write(v).toObject
  }
}
