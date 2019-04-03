package me.shadaj.scalapy

import scala.collection.mutable

import scala.concurrent.Future

package object py {
  private var _interpreter: Platform.InterpreterImplementation = null
  def interpreter = {
    if (_interpreter == null) {
      _interpreter = Platform.newInterpreter
    }
    
    _interpreter
  }
  
  def module(name: String) = Module(name)
  def module(name: String, subname: String) = Module(name, subname)

  val global = new Global()

  object None

  type NoneOr[T] = None.type | T

  def `with`[T <: py.Object, O](ref: T)(withValue: T => O): O = {
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
