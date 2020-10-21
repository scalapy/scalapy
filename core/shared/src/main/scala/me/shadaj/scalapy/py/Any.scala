package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.collection.mutable

import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue, VariableReference}
import me.shadaj.scalapy.readwrite.{Reader, Writer}

trait Any extends scala.Any { self =>
  private[scalapy] def value: PyValue

  override def toString: String = value.getStringified

  final def as[T: Reader]: T = implicitly[Reader[T]].read(value)
}

object Any {
  def populateWith(v: PyValue): Any = {
    new Any {
      val value = v
    }
  }

  implicit def from[T](v: T)(implicit writer: Writer[T]): Any = {
    Any.populateWith(writer.write(v))
  }
}
