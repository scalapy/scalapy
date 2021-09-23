package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter.PyValue

trait AnyRawValue {
  private[scalapy] var rawValue: PyValue = null
}

trait AnyPopulateWith {
  def populateWith(v: PyValue): Any = {
    new Any {
      rawValue = v
    }
  }
}
