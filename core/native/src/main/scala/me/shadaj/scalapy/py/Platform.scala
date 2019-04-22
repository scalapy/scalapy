package me.shadaj.scalapy.py

object Platform {
  type InterpreterImplementation = CPythonInterpreter
  def newInterpreter: InterpreterImplementation = new CPythonInterpreter
  final val isNative = true
}
