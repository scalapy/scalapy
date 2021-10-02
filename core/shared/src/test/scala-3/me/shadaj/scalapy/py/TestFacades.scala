package me.shadaj.scalapy.py

import scala.language.dynamics

import me.shadaj.scalapy.interpreter

@native class StringObjectFacade extends Object {
  def replace(old: String, newValue: String): String = native
}

@native class StringModuleFacade extends Module {
  def digits: String = native
}

@native object StringModuleStaticFacade extends StringModuleFacade with StaticModule("string")

@native class IntList extends Any {
  @PyBracketAccess
  def apply(index: Int): Int = native

  @PyBracketAccess
  def update(index: Int, newValue: Int): Unit = native
}
