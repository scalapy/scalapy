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

  def `with`[T <: py.Any, O](ref: T)(withValue: T => O): O = {
    ref.as[Dynamic](Reader.facadeReader[Dynamic](FacadeCreator.getCreator[Dynamic])).__enter__()
    val ret = withValue(ref)
    ref.as[Dynamic].__exit__(None, None, None)
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

  import py.{Dynamic => PyDynamic, Any => PyAny}
  trait PyQuotable {
    def stringToInsert: String
  }

  object PyQuotable {
    implicit def fromAny(any: py.Any): PyQuotable = new PyQuotable {
      def stringToInsert: String = any.expr.toString
    }

    implicit def fromValue[V](value: V)(implicit writer: Writer[V]): PyQuotable = new PyQuotable {
      def stringToInsert: String = writer.write(value).left.map(Any.populateWith).merge.expr.toString
    }
  }

  implicit class PyQuote(private val sc: StringContext) extends AnyVal {
    def py(values: PyQuotable*): PyDynamic = {
      local {
        val strings = sc.parts.iterator
        val expressions = values.iterator
        var buf = new StringBuffer(strings.next)
        while (strings.hasNext) {
          buf append expressions.next.stringToInsert
          buf append strings.next
        }
        
        PyAny.populateWith(interpreter.load(buf.toString)).as[Dynamic]
      }
    }
  }

  def eval(str: String): PyDynamic = {
    PyAny.populateWith(interpreter.load(str.toString)).as[Dynamic]
  }

  import scala.language.experimental.macros
  def native[T]: T = macro FacadeImpl.native_impl[T]
  def nativeNamed[T]: T = macro FacadeImpl.native_named_impl[T]
}
