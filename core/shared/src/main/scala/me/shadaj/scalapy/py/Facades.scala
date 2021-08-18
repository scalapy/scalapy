package me.shadaj.scalapy.py

import scala.language.experimental.macros

import me.shadaj.scalapy.interpreter.PyValue

// class FacadeValueProvider(private[scalapy] var rawValue: PyValue) extends Any

trait StaticModule(name: String) extends Module {
  rawValue = module(name).value
}

class StaticValue(value: Any) extends Any {
  rawValue = value.value
}

abstract class FacadeCreator[F <: Any] {
  def create: F
}

object FacadeCreator extends FacadesCreatorMacros {}
