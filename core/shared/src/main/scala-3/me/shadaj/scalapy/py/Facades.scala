package me.shadaj.scalapy.py

import scala.language.dynamics

import me.shadaj.scalapy.interpreter.PyValue

trait StaticModule(name: String) extends Module {
  __scalapy__rawValue = module(name).__scalapy_value
}

trait StaticValue(value: Any) extends Any {
  __scalapy__rawValue = value.__scalapy_value
}

abstract class FacadeCreator[F <: Any] {
  def create(value: PyValue): F = 
    val f = create
    f.__scalapy__rawValue = value
    f
  
  def create: F
}

object FacadeCreator extends FacadesCreatorMacros
