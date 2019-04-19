package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.collection.mutable

class Object(val value: PyValue) { self =>
  private var _expr: VariableReference = null
  def expr: VariableReference = {
    if (_expr == null) {
      _expr = interpreter.getVariableReference(value)
    }

    _expr
  }

  override def toString: String = value.getStringified

  def as[T: ObjectReader]: T = implicitly[ObjectReader[T]].read(new ValueAndRequestObject(value) {
    override def getObject: Object = self
  })
}

object Object {
  def apply(stringToEval: String): Object = {
    populateWith(interpreter.load(stringToEval))
  }

  def populateWith(v: PyValue): Object = {
    new DynamicObject(v)
  }

  implicit def from[T](v: T)(implicit writer: ObjectWriter[T]): Object = {
    writer.write(v).left.map(Object.populateWith).merge
  }
}
