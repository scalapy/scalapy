package me.shadaj.scalapy.py

import scala.reflect.ClassTag

import me.shadaj.scalapy.py.interpreter.PyValue

trait Reader[T] {
  def read(r: PyValue): T
}

object Reader extends TupleReaders {
  implicit val anyReader = new Reader[Any] {
    def read(r: PyValue): Any = Any.populateWith(r)
  }

  implicit def facadeReader[F <: Any](implicit creator: FacadeCreator[F]): Reader[F] = new Reader[F] {
    override def read(r: PyValue): F = creator.create(r)
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

  implicit def seqReader[T](implicit reader: Reader[T]): Reader[Seq[T]] = new Reader[Seq[T]] {
    def read(r: PyValue) = r.getSeq.map(reader.read)
  }

  implicit def mapReader[I, O](implicit readerI: Reader[I], readerO: Reader[O]): Reader[Map[I, O]] = new Reader[Map[I, O]] {
    override def read(r: PyValue): Map[I, O] = {
      r.getMap.map { case (k, v) =>
        readerI.read(k) -> readerO.read(v)
      }.toMap
    }
  }
}
