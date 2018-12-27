package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics

class Global private[py](implicit jep: Jep) extends scala.Dynamic {
  def applyDynamic(method: String)(params: Object*): DynamicObject = {
    Object(s"$method(${params.map(_.expr).mkString(",")})")
  }

  def applyDynamicNamed(method: String)(params: (String, Object)*): DynamicObject = {
    Object(s"$method(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})").asInstanceOf[DynamicObject]
  }

  def selectDynamic(value: String): DynamicObject = {
    Object(value)
  }
}