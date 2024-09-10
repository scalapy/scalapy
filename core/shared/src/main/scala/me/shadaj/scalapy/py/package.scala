package me.shadaj.scalapy

import scala.collection.mutable
import scala.language.implicitConversions

import scala.concurrent.Future

import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue}
import me.shadaj.scalapy.readwrite.{Reader, Writer}
import scala.collection.mutable.Queue
import me.shadaj.scalapy.interpreter.Platform

package object py extends PyMacros {
  def module(name: String) = Module(name)
  def module(name: String, subname: String) = Module(name, subname)

  val None = Any.populateWith(CPythonInterpreter.noneValue).as[PyNone.None]

  type NoneOr[T] = PyNone.None | T

  def `with`[T <: py.Any, O](ref: T)(withValue: T => O): O = {
    implicit val facadeReader: Reader[Dynamic] = Reader.facadeReader[Dynamic](FacadeCreator.getCreator[Dynamic])
    ref.as[Dynamic].__enter__()
    try {
      withValue(ref)
    } finally {
      ref.as[Dynamic].__exit__(None, None, None)
    }
  }

  def local[T](f: => T): T = {
    val myQueue = Queue.empty[PyValue]
    PyValue.allocatedValues.get().push(myQueue)

    try {
      f
    } finally {
      myQueue.foreach(_.cleanup(ignoreCleaned = true))
      assert(PyValue.allocatedValues.get().pop.eq(myQueue))
    }
  }

  implicit class SeqConverters[T, C](seq: C)(implicit ev: C => scala.collection.Seq[T]) {
    def toPythonCopy(implicit elemWriter: ConvertableToSeqElem[T]): Any = {
      Any.populateWith(PyValue.fromNew(implicitly[ConvertableToSeqElem[C]].convertCopy(seq)))
    }

    def toPythonProxy(implicit elemWriter: ConvertableToSeqElem[T]): Any = {
      Any.populateWith(implicitly[ConvertableToSeqElem[C]].convertProxy(seq))
    }
  }

  def eval(str: String): Dynamic = {
    Any.populateWith(CPythonInterpreter.load(str)).as[Dynamic]
  }

  def exec(str: String): Unit = {
    CPythonInterpreter.execManyLines(str)
  }

  final class PyQuotable(val variable: String) extends AnyVal {
    def cleanup() = CPythonInterpreter.cleanupVariableReference(variable)
  }

  object PyQuotable {
    implicit def fromAny(any: py.Any): PyQuotable = {
      new PyQuotable(CPythonInterpreter.getVariableReference(any.__scalapy_value))
    }

    implicit def fromValue[V](value: V)(implicit writer: Writer[V]): PyQuotable = {
      new PyQuotable(CPythonInterpreter.getVariableReference(writer.write(value)))
    }
  }

  implicit class PyQuote(private val sc: StringContext) extends AnyVal {
    def py(values: PyQuotable*): Dynamic = {
      val strings = sc.parts.iterator
      val expressions = values.iterator
      val buf = new StringBuffer(strings.next)
      while (strings.hasNext) {
        val expr = expressions.next
        buf.append(expr.variable)
        buf.append(strings.next)
      }

      try {
        eval(buf.toString)
      } finally {
        values.foreach(_.cleanup())
      }
    }
  }
}
