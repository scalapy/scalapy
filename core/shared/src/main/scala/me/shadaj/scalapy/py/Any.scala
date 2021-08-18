package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.collection.mutable

import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue}
import me.shadaj.scalapy.readwrite.{Reader, Writer}

trait Any { self =>
  private var cleaned = false
    // var noOfObjects = 0;
  
    // // Instead of performing increment in the constructor
    // // instance block is preferred to make this program generic.
    // {
    //     noOfObjects += 1;
    // }
    
  private[scalapy] var rawValue: PyValue = null
  private[scalapy] def value: PyValue = {
    if (cleaned) {
      throw new IllegalAccessException("The Python value you are try to access has already been released by a call to py.Any.del()")
    } else {
     // println("HASHCODE: " + rawValue.hashCode() + " rawValue: " + rawValue.getStringified)
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

object Any {
  def populateWith(v: PyValue): Any = {
    new Any {
      rawValue = v
    }
  }

  implicit def from[T](v: T)(implicit writer: Writer[T]): Any = {
    Any.populateWith(writer.write(v))
  }
}
