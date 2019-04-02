package me.shadaj.scalapy.py

import jep.Jep

class JepInterpreter extends Interpreter {
  val underlying = new Jep

  override def eval(code: String): Unit = {
    underlying.eval(code)
  }

  override def set(variable: String, value: Any): Unit = {
    underlying.set(variable, value)
  }

  override def loadAsString(code: String): String = {
    underlying.getValue(code, classOf[String])
  }

  override def loadAsAny(code: String): Any = {
    underlying.getValue(code)
  }
}
