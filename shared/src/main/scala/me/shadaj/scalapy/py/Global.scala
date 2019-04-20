package me.shadaj.scalapy.py

import scala.language.dynamics

class Global private[py]() extends scala.Dynamic {
  def applyDynamic(method: String)(params: Object*): Object = {
    new Dynamic(interpreter.callGlobal(method, params.map(_.value): _*))
  }

  def applyDynamicNamed(method: String)(params: (String, Object)*): Dynamic = {
    Object(s"$method(${params.map(t => s"${t._1} = ${t._2.expr.variable}").mkString(",")})").asDynamic
  }

  def selectDynamic(value: String): Object = {
    Object(value)
  }
}
