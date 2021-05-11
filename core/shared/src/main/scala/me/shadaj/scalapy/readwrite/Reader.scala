package me.shadaj.scalapy.readwrite

import scala.reflect.ClassTag

import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.FacadeCreator

import scala.collection.mutable

trait Reader[T] {
  // the value is borrowed, the reader must never leak references to the PyValue
  def read(r: PyValue): T
}

object Reader extends TupleReaders with FunctionReaders {
  implicit val anyReader = new Reader[py.Any] {
    def read(r: PyValue): py.Any = py.Any.populateWith(r.dup())
  }

  implicit def facadeReader[F <: py.Any](implicit creator: FacadeCreator[F]): Reader[F] = new Reader[F] {
    override def read(r: PyValue): F = creator.create(r.dup())
  }

  implicit val unitReader = new Reader[Unit] {
    def read(r: PyValue): Unit = ()
  }

  implicit val byteReader = new Reader[Byte] {
    def read(r: PyValue): Byte = r.getLong.toByte
  }

  implicit val intReader = new Reader[Int] {
    def read(r: PyValue): Int = r.getLong.toInt
  }

  implicit val longReader = new Reader[Long] {
    def read(r: PyValue): Long = r.getLong
  }

  implicit val doubleReader = new Reader[Double] {
    def read(r: PyValue): Double = r.getDouble
  }

  implicit val floatReader = new Reader[Float] {
    def read(r: PyValue): Float = r.getDouble.toFloat
  }

  implicit val booleanReader = new Reader[Boolean] {
    def read(r: PyValue): Boolean = r.getBoolean
  }

  implicit val stringReader = new Reader[String] {
    def read(r: PyValue): String = r.getString
  }

  implicit val charReader = new Reader[Char] {
    def read(r: PyValue): Char = {
      val rStr = r.getString
      if (rStr.length != 1) {
        throw new IllegalArgumentException("Cannot extract a char from a string with length != 1")
      } else {
        rStr.head
      }
    }
  }

  implicit def mutableSeqReader[T](implicit reader: Reader[T], writer: Writer[T]): Reader[mutable.Seq[T]] = new Reader[mutable.Seq[T]] {
    def read(r: PyValue) = r.dup().getSeq(reader.read, writer.write)
  }

  implicit def seqReader[T](implicit reader: Reader[T]): Reader[Seq[T]] = new Reader[Seq[T]] {
    def read(r: PyValue) = r.dup().getSeq(reader.read, null).toSeq
  }

  implicit def mapReader[I, O](implicit readerI: Reader[I], readerO: Reader[O]): Reader[Map[I, O]] = new Reader[Map[I, O]] {
    override def read(r: PyValue): Map[I, O] = {
      r.dup().getMap.map { case (k, v) =>
        readerI.read(k) -> readerO.read(v)
      }.toMap
    }
  }
}
