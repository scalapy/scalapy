package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.collection.mutable
import scala.language.implicitConversions

import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue}
import me.shadaj.scalapy.readwrite.{Reader, Writer}

trait Any extends AnyRawValue { self =>
  private var cleaned = false
  
  private[scalapy] def __scalapy_value: PyValue = {
    if (cleaned) {
      throw new IllegalAccessException("The Python value you are try to access has already been released by a call to py.Any.del()")
    } else {
      __scalapy__rawValue
    }
  }

  override def toString: String = __scalapy_value.getStringified

  final def as[T: Reader]: T = implicitly[Reader[T]].read(__scalapy_value)

  final def del(): Unit = {
    __scalapy_value.cleanup()
    cleaned = true
  }

  override def equals(obj: scala.Any): Boolean = {
    obj.isInstanceOf[Any] && __scalapy_value == obj.asInstanceOf[Any].__scalapy_value
  }

  override def hashCode(): Int = __scalapy_value.hashCode()
}

object Any extends AnyPopulateWith {
  implicit def from[T: Writer](v: T): Any = {
    Any.populateWith(Writer.write(v))
  }

  def proxyFrom[T: ProxyWriter](v: T): Any = {
    Any.populateWith(ProxyWriter.writeProxy(v))
  }
}
