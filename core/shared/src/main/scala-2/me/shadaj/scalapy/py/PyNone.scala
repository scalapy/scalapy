package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue}

object PyNone {
  object None extends Any {
    override val __scalapy__rawValue: PyValue = CPythonInterpreter.noneValue
  }
}
