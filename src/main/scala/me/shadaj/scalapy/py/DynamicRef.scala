package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics

class DynamicRef private[py](expr: String) extends Ref(expr) with scala.Dynamic {
  def applyDynamic(method: String)(params: Ref*): DynamicRef = {
    if (method == "apply") {
      // TODO: do we need this case?
      Ref(s"($expr)(${params.map(_.expr).mkString(",")})").asInstanceOf[DynamicRef]
    } else {
      Ref(s"($expr).$method(${params.map(_.expr).mkString(",")})").asInstanceOf[DynamicRef]
    }
  }

  def applyDynamicNamed(method: String)(params: (String, Ref)*): DynamicRef = {
    Ref(s"$expr.$method(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})").asInstanceOf[DynamicRef]
  }

  def selectDynamic(value: String): DynamicRef = {
    Ref(s"($expr).$value").asInstanceOf[DynamicRef]
  }

  def arrayAccess(key: Ref): DynamicRef = {
    Ref(s"($expr)[${key.expr}]").asInstanceOf[DynamicRef]
  }

  def unary_+(): DynamicRef = {
    Ref(s"+($expr)").asInstanceOf[DynamicRef]
  }

  def unary_-(): DynamicRef = {
    Ref(s"-($expr)").asInstanceOf[DynamicRef]
  }

  def +(that: Ref): DynamicRef = {
    Ref(s"($expr) + (${that.expr})").asInstanceOf[DynamicRef]
  }

  def -(that: Ref): DynamicRef = {
    Ref(s"($expr) - (${that.expr})").asInstanceOf[DynamicRef]
  }

  def *(that: Ref): DynamicRef = {
    Ref(s"($expr) * (${that.expr})").asInstanceOf[DynamicRef]
  }

  def /(that: Ref): DynamicRef = {
    Ref(s"($expr) / (${that.expr})").asInstanceOf[DynamicRef]
  }

  def %(that: Ref): DynamicRef = {
    Ref(s"($expr) % (${that.expr})").asInstanceOf[DynamicRef]
  }
}
