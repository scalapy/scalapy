package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue}

object PyNone {
  object None extends Any {
    __scalapy__rawValue = CPythonInterpreter.noneValue
  }
}
