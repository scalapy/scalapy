package me.shadaj.scalapy.py

import jep.Jep

import scala.reflect.ClassTag

import scala.collection.JavaConverters._

trait ObjectReader[T] {
  def read(r: Any)(implicit jep: Jep): T
}

object ObjectReader extends ObjectTupleReaders {
  implicit val wrapperReader = new ObjectReader[Object] {
    def read(r: Any)(implicit jep: Jep): Object = Object { (variable, jep) =>
      jep.set(variable, r)
    }
  }

  implicit val wrapperDynReader = new ObjectReader[DynamicObject] {
    def read(r: Any)(implicit jep: Jep): DynamicObject = Object { (variable, jep) =>
      jep.set(variable, r)
    }.asInstanceOf[DynamicObject]
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
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass}")
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
    def read(r: Any)(implicit jep: Jep): Unit = ()
  }

  implicit val byteReader = new ObjectReader[Byte] {
    def read(r: Any)(implicit jep: Jep): Byte = toByte(r)
  }

  implicit val intReader = new ObjectReader[Int] {
    def read(r: Any)(implicit jep: Jep): Int = toInt(r)
  }

  implicit val longReader = new ObjectReader[Long] {
    def read(r: Any)(implicit jep: Jep): Long = toLong(r)
  }

  implicit val doubleReader = new ObjectReader[Double] {
    def read(r: Any)(implicit jep: Jep): Double = toDouble(r)
  }

  implicit val floatReader = new ObjectReader[Float] {
    def read(r: Any)(implicit jep: Jep): Float = toFloat(r)
  }

  implicit val booleanReader = new ObjectReader[Boolean] {
    def read(r: Any)(implicit jep: Jep): Boolean = {
      val obj = r
      val value = obj

      value match {
        case b: Boolean =>
          b
        case s: String =>
          s == "True"
        case i: Int if i == 0 || i == 1 =>
          i == 1
        case _ =>
          throw new IllegalArgumentException(s"Unknown boolean type for value $value")
      }
    }
  }

  implicit val stringReader = new ObjectReader[String] {
    def read(r: Any)(implicit jep: Jep): String = r.asInstanceOf[String]
  }

  implicit def seqReader[T](implicit reader: ObjectReader[T]): ObjectReader[Seq[T]] = new ObjectReader[Seq[T]] {
    def read(r: Any)(implicit jep: Jep) = {
      r match {
        case arr: Array[_] =>
          arr.map(reader.read).toSeq
        case arrList: java.util.ArrayList[Any] =>
          arrList.toArray.map(reader.read).toSeq
      }
//      null
    }//new NativeSeq[T](r)(reader, jep)
  }

  implicit def mapReader[I, O](implicit readerI: ObjectReader[I], readerO: ObjectReader[O]): ObjectReader[Map[I, O]] = new ObjectReader[Map[I, O]] {
    override def read(r: Any)(implicit jep: Jep): Map[I, O] = {
      r.asInstanceOf[java.util.Map[_, _]].asScala.map { case (k, v) =>
        readerI.read(k) -> readerO.read(v)
      }.toMap
    }
  }
}
