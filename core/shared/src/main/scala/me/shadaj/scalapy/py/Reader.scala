package me.shadaj.scalapy.py

import scala.reflect.ClassTag
import scala.collection.JavaConverters._

abstract class ValueAndRequestRef(val value: PyValue) {
  protected def getRef: Any

  private var refCache: Any = null
  final def requestRef: Any = {
    if (refCache == null) refCache = getRef
    refCache
  }
}

trait Reader[T] {
  def read(r: ValueAndRequestRef): T
}

object Reader extends TupleReaders {
  implicit val anyReader = new Reader[Any] {
    def read(r: ValueAndRequestRef): Any = r.requestRef
  }

  implicit def facadeReader[F <: Any](implicit creator: FacadeCreator[F]): Reader[F] = new Reader[F] {
    override def read(r: ValueAndRequestRef): F = creator.create(r.requestRef.value)
  }

  implicit val unitReader = new Reader[Unit] {
    def read(r: ValueAndRequestRef): Unit = ()
  }

  implicit val byteReader = new Reader[Byte] {
    def read(r: ValueAndRequestRef): Byte = r.value.getLong.toByte
  }

  implicit val intReader = new Reader[Int] {
    def read(r: ValueAndRequestRef): Int = r.value.getLong.toInt
  }

  implicit val longReader = new Reader[Long] {
    def read(r: ValueAndRequestRef): Long = r.value.getLong
  }

  implicit val doubleReader = new Reader[Double] {
    def read(r: ValueAndRequestRef): Double = r.value.getDouble
  }

  implicit val floatReader = new Reader[Float] {
    def read(r: ValueAndRequestRef): Float = r.value.getDouble.toFloat
  }

  implicit val booleanReader = new Reader[Boolean] {
    def read(r: ValueAndRequestRef): Boolean = {
      r.value.getBoolean
    }
  }

  implicit val stringReader = new Reader[String] {
    def read(r: ValueAndRequestRef): String = r.value.getString
  }

  implicit def seqReader[T](implicit reader: Reader[T]): Reader[Seq[T]] = new Reader[Seq[T]] {
    def read(r: ValueAndRequestRef) = {
      r.value.getSeq.zipWithIndex.map { case (v, i) =>
        reader.read(new ValueAndRequestRef(v) {
          def getRef = r.requestRef.as[Dynamic].arrayAccess(i)
        })
      }.toSeq
    }
  }

  implicit def mapReader[I, O](implicit readerI: Reader[I], readerO: Reader[O]): Reader[Map[I, O]] = new Reader[Map[I, O]] {
    override def read(r: ValueAndRequestRef): Map[I, O] = {
      r.value.getMap.map { case (k, v) =>
        readerI.read(new ValueAndRequestRef(k) {
          def getRef = throw new IllegalAccessException("Cannot read a Python object for the key of a map")
        }) -> readerO.read(new ValueAndRequestRef(v) {
          def getRef = {
            r.requestRef.as[Dynamic].dictionaryAccess(Any.populateWith(k))
          }
        })
      }.toMap
    }
  }
}
