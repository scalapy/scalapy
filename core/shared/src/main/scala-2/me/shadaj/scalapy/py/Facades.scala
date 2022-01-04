package me.shadaj.scalapy.py

import scala.language.experimental.macros

import me.shadaj.scalapy.interpreter.PyValue

class FacadeValueProvider(private[scalapy] val __scalapy__rawValue: PyValue) extends Any

class StaticModule(name: String) extends Module {
  private[scalapy] val __scalapy__rawValue = module(name).__scalapy_value
}

class StaticValue(value: Any) extends Any {
  private[scalapy] val __scalapy__rawValue = value.__scalapy_value
}

abstract class FacadeCreator[F <: Any] {
  def create(value: PyValue): F
}

object FacadeCreator extends FacadesCreatorMacros
