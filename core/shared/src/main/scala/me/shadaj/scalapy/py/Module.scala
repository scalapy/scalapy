package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.reflect.ClassTag

@native trait Module extends Dynamic

object Module {
  def apply(module: String): Module = {
    val loadedModuleName = "tmp_load_module"

    interpreter.eval(s"import $module as $loadedModuleName")
    val ret = Any.populateWith(interpreter.load(loadedModuleName)).as[Module]

    interpreter.eval(s"del $loadedModuleName")

    ret
  }

  def apply(module: String, subname: String): Module = {
    val loadedModuleName = "tmp_load_module"

    interpreter.eval(s"from $module import $subname as $loadedModuleName")
    val ret = Any.populateWith(interpreter.load(loadedModuleName)).as[Module]

    interpreter.eval(s"del $loadedModuleName")

    ret
  }
}
