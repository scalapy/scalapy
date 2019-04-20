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

  object None

  type NoneOr[T] = None.type | T

  def `with`[T <: py.Object, O](ref: T)(withValue: T => O): O = {
    ref.asDynamic.__enter__()
    val ret = withValue(ref)
    ref.asDynamic.__exit__(None, None, None)
    ret
  }

  def local[T](f: => T): T = {
    py.PyValue.allocatedValues = List.empty[PyValue] :: py.PyValue.allocatedValues
    py.VariableReference.allocatedReferences = List.empty[VariableReference] :: py.VariableReference.allocatedReferences

    try {
      f
    } finally {
      py.PyValue.allocatedValues.head.foreach { c =>
        c.cleanup()
      }

      py.VariableReference.allocatedReferences.head.foreach { c =>
        c.cleanup()
      }

      py.PyValue.allocatedValues = py.PyValue.allocatedValues.tail
      py.VariableReference.allocatedReferences = py.VariableReference.allocatedReferences.tail
    }
  }

  import scala.annotation.StaticAnnotation
  class native extends StaticAnnotation

  import scala.language.experimental.macros
  def native[T]: T = macro ObjectFacadeImpl.native_impl[T]
  def nativeNamed[T]: T = macro ObjectFacadeImpl.native_named_impl[T]
}
