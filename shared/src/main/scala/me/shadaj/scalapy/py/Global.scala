package me.shadaj.scalapy.py

import scala.language.dynamics

class Global private[py]() extends scala.Dynamic {
  def applyDynamic(method: String)(params: Object*): Object = {
    new DynamicObject(interpreter.callGlobal(method, params.map(_.value): _*))
  }

  def applyDynamicNamed(method: String)(params: (String, Object)*): DynamicObject = {
    Object(s"$method(${params.map(t => s"${t._1} = ${t._2.expr.variable}").mkString(",")})").asInstanceOf[DynamicObject]
  }

  def selectDynamic(value: String): Object = {
    Object(value)
  }
}
