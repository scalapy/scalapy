package me.shadaj.scalapytests

import me.shadaj.scalapy.py._

import scala.language.dynamics

import me.shadaj.scalapy.interpreter

@native class StringObjectFacade extends Object {
  def replace(old: String, newValue: String): String = native
}

@native class StringModuleFacade extends Module {
  def digits: String = native

  // this is an attribute, not a method
  def ascii_letters(): String = native
}

@native object StringModuleStaticFacade extends StringModuleFacade with StaticModule("string")

@native class IntList extends Any {
  @PyBracketAccess
  def apply(index: Int): Int = native

  @PyBracketAccess
  def update(index: Int, newValue: Int): Unit = native
}

@native class ReduceFacade extends Any {
  def reduce(lambda: (Int, Int) => Int, numbers: Any, initializer: Int): Int = native
}
