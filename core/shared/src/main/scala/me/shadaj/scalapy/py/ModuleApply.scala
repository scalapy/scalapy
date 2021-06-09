package me.shadaj.scalapy.py

import scala.language.dynamics

import me.shadaj.scalapy.interpreter.CPythonInterpreter

trait ModuleApply {
  def apply(module: String): Module = {
    Any.populateWith(CPythonInterpreter.importModule(module)).as[Module]
  }

  def apply(module: String, subname: String): Any = {
    Any.populateWith(CPythonInterpreter.importModule(module))
      .as[Dynamic].selectDynamic(subname)
  }
}