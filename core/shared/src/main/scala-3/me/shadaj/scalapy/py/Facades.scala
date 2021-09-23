package me.shadaj.scalapy.py

import scala.language.dynamics

import me.shadaj.scalapy.interpreter.PyValue

trait StaticModule(name: String) extends Module {
  rawValue = module(name).value
}

trait StaticValue(value: Any) extends Any {
  rawValue = value.value
}

abstract class FacadeCreator[F <: Any] {
  def create(value: PyValue): F = 
    val f = create
    f.rawValue = value
    f
  
  def create: F
}

object FacadeCreator extends FacadesCreatorMacros
