package me.shadaj.scalapy.py

import jep.{Jep, NDArray}

import scala.reflect.ClassTag
import scala.collection.JavaConverters._

abstract class ValueAndRequestObject(getValue: => Any) {
  final def value: Any = getValue

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
    def read(r: ValueAndRequestObject): Object = new DynamicObject(r.requestObject.variableId)
  }

  implicit val wrapperDynReader = new ObjectReader[DynamicObject] {
    def read(r: ValueAndRequestObject): DynamicObject =
      new DynamicObject(r.requestObject.variableId)
  }

  implicit def facadeReader[F <: ObjectFacade](implicit classTag: ClassTag[F]): ObjectReader[F] = new ObjectReader[F] {
    override def read(r: ValueAndRequestObject): F = {
      classTag.runtimeClass.getConstructor(classOf[Object]).newInstance(new DynamicObject(r.requestObject.variableId)).asInstanceOf[F]
    }
  }

  def toByte(value: Any): Byte = {
    value match {
      case b: Byte => b
      case i: Int => if (i <= Byte.MaxValue && i >= Byte.MaxValue) i.toByte else {
        throw new IllegalArgumentException("Tried to convert a Int outside Byte range to an Byte")
      }
      case l: Long => if (l <= Byte.MaxValue && l >= Byte.MinValue) l.toByte else {
        throw new IllegalArgumentException("Tried to convert a Long outside Byte range to an Byte")
      }
      case _: Double =>
        throw new IllegalArgumentException("Cannot up-convert a Double to a Byte")
      case _: Float =>
        throw new IllegalArgumentException("Cannot up-convert a Float to a Byte")
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass}")
    }
  }

  def toInt(value: Any): Int = {
    value match {
      case b: Byte => b
      case i: Int => i
      case l: Long => if (l <= Int.MaxValue && l >= Int.MinValue) l.toInt else {
        throw new IllegalArgumentException("Tried to convert a Long outside Int range to an Int")
      }
      case _: Double =>
        throw new IllegalArgumentException("Cannot up-convert a Double to an Int")
      case _: Float =>
        throw new IllegalArgumentException("Cannot up-convert a Float to an Int")
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass}: $value")
    }
  }

  def toLong(value: Any): Long = {
    value match {
      case b: Byte => b
      case i: Int => i
      case l: Long => l
      case _: Double =>
        throw new IllegalArgumentException("Cannot up-convert a Double to a Long")
      case _: Float =>
        throw new IllegalArgumentException("Cannot up-convert a Float to a Long")
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass}")
    }
  }

  def toDouble(value: Any): Double = {
    value match {
      case i: Int => i
      case l: Long => l
      case d: Double => d
      case f: Float => f
      case s: String => s.toDouble
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass} for value $value")
    }
  }

  def toFloat(value: Any): Float = {
    value match {
      case i: Int => i
      case l: Long => l
      case d: Double =>
        if (d.toFloat == d) d.toFloat else {
          throw new IllegalArgumentException("Cannot up-convert a Double to a Float")
        }
      case fl: Float => fl
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass}")
    }
  }

  implicit val unitReader = new ObjectReader[Unit] {
    def read(r: ValueAndRequestObject): Unit = ()
  }

  implicit val byteReader = new ObjectReader[Byte] {
    def read(r: ValueAndRequestObject): Byte = toByte(r.value)
  }

  implicit val intReader = new ObjectReader[Int] {
    def read(r: ValueAndRequestObject): Int = toInt(r.value)
  }

  implicit val longReader = new ObjectReader[Long] {
    def read(r: ValueAndRequestObject): Long = toLong(r.value)
  }

  implicit val doubleReader = new ObjectReader[Double] {
    def read(r: ValueAndRequestObject): Double = toDouble(r.value)
  }

  implicit val floatReader = new ObjectReader[Float] {
    def read(r: ValueAndRequestObject): Float = toFloat(r.value)
  }

  implicit val booleanReader = new ObjectReader[Boolean] {
    def read(r: ValueAndRequestObject): Boolean = {
      r.value match {
        case b: Boolean =>
          b
        case s: String =>
          s == "True"
        case i: Int if i == 0 || i == 1 =>
          i == 1
        case o =>
          throw new IllegalArgumentException(s"Unknown boolean type for value $o")
      }
    }
  }

  implicit val stringReader = new ObjectReader[String] {
    def read(r: ValueAndRequestObject): String = r.value.asInstanceOf[String]
  }

  implicit def seqReader[T](implicit reader: ObjectReader[T]): ObjectReader[Seq[T]] = new ObjectReader[Seq[T]] {
    def read(r: ValueAndRequestObject) = {
      (r.value match {
        case arr: Array[_] =>
          arr.zipWithIndex
        case arrList: java.util.List[_] =>
          arrList.toArray.zipWithIndex
        case ndArr: NDArray[Array[_]] =>
          ndArr.getData.zipWithIndex
      }).map { case (v, i) =>
        reader.read(new ValueAndRequestObject(v) {
          override def getObject: Object = r.requestObject.asInstanceOf[DynamicObject].arrayAccess(i)
        })
      }.toSeq
    }
  }

  implicit def mapReader[I, O](implicit readerI: ObjectReader[I], readerO: ObjectReader[O]): ObjectReader[Map[I, O]] = new ObjectReader[Map[I, O]] {
    override def read(r: ValueAndRequestObject): Map[I, O] = {
      r.value.asInstanceOf[java.util.Map[_, _]].asScala.map { case (k, v) =>
        readerI.read(new ValueAndRequestObject(k) {
          override def getObject: Object =
            throw new IllegalAccessException("Cannot read a Python object for the key of a map")
        }) -> readerO.read(new ValueAndRequestObject(v) {
          override def getObject: Object = r.requestObject.asInstanceOf[DynamicObject].arrayAccess(Object.populateWith(k))
        })
      }.toMap
    }
  }
}
