package me.shadaj.scalapy.py

import scala.language.dynamics

object global extends scala.Dynamic {
  def applyDynamic(method: String)(params: Any*): Any = {
    new Dynamic(interpreter.callGlobal(method, params.map(_.value): _*))
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    py"$method(${params.map(t => s"${t._1} = ${t._2.expr.variable}").mkString(",")})"
  }

  def selectDynamic(value: String): Any = {
    py"$value"
  }
}
