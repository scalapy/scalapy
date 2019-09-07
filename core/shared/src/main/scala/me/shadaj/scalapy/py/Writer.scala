package me.shadaj.scalapy.py

import scala.collection.mutable
import scala.reflect.ClassTag

abstract class Writer[T] {
  def write(v: T): PyValue
}

object Writer extends TupleWriters {
  implicit def anyWriter[T <: Any]: Writer[T] = new Writer[T] {
    override def write(v: T): PyValue = v.value
  }

  implicit val noneWriter: Writer[None.type] = new Writer[None.type] {
    override def write(v: None.type): PyValue = interpreter.noneValue
  }

  implicit def unionWriter[A, B](implicit aClass: ClassTag[A], bClass: ClassTag[B], aWriter: Writer[A], bWriter: Writer[B]): Writer[A | B] = new Writer[A | B] {
    override def write(v: A | B): PyValue = {
      aClass.unapply(v.value) match {
        case Some(a) => aWriter.write(a)
        case _ => bWriter.write(v.value.asInstanceOf[B])
      }
    }
  }

  implicit val byteWriter: Writer[Byte] = new Writer[Byte] {
    override def write(v: Byte): PyValue = interpreter.valueFromLong(v)
  }

  implicit val intWriter: Writer[Int] = new Writer[Int] {
    override def write(v: Int): PyValue = interpreter.valueFromLong(v)
  }

  implicit val longWriter: Writer[Long] = new Writer[Long] {
    override def write(v: Long): PyValue = interpreter.valueFromLong(v)
  }

  implicit val doubleWriter: Writer[Double] = new Writer[Double] {
    override def write(v: Double): PyValue = interpreter.valueFromDouble(v)
  }

  implicit val floatWriter: Writer[Float] = new Writer[Float] {
    override def write(v: Float): PyValue = interpreter.valueFromDouble(v)
  }

  implicit val booleanWriter: Writer[Boolean] = new Writer[Boolean] {
    override def write(v: Boolean): PyValue = interpreter.valueFromBoolean(v)
  }

  implicit val stringWriter: Writer[String] = new Writer[String] {
    override def write(v: String): PyValue = interpreter.valueFromString(v)
  }

  implicit def seqWriter[T, C](implicit ev1: C => Seq[T], tWriter: Writer[T]): Writer[C] = new Writer[C] {
    override def write(v: C): PyValue = {
      interpreter.createList(v.view.map(tWriter.write))
    }
  }

  implicit def mapWriter[I, O](implicit iWriter: Writer[I], oWriter: Writer[O]) = new Writer[Map[I, O]] {
    override def write(map: Map[I, O]): PyValue = {
      val toAddLater = mutable.Queue.empty[(Any, Any)]

      map.foreach { case (i, o) =>
        toAddLater.enqueue((Any.populateWith(iWriter.write(i)), Any.populateWith(oWriter.write(o))))
      }

      val obj = py"{}"
      toAddLater.foreach { case (ko, vo) =>
        interpreter.eval(s"${obj.expr}[${ko.expr}] = ${vo.expr}")
      }

      obj.value
    }
  }
}
