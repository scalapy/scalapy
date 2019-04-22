package me.shadaj.scalapy.py

import scala.reflect.ClassTag
import scala.collection.JavaConverters._

abstract class ValueAndRequestObject(val value: PyValue) {
  protected def getObject: Object

  private var objectCache: Object = null
  final def requestObject: Object = {
    if (objectCache == null) objectCache = getObject
    objectCache
  }
}

trait ObjectReader[T] {
  def read(r: ValueAndRequestObject): T
}

object ObjectReader extends ObjectTupleReaders {
  implicit val wrapperReader = new ObjectReader[Object] {
    def read(r: ValueAndRequestObject): Object = r.requestObject
  }

  implicit val wrapperDynReader = new ObjectReader[Dynamic] {
    def read(r: ValueAndRequestObject): Dynamic = r.requestObject.asDynamic
  }

  implicit def facadeReader[F <: Object](implicit creator: FacadeCreator[F]): ObjectReader[F] = new ObjectReader[F] {
    override def read(r: ValueAndRequestObject): F = creator.create(r.value)
  }

  implicit val unitReader = new ObjectReader[Unit] {
    def read(r: ValueAndRequestObject): Unit = ()
  }

  implicit val byteReader = new ObjectReader[Byte] {
    def read(r: ValueAndRequestObject): Byte = r.value.getLong.toByte
  }

  implicit val intReader = new ObjectReader[Int] {
    def read(r: ValueAndRequestObject): Int = r.value.getLong.toInt
  }

  implicit val longReader = new ObjectReader[Long] {
    def read(r: ValueAndRequestObject): Long = r.value.getLong
  }

  implicit val doubleReader = new ObjectReader[Double] {
    def read(r: ValueAndRequestObject): Double = r.value.getDouble
  }

  implicit val floatReader = new ObjectReader[Float] {
    def read(r: ValueAndRequestObject): Float = r.value.getDouble.toFloat
  }

  implicit val booleanReader = new ObjectReader[Boolean] {
    def read(r: ValueAndRequestObject): Boolean = {
      r.value.getBoolean
    }
  }

  implicit val stringReader = new ObjectReader[String] {
    def read(r: ValueAndRequestObject): String = r.value.getString
  }

  implicit def seqReader[T](implicit reader: ObjectReader[T]): ObjectReader[Seq[T]] = new ObjectReader[Seq[T]] {
    def read(r: ValueAndRequestObject) = {
      r.value.getSeq.zipWithIndex.map { case (v, i) =>
        reader.read(new ValueAndRequestObject(v) {
          def getObject = r.requestObject.asDynamic.arrayAccess(i)
        })
      }.toSeq
    }
  }

  implicit def mapReader[I, O](implicit readerI: ObjectReader[I], readerO: ObjectReader[O]): ObjectReader[Map[I, O]] = new ObjectReader[Map[I, O]] {
    override def read(r: ValueAndRequestObject): Map[I, O] = {
      r.value.getMap.map { case (k, v) =>
        readerI.read(new ValueAndRequestObject(k) {
          def getObject = throw new IllegalAccessException("Cannot read a Python object for the key of a map")
        }) -> readerO.read(new ValueAndRequestObject(v) {
          def getObject = {
            if (Platform.isNative) ??? else {
              r.requestObject.asDynamic.dictionaryAccess(
                Object.populateWith(interpreter.asInstanceOf[JepInterpreter].valueFromAny(k))
              )
            }
          }
        })
      }.toMap
    }
  }
}
