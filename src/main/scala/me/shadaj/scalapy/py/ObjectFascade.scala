package me.shadaj.scalapy.py

import jep.Jep

class ObjectFascade(originalObject: Object)(implicit jep: Jep) extends Ref(originalObject.expr) {
  private val keeper = originalObject.keeper

  protected val dynamic = originalObject.asInstanceOf[DynamicObject]

  override def toString: String = originalObject.toString
}
