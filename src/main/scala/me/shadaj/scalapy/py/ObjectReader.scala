package me.shadaj.scalapy.py

import jep.Jep

import scala.reflect.ClassTag

trait ObjectReader[T] {
  def read(r: Ref)(implicit jep: Jep): T
}

object ObjectReader extends ObjectTupleReaders {
  implicit def refReader = new ObjectReader[Ref] {
    def read(r: Ref)(implicit jep: Jep): Ref = r
  }

  implicit def refDynReader = new ObjectReader[DynamicRef] {
    def read(r: Ref)(implicit jep: Jep): DynamicRef = Ref(r.expr).asInstanceOf[DynamicRef]
  }

  implicit def wrapperReader = new ObjectReader[Object] {
    def read(r: Ref)(implicit jep: Jep): Object = r.toObject
  }

  implicit def fascadeReader[T <: ObjectFascade](implicit classTag: ClassTag[T]): ObjectReader[T] = new ObjectReader[T] {
    def read(r: Ref)(implicit jep: Jep): T = {
      classTag.runtimeClass.getConstructor(classOf[Object], classOf[Jep]).newInstance(r.toObject, jep).asInstanceOf[T]
    }
  }

  implicit def wrapperDynReader = new ObjectReader[DynamicObject] {
    def read(r: Ref)(implicit jep: Jep): DynamicObject = r.toObject.asInstanceOf[DynamicObject]
  }

  def toInt(value: Any): Int = {
    value match {
      case i: Int => i
      case _: Double =>
        throw new IllegalArgumentException("Cannot up-convert a Double to an Int")
      case _: Float =>
        throw new IllegalArgumentException("Cannot up-convert a Float to an Int")
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass}")
    }
  }

  def toDouble(value: Any): Double = {
    value match {
      case i: Int => i
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
      case _: Double =>
        throw new IllegalArgumentException("Cannot up-convert a Double to a Float")
      case fl: Float => fl
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass}")
    }
  }

  implicit def intReader = new ObjectReader[Int] {
    def read(r: Ref)(implicit jep: Jep): Int = toInt(r.toObject.value)
  }

  implicit def doubleReader = new ObjectReader[Double] {
    def read(r: Ref)(implicit jep: Jep): Double = toDouble(r.toObject.value)
  }

  implicit def floatReader = new ObjectReader[Float] {
    def read(r: Ref)(implicit jep: Jep): Float = toFloat(r.toObject.value)
  }

  implicit def booleanReader = new ObjectReader[Boolean] {
    def read(r: Ref)(implicit jep: Jep): Boolean = {
      val obj = r.toObject
      val value = obj.value

      value match {
        case b: Boolean =>
          b
        case s: String =>
          s == "True"
        case _: Int if obj.as[Int] == 0 || obj.as[Int] == 1 =>
          obj.as[Int] == 1
        case _ =>
          throw new IllegalArgumentException(s"Unknown boolean type for value $value")
      }
    }
  }

  implicit def stringReader = new ObjectReader[String] {
    def read(r: Ref)(implicit jep: Jep): String = r.toObject.value.toString
  }

  implicit def seqReader[T](implicit reader: ObjectReader[T]): ObjectReader[Seq[T]] = new ObjectReader[Seq[T]] {
    def read(r: Ref)(implicit jep: Jep) = new NativeSeq[T](r)(reader, jep)
  }
}
