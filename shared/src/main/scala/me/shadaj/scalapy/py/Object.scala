package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.collection.mutable

class Object(val value: PyValue) { self =>
  private var _expr: VariableReference = null
  def expr = {
    if (_expr == null) {
      _expr = interpreter.getVariableReference(value)
    }

    _expr
  }

  private var cleaned = false

  override def toString: String = value.getStringified

  override def finalize(): Unit = {
    if (!cleaned) {
      if (_expr != null) {
        _expr.cleanup()
      }

      cleaned = true
    }
  }

  def as[T: ObjectReader]: T = implicitly[ObjectReader[T]].read(new ValueAndRequestObject(interpreter.load(expr.variable)) {
    override def getObject: Object = self
  })
}

object Object {
  private var nextCounter: Int = 0

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
