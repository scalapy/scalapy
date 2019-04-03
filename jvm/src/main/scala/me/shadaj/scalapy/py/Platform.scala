package me.shadaj.scalapy.py

object Platform {
  type InterpreterImplementation = JepInterpreter
  def newInterpreter: InterpreterImplementation = new JepInterpreter
  final val isNative = false
}
