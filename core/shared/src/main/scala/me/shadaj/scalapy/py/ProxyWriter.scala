package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.readwrite.{Reader, Writer}

/* A ProxyWriter is a type class that encapsulates transferring scala objects to python.
 *
 * In contrast to the Writer[T] type class, the ProxyWriter focuses on lazy transformations.
 *
 * For example:
 * The Writer[Seq[Int]] will copy all Int elements over to python and the python list
 * can exist independently of the scala Seq[Int].
 * The ProxyWriter[Seq[Int]] will not copy all elements, but will fetch element whenever they
 * are needed.
 */
trait ProxyWriter[T] {
  def writeProxy(v: T): PyValue
}

object ProxyWriter {
  @inline def writeProxy[A](value: A)(implicit writer: ProxyWriter[A]): PyValue = writer.writeProxy(value)

  @inline private implicit def asWriter[T](implicit ppWriter: PreferProxyWriter[T]): Writer[T] =
    ppWriter match {
      case PreferProxyWriter.PWriter(writer) => new Writer[T]{
        override def write(t:T): PyValue = writer.writeProxy(t)
      }
      case PreferProxyWriter.CWriter(writer) => writer
    }

  implicit def sequenceProxy[T:PreferProxyWriter, C[_]]
    (implicit ev: C[T] => scala.collection.Seq[T]): ProxyWriter[C[T]] =
    new ProxyWriter[C[T]] {
      def writeProxy(v: C[T]): PyValue =
        SequenceProxy.createListProxy(ev(v))
    }

  implicit def mapProxy[K: Reader: Writer, V: PreferProxyWriter, M[_, _]]
    (implicit ev: M[K, V] <:< scala.collection.Map[K, V]): ProxyWriter[M[K, V]] =
    new ProxyWriter[M[K, V]] {
      override def writeProxy(v: M[K, V]): PyValue =
        MappingProxy.createMapProxy(ev(v))
    }
}

/* This trait has only purpose: it gives ProxyWriter a higher priority over (copy) Writers.
 * It does this by wrapping ProxyWriter with a PWriter and (copy) Writer with a CWriter.
 *
 * The implicit wrapping method for (copy) Writer is defined in an inherited trait, that gives this
 * wrapper a lower priority, so it will only be used if the higher priority wrapper for ProxyWriter
 * fails to be constructed, that only happens if no instance for ProxyWriter[T] could be found.
 *
 * Unwrapping can be done with ProxyWriter.asWriter
 */
sealed trait PreferProxyWriter[T]
object PreferProxyWriter extends PreferProxyWriterLowPriority {
  case class PWriter[T](writer: ProxyWriter[T]) extends PreferProxyWriter[T]
  case class CWriter[T](writer: Writer[T]) extends PreferProxyWriter[T]

  implicit def proxyWriter[T](implicit writer: ProxyWriter[T]): PWriter[T] =
    PWriter(writer)
}

trait PreferProxyWriterLowPriority { self: PreferProxyWriter.type =>
  implicit def copyWriter[T](implicit writer: Writer[T]): CWriter[T] =
    CWriter(writer)
}
