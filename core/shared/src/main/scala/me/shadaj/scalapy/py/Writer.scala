package me.shadaj.scalapy.py

import scala.collection.mutable
import scala.reflect.ClassTag

abstract class Writer[T] {
  def write(v: T): Either[PyValue, Any]
}

object Writer extends TupleWriters {
  implicit def anyWriter[T <: Any]: Writer[T] = new Writer[T] {
    override def write(v: T): Either[PyValue, T] = {
      Right(v)
    }
  }

  implicit val noneWriter: Writer[None.type] = new Writer[None.type] {
    override def write(v: None.type): Either[PyValue, Any] = Left(interpreter.noneValue)
  }

  implicit def unionWriter[A, B](implicit aClass: ClassTag[A], bClass: ClassTag[B], aWriter: Writer[A], bWriter: Writer[B]): Writer[A | B] = new Writer[A | B] {
    override def write(v: A | B): Either[PyValue, Any] = {
      aClass.unapply(v.value) match {
        case Some(a) => aWriter.write(a)
        case _ => bWriter.write(v.value.asInstanceOf[B])
      }
    }
  }

  implicit val byteWriter: Writer[Byte] = new Writer[Byte] {
    override def write(v: Byte): Either[PyValue, Any] = {
      Left(interpreter.valueFromLong(v))
    }
  }

  implicit val intWriter: Writer[Int] = new Writer[Int] {
    override def write(v: Int): Either[PyValue, Any] = {
      Left(interpreter.valueFromLong(v))
    }
  }

  implicit val longWriter: Writer[Long] = new Writer[Long] {
    override def write(v: Long): Either[PyValue, Any] = {
      Left(interpreter.valueFromLong(v))
    }
  }

  implicit val doubleWriter: Writer[Double] = new Writer[Double] {
    override def write(v: Double): Either[PyValue, Any] = {
      Left(interpreter.valueFromDouble(v))
    }
  }

  implicit val floatWriter: Writer[Float] = new Writer[Float] {
    override def write(v: Float): Either[PyValue, Any] = {
      Left(interpreter.valueFromDouble(v))
    }
  }

  implicit val booleanWriter: Writer[Boolean] = new Writer[Boolean] {
    override def write(v: Boolean): Either[PyValue, Any] = {
      Left(interpreter.valueFromBoolean(v))
    }
  }

  implicit val stringWriter: Writer[String] = new Writer[String] {
    override def write(v: String): Either[PyValue, Any] = {
      Left(interpreter.valueFromString(v))
    }
  }

  private val supportedObjectTypes = Set[Class[_]](classOf[String])
  implicit def seqWriter[T: ClassTag, C](implicit ev1: C => Seq[T], tWriter: Writer[T]): Writer[C] = new Writer[C] {
    override def write(v: C): Either[PyValue, Any] = {
      Left(interpreter.createList(v.map { e =>
        tWriter.write(e).right.map(_.value).merge
      }))
    }
  }

  implicit def mapWriter[I, O](implicit iWriter: Writer[I], oWriter: Writer[O]) = new Writer[Map[I, O]] {
    override def write(map: Map[I, O]): Either[PyValue, Any] = {
      val toAddLater = mutable.Queue.empty[(Any, Any)]

      map.foreach { case (i, o) =>
        (iWriter.write(i), oWriter.write(o)) match {
          case (Left(k), Left(v)) =>
            toAddLater.enqueue((Any.populateWith(k), Any.populateWith(v)))
          case (Left(k), Right(vo)) => toAddLater.enqueue((Any.populateWith(k), vo))
          case (Right(ko), Left(v)) => toAddLater.enqueue((ko, Any.populateWith(v)))
          case (Right(ko), Right(vo)) => toAddLater.enqueue((ko, vo))
        }
      }

      val obj = py"{}"
      toAddLater.foreach { case (ko, vo) =>
        interpreter.eval(s"${obj.expr}[${ko.expr}] = ${vo.expr}")
      }

      Right(obj)
    }
  }
}
