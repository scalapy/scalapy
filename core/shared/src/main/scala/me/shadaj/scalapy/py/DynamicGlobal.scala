package me.shadaj.scalapy.py

import scala.language.dynamics

import me.shadaj.scalapy.interpreter.CPythonInterpreter

trait DynamicGlobal {
  object global extends scala.Dynamic {
    def applyDynamic(method: String)(params: Any*): Dynamic = {
      Any.populateWith(CPythonInterpreter.callGlobal(method, params.map(_.__scalapy_value), Seq())).as[Dynamic]
    }

    def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
      Any.populateWith(CPythonInterpreter.callGlobal(
        method,
        params.filter(_._1.isEmpty).map(_._2.__scalapy_value),
        params.filter(_._1.nonEmpty).map(t => (t._1, t._2.__scalapy_value))
      )).as[Dynamic]
    }

    def selectDynamic(value: String): Dynamic = {
      Any.populateWith(CPythonInterpreter.selectGlobal(value)).as[Dynamic]
    }
  }
}
