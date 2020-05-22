package me.shadaj.scalapy.py

import scala.language.dynamics

object global extends scala.Dynamic {
  def applyDynamic(method: String)(params: Any*): Dynamic = {
    Any.populateWith(CPythonInterpreter.callGlobal(method, params.map(_.value))).as[Dynamic]
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    // a copy of Dynamic.applyDynamicNamed()
    val otherParams: List[(String, Any)] = params.toList
      .filterNot(_._1 == "kwargs")
      .map{ case (name, value) => name -> value}
    val mapParams: List[(String, Any)] = params.toList
      .filter(_._1 == "kwargs")
      .flatMap { case (_ , value) => value.as[Map[String, Any]].toList }
    val allParams: List[(String, Any)] = otherParams ++ mapParams

    eval(s"$method(${allParams.map{ case (name, value) =>
      if (name.isEmpty) Arg(value) else Arg(name, value)  // Positional arguments have no name
    }.mkString(",")})")
  }

  def selectDynamic(value: String): Any = {
    Any.populateWith(CPythonInterpreter.selectGlobal(value))
  }
}
