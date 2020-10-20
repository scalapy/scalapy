package me.shadaj.scalapy

import scala.collection.mutable

import scala.concurrent.Future

import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue, VariableReference}
import me.shadaj.scalapy.readwrite.{Reader, Writer}

package object py {
  def module(name: String) = Module(name)
  def module(name: String, subname: String) = Module(name, subname)

  @py.native trait None extends Any
  val None = Any.populateWith(CPythonInterpreter.noneValue).as[None]

  type NoneOr[T] = None | T

  def `with`[T <: py.Any, O](ref: T)(withValue: T => O): O = {
    ref.as[Dynamic](Reader.facadeReader[Dynamic](FacadeCreator.getCreator[Dynamic])).__enter__()
    try {
      withValue(ref)
    } finally {
      ref.as[Dynamic].__exit__(None, None, None)
    }
  }

  def local[T](f: => T): T = {
    PyValue.allocatedValues = List.empty[PyValue] :: PyValue.allocatedValues
    VariableReference.allocatedReferences = List.empty[VariableReference] :: VariableReference.allocatedReferences

    try {
      f
    } finally {
      PyValue.allocatedValues.head.foreach { c =>
        c.cleanup()
      }

      VariableReference.allocatedReferences.head.foreach { c =>
        c.cleanup()
      }

      PyValue.allocatedValues = PyValue.allocatedValues.tail
      VariableReference.allocatedReferences = VariableReference.allocatedReferences.tail
    }
  }

  implicit class SeqConverters[T, C <% Seq[T]](seq: C) {
    def toPythonCopy(implicit elemWriter: Writer[T]): Any = {
      Any.populateWith(CPythonInterpreter.createListCopy(seq, elemWriter.write))
    }

    def toPythonProxy(implicit elemWriter: Writer[T]): Any = {
      Any.populateWith(CPythonInterpreter.createListProxy(seq, elemWriter.write))
    }
  }

  import py.{Dynamic => PyDynamic, Any => PyAny}
  trait PyQuotable {
    def stringToInsert: String
    def cleanup(): Unit
  }

  object PyQuotable {
    implicit def fromAny(any: py.Any): PyQuotable = new PyQuotable {
      private val expr = any.expr
      def stringToInsert: String = expr.toString
      def cleanup() = expr.cleanup()
    }

    implicit def fromValue[V](value: V)(implicit writer: Writer[V]): PyQuotable = new PyQuotable {
      private val expr = Any.populateWith(writer.write(value)).expr
      def stringToInsert: String = expr.toString
      def cleanup() = expr.cleanup()
    }
  }

  implicit class PyQuote(private val sc: StringContext) extends AnyVal {
    def py(values: PyQuotable*): PyDynamic = {
      val strings = sc.parts.iterator
      val expressions = values.iterator
      var buf = new StringBuffer(strings.next)
      val toCleanup = mutable.Queue[PyQuotable]()
      while (strings.hasNext) {
        val expr = expressions.next
        buf append expr.stringToInsert
        toCleanup += expr

        buf append strings.next
      }
      
      val ret = PyAny.populateWith(CPythonInterpreter.load(buf.toString)).as[Dynamic]
      toCleanup.foreach(_.cleanup())
      ret
    }
  }

  def eval(str: String): PyDynamic = {
    PyAny.populateWith(CPythonInterpreter.load(str)).as[Dynamic]
  }

  import scala.language.experimental.macros
  def native[T]: T = macro FacadeImpl.native_impl[T]
  def nativeNamed[T]: T = macro FacadeImpl.native_named_impl[T]
}
