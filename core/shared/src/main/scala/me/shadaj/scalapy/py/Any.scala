package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.collection.mutable

trait Any extends scala.Any { self =>
  private[py] def value: PyValue
  
  final def expr: VariableReference = {
    interpreter.getVariableReference(value)
  }

  override def toString: String = value.getStringified

  final def as[T: Reader]: T = implicitly[Reader[T]].read(new ValueAndRequestRef(value) {
    override def getRef: Any = self
  })
}

object Any {
  def populateWith(v: PyValue): Any = {
    new Any {
      val value = v
    }
  }

  implicit def from[T](v: T)(implicit writer: Writer[T]): Any = {
    writer.write(v).left.map(Any.populateWith).merge
  }
}
