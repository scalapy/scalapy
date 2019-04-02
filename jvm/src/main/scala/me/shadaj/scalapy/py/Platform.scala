package me.shadaj.scalapy.py

object Platform {
  def newInterpreter: Interpreter = new JepInterpreter
  final val isNative = false
}
