package me.shadaj.scalapy

import me.shadaj.scalapy.interpreter.CPythonInterpreter
import me.shadaj.scalapy.py.Any

object PyNone {
  @py.native class None extends Any
}
