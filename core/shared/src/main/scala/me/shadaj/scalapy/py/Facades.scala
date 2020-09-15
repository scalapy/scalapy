package me.shadaj.scalapy.py

import scala.language.experimental.macros

import me.shadaj.scalapy.py.interpreter.PyValue

class FacadeValueProvider(private[py] val value: PyValue) extends Any

abstract class FacadeCreator[F <: Any] {
  def create(value: PyValue): F
}

object FacadeCreator {
  implicit def getCreator[F <: Any]: FacadeCreator[F] = macro FacadeImpl.creator[F]
}
