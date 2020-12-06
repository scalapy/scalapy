package me.shadaj.scalapy.readwrite

import scala.collection.mutable
import scala.reflect.ClassTag

import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue}
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.|
import me.shadaj.scalapy.py.PyQuote

abstract class Writer[T] {
  def write(v: T): PyValue
}

object Writer extends TupleWriters with FunctionWriters {
  implicit def anyWriter[T <: py.Any]: Writer[T] = new Writer[T] {
    override def write(v: T): PyValue = v.value
  }

  implicit def unionWriter[A, B](implicit aClass: ClassTag[A], bClass: ClassTag[B], aWriter: Writer[A], bWriter: Writer[B]): Writer[A | B] = new Writer[A | B] {
    override def write(v: A | B): PyValue = {
      aClass.unapply(v.value) match {
        case Some(a) => aWriter.write(a)
        case _ => bWriter.write(v.value.asInstanceOf[B])
      }
    }
  }

  implicit val unitWriter: Writer[Unit] = new Writer[Unit] {
    override def write(v: Unit): PyValue = CPythonInterpreter.noneValue
  }

  implicit val byteWriter: Writer[Byte] = new Writer[Byte] {
    override def write(v: Byte): PyValue = CPythonInterpreter.valueFromLong(v)
  }

  implicit val intWriter: Writer[Int] = new Writer[Int] {
    override def write(v: Int): PyValue = CPythonInterpreter.valueFromLong(v)
  }

  implicit val longWriter: Writer[Long] = new Writer[Long] {
    override def write(v: Long): PyValue = CPythonInterpreter.valueFromLong(v)
  }

  implicit val doubleWriter: Writer[Double] = new Writer[Double] {
    override def write(v: Double): PyValue = CPythonInterpreter.valueFromDouble(v)
  }

  implicit val floatWriter: Writer[Float] = new Writer[Float] {
    override def write(v: Float): PyValue = CPythonInterpreter.valueFromDouble(v)
  }

  implicit val booleanWriter: Writer[Boolean] = new Writer[Boolean] {
    override def write(v: Boolean): PyValue = CPythonInterpreter.valueFromBoolean(v)
  }

  implicit val stringWriter: Writer[String] = new Writer[String] {
    override def write(v: String): PyValue = CPythonInterpreter.valueFromString(v)
  }

  implicit def mapWriter[K, V](implicit kWriter: Writer[K], vWriter: Writer[V]) = new Writer[Map[K, V]] {
    override def write(map: Map[K, V]): PyValue = {
      val obj = CPythonInterpreter.newDictionary()
      map.foreach { case (k, v) =>
        CPythonInterpreter.updateBracket(obj, py.Any.from(k).value, py.Any.from(v).value)
      }

      obj
    }
  }
}
