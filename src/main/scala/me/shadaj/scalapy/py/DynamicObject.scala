package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics
object DynamicObject {}
class DynamicObject(varId: Int)(implicit jep: Jep)
    extends Object(varId)
    with scala.Dynamic {
  val uniaryOp = { s: String =>
    Object(s"+$s")
  }
  def binaryOp(opNm: String, obj2: Object) = Object(s"$expr + (${obj2.expr})")
  def applyDynamic(method: String)(params: Object*): DynamicObject =
    Object(s"$expr.$method(${params.map(_.expr).mkString(",")})")

  def applyDynamicNamed(method: String)(
      params: (String, Object)*): DynamicObject = {
    Object(
      s"$expr.$method(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})")

  }

  def selectDynamic(value: String): DynamicObject = Object(s"$expr.$value")

  def updateDynamic(method: String)(value: Any) =
    Object(s"$expr.$method = $value")

  def arrayAccess(key: Object): DynamicObject =
    Object(s"$expr[${key.expr}]")

  def unary_+(): DynamicObject = Object(s"+$expr")

  def unary_-(): DynamicObject = {
    Object(s"-$expr")
  }

  def +(that: Object): DynamicObject = binaryOp("+", that)
  def -(that: Object): DynamicObject = binaryOp("-", that)
  def *(that: Object): DynamicObject = binaryOp("*", that)
  def /(that: Object): DynamicObject = binaryOp("/", that)
  def %(that: Object): DynamicObject = binaryOp("%", that)
}
