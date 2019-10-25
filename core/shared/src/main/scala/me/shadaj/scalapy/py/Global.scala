package me.shadaj.scalapy.py

import scala.language.dynamics

object global extends scala.Dynamic {
  def applyDynamic(method: String)(params: Any*): Dynamic = {
    // Trick to distinguish positional Python arguments in Scala follows
    val tupled = params.map(value => ("", value))
    applyDynamicNamed(method)(tupled: _*)
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    eval(s"$method(${params.map{ case (name, value) =>
      if(name.isEmpty) Arg(value) else Arg(name, value)  // Positional arguments have no name
    }.mkString(",")})")
  }

  def selectDynamic(value: String): Any = {
    eval(value)
  }
}
