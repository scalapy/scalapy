package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter

@native trait StringObjectFacade extends Object {
  def replace(old: String, newValue: String): String = native
}

@native trait StringModuleFacade extends Module {
  def digits: String = native
}

@native object StringModuleStaticFacade extends StaticModule("string") with StringModuleFacade

@native trait IntList extends Any {
  @PyBracketAccess
  def apply(index: Int): Int = native

  @PyBracketAccess
  def update(index: Int, newValue: Int): Unit = native
}
