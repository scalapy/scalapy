package me.shadaj.scalapy.py

import scala.language.dynamics

object global extends scala.Dynamic {
  def applyDynamic(method: String)(params: Any*): Any = {
    Any.populateWith(interpreter.callGlobal(method, params.map(_.value))).as[Dynamic]
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    eval(s"$method(${params.map(t => s"${t._1} = ${t._2.expr.variable}").mkString(",")})")
  }

  def selectDynamic(value: String): Any = {
    eval(value)
  }
}
