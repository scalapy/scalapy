package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics

case class Ref private[py](private[py] val expr: String) extends scala.Dynamic {
  def applyDynamic(method: String)(params: Ref*): Ref = {
    if (method == "apply") {
      // TODO: do we need this case?
      Ref(s"($expr)(${params.map(_.expr).mkString(",")})")
    } else {
      Ref(s"($expr).$method(${params.map(_.expr).mkString(",")})")
    }
  }

  def selectDynamic(value: String): Ref = {
    Ref(s"($expr).$value")
  }

  def arrayAccess(key: Ref): Ref = {
    Ref(s"($expr)[${key.expr}]")
  }

  def +(that: Ref): Ref = {
    Ref(s"($expr) + (${that.expr})")
  }

  def -(that: Ref): Ref = {
    Ref(s"($expr) - (${that.expr})")
  }

  def *(that: Ref): Ref = {
    Ref(s"($expr) * (${that.expr})")
  }

  def /(that: Ref): Ref = {
    Ref(s"($expr) / (${that.expr})")
  }

  def %(that: Ref): Ref = {
    Ref(s"($expr) % (${that.expr})")
  }

  def as[T](implicit reader: ObjectReader[T], jep: Jep): T = reader.read(this)

  def toObject(implicit jep: Jep): Object = {
    Object(expr)
  }

  def execute(implicit jep: Jep): Unit = {
    jep.eval(expr)
  }
}

object Ref {
  implicit def from[T](v: T)(implicit writer: ObjectWriter[T], jep: Jep): Ref = {
    writer.write(v)
  }
}