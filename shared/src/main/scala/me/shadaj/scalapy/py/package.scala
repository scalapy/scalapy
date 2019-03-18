package me.shadaj.scalapy

import jep.Jep

import scala.collection.mutable

import scala.concurrent.Future

package object py {
  def module(name: String)(implicit jep: Jep) = Module(name)
  def module(name: String, subname: String)(implicit jep: Jep) = Module(name, subname)

  def global(implicit jep: Jep) = new Global()

  object None

  type NoneOr[T] = None.type | T

  def `with`[T <: py.Object, O](ref: T)(withValue: T => O)(implicit jep: Jep): O = {
    ref.asInstanceOf[DynamicObject].__enter__()
    val ret = withValue(ref)
    ref.asInstanceOf[DynamicObject].__exit__(None, None, None)
    ret
  }

  def local(f: => Unit): Unit = {
    py.Object.allocatedObjects = mutable.Queue.empty[py.Object] :: py.Object.allocatedObjects
    f

    val toClean = py.Object.allocatedObjects.head

    toClean.foreach { c =>
      c.finalize()
    }

    py.Object.allocatedObjects = py.Object.allocatedObjects.tail
  }
}
