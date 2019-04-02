package me.shadaj.scalapy.py

object Platform {
  def newInterpreter: Interpreter = new JepInterpreter
}
