package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.collection.mutable
import scala.language.implicitConversions

import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue}
import me.shadaj.scalapy.readwrite.{Reader, Writer}

trait Any extends AnyRawValue { self =>
  private var cleaned = false
  
  private[scalapy] def value: PyValue = {
    if (cleaned) {
      throw new IllegalAccessException("The Python value you are try to access has already been released by a call to py.Any.del()")
    } else {
      rawValue
    }
  }

  override def toString: String = value.getStringified

  final def as[T: Reader]: T = implicitly[Reader[T]].read(value)

  final def del(): Unit = {
    value.cleanup()
    cleaned = true
  }

  override def equals(obj: scala.Any): Boolean = {
    obj.isInstanceOf[Any] && value == obj.asInstanceOf[Any].value
  }

  override def hashCode(): Int = value.hashCode()
}

object Any extends AnyPopulateWith {
  implicit def from[T](v: T)(implicit writer: Writer[T]): Any = {
    Any.populateWith(writer.write(v))
  }
}
