package me.shadaj.scalapy.py

trait Interpreter {
  def eval(string: String): Unit
  def set(string: String, value: Any): Unit
  
  def loadAsString(variable: String): String
  
  def loadAsAny(variable: String): Any
}
