package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.reflect.ClassTag

import me.shadaj.scalapy.interpreter.CPythonInterpreter

@native trait Module extends Dynamic

object Module {
  def apply(module: String): Module = {
    val loadedModuleName = "tmp_load_module"

    CPythonInterpreter.eval(s"import $module as $loadedModuleName")
    val ret = Any.populateWith(CPythonInterpreter.load(loadedModuleName)).as[Module]

    CPythonInterpreter.eval(s"del $loadedModuleName")

    ret
  }

  def apply(module: String, subname: String): Module = {
    val loadedModuleName = "tmp_load_module"

    CPythonInterpreter.eval(s"from $module import $subname as $loadedModuleName")
    val ret = Any.populateWith(CPythonInterpreter.load(loadedModuleName)).as[Module]

    CPythonInterpreter.eval(s"del $loadedModuleName")

    ret
  }
}
