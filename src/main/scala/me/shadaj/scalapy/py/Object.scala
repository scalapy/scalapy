package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics
import scala.reflect.ClassTag

class ObjectKeeper(varName: String)(implicit jep: Jep) {
  override def finalize(): Unit = {
    jep.eval(s"del $varName")
  }
}

class Object private[py](val varId: Int)(implicit jep: Jep) extends Ref(s"spy_o_$varId") {
  private[py] var keeper: ObjectKeeper = null

  def value: Any = jep.getValue(s"$expr")

  def asRef: Ref = Ref(expr)

  override def toObject(implicit jep: Jep): Object = this

  override def toString: String = {
    jep.getValue(s"str($expr)").asInstanceOf[String]
  }
}

object Object {
  private var nextCounter: Int = 0

  def empty(implicit jep: Jep): Object = {
    val variableName = nextCounter
    nextCounter += 1

    val ret = new DynamicObject(variableName)
    ret.keeper = new ObjectKeeper(ret.expr)
    ret
  }

  def apply(stringToEval: String)(implicit jep: Jep): Object = {
    val variableName = nextCounter
    nextCounter += 1

    jep.eval(s"spy_o_$variableName = $stringToEval")

    val ret = new DynamicObject(variableName)
    ret.keeper = new ObjectKeeper(ret.expr)
    ret
  }

  implicit def from[T](v: T)(implicit writer: ObjectWriter[T], jep: Jep): Object = {
    val converted = writer.write(v).toObject

    val ret = new DynamicObject(converted.varId)
    ret.keeper = converted.keeper
    ret
  }
}
