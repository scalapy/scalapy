package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter.PyValue

trait AnyRawValue {
  private[scalapy] val rawValue: PyValue
}

trait AnyPopulateWith {
  def populateWith(v: PyValue): Any = {
    new Any {
      val rawValue = v
    }
  }
}
