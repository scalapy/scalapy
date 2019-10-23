package me.shadaj.scalapy.py

/**
  * Represents an argument to a Python function or method.
  * @param name: Name of the argument, or `None` if it has been passed positionally.
  * @param value: Variable reference
  */
class Arg(name: Option[String], value: Any){
  override def toString: String =
    name.fold(value.expr.toString())(n => s"$n = ${value.expr}")
}

object Arg {
  def apply(name: String, value: Any): Arg = new Arg(Some(name), value)

  def apply(value: Any): Arg = new Arg(Option.empty, value)
}
