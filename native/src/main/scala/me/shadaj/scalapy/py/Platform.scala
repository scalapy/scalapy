package me.shadaj.scalapy.py

object Platform {
  def newInterpreter: Interpreter = new CPythonInterpreter
  final val isNative = true
}
