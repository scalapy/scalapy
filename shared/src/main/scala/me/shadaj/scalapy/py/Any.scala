package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.collection.mutable

trait Any extends scala.Any { self =>
  private[py] def value: PyValue
  
  final def expr: VariableReference = {
    interpreter.getVariableReference(value)
  }

  override def toString: String = value.getStringified

  final def asDynamic: Dynamic = new Dynamic(value)
  final def as[T: ObjectReader]: T = implicitly[ObjectReader[T]].read(new ValueAndRequestObject(value) {
    override def getObject: Any = self
  })
}

object Any {
  def populateWith(v: PyValue): Any = {
    new Any {
      val value = v
    }
  }

  implicit def from[T](v: T)(implicit writer: ObjectWriter[T]): Any = {
    writer.write(v).left.map(Any.populateWith).merge
  }

  import scala.language.experimental.macros
  implicit def getCreator[F <: Any]: FacadeCreator[F] = macro FacadeImpl.creator[F]
}
