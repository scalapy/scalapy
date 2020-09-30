package me.shadaj.scalapy.py

import scala.language.dynamics

import me.shadaj.scalapy.interpreter.CPythonInterpreter

object global extends scala.Dynamic {
  def applyDynamic(method: String)(params: Any*): Dynamic = {
    Any.populateWith(CPythonInterpreter.callGlobal(method, params.map(_.value))).as[Dynamic]
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    eval(s"$method(${params.map{ case (name, value) =>
      if (name.isEmpty) Arg(value) else Arg(name, value)  // Positional arguments have no name
    }.mkString(",")})")
  }

  def selectDynamic(value: String): Any = {
    Any.populateWith(CPythonInterpreter.selectGlobal(value))
  }
}
