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

  if (Object.allocatedObjects.nonEmpty) {
    Object.allocatedObjects.head += this
  } else if (Platform.isNative) {
    println(s"Warning: the object $this was allocated into a global space, which means it will not be garbage collected in Scala Native")
  }

  override def toString: String = value.getStringified

  override def finalize(): Unit = {
    if (!cleaned) {
      if (_expr != null) {
        _expr.finalize()
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
  private[py] var allocatedObjects: List[mutable.Queue[Object]] = List.empty

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
