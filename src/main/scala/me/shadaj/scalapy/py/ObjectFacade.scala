package me.shadaj.scalapy.py

import jep.Jep

import scala.reflect.macros.whitebox
import scala.language.experimental.macros

class ObjectFacade(originalObject: Object)(implicit jep: Jep) extends Object(originalObject.variableId) {
  final val toObject = originalObject
  final val toDynamic = originalObject.asInstanceOf[DynamicObject]

  protected def native[T]: T = macro ObjectFacadeMacro.native_impl[T]
  protected def nativeNamed[T]: T = macro ObjectFacadeMacro.native_named_impl[T]

//  override def finalize(): Unit = {} // let the originalObject handle this
}


