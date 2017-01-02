package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics

class Ref private[py](private[py] val expr: String) {
  def as[T](implicit reader: ObjectReader[T], jep: Jep): T = reader.read(this)

  def toObject(implicit jep: Jep): Object = {
    Object(expr)
  }

  def execute(implicit jep: Jep): Unit = {
    jep.eval(expr)
  }
}

object Ref {
  def apply(expr: String): Ref = {
    new DynamicRef(expr)
  }

  implicit def from[T](v: T)(implicit writer: ObjectWriter[T], jep: Jep): Ref = {
    if (v == null) {
      None
    } else {
      writer.write(v)
    }
  }
}