package me.shadaj.scalapy.py

import scala.language.experimental.macros

import me.shadaj.scalapy.interpreter.PyValue

class FacadeValueProvider(private[scalapy] val rawValue: PyValue) extends Any

class StaticModule(name: String) extends Module {
  private[scalapy] val rawValue = module(name).value
}

abstract class FacadeCreator[F <: Any] {
  def create(value: PyValue): F
}

object FacadeCreator {
  implicit def getCreator[F <: Any]: FacadeCreator[F] = macro FacadeImpl.creator[F]
}
