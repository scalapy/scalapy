package me.shadaj.scalapy.readwrite

import scala.reflect.ClassTag
import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.FacadeCreator

import scala.collection.mutable
import scala.collection.compat._
import me.shadaj.scalapy.interpreter.Platform
import me.shadaj.scalapy.interpreter.CPythonInterpreter
import me.shadaj.scalapy.interpreter.CPythonAPI

trait Reader[T] {
  // no guarantees
  def read(r: PyValue): T = {
    CPythonInterpreter.withGil(readNative(r.underlying))
  }

  // borrowed, no references should be leaded without a ref count bump
  // assumes that GIL is held
  def readNative(r: Platform.Pointer): T = {
    read(PyValue.fromBorrowed(r))
  }
}

object Reader extends TupleReaders with FunctionReaders {
  implicit val anyReader: Reader[py.Any] = new Reader[py.Any] {
    override def readNative(r: Platform.Pointer): py.Any = py.Any.populateWith(PyValue.fromBorrowed(r))
  }

  implicit def facadeReader[F <: py.Any](implicit creator: FacadeCreator[F]): Reader[F] = new Reader[F] {
    override def readNative(r: Platform.Pointer): F = creator.create(PyValue.fromBorrowed(r))
  }

  implicit val unitReader: Reader[Unit] = new Reader[Unit] {
    override def readNative(r: Platform.Pointer): Unit = ()
  }

  implicit val byteReader: Reader[Byte] = new Reader[Byte] {
    private val min: Long = Byte.MinValue.toLong
    private val max: Long = Byte.MaxValue.toLong
    override def readNative(r: Platform.Pointer): Byte = {
      val res = CPythonAPI.PyLong_AsLongLong(r)
      if (res == -1) {
        CPythonInterpreter.throwErrorIfOccured()
      }

      if(res > max || res < min)
        throw new IllegalArgumentException("Cannot convert value outside of range to Byte")

      res.toByte
    }
  }

  implicit val intReader: Reader[Int] = new Reader[Int] {
    private val min: Long = Int.MinValue.toLong
    private val max: Long = Int.MaxValue.toLong
    override def readNative(r: Platform.Pointer): Int = {
      val res = CPythonAPI.PyLong_AsLongLong(r)
      if (res == -1) {
        CPythonInterpreter.throwErrorIfOccured()
      }

      if(res > max || res < min)
        throw new IllegalArgumentException("Cannot convert value outside of range to Int")

      res.toInt
    }
  }

  implicit val longReader: Reader[Long] = new Reader[Long] {
    override def readNative(r: Platform.Pointer): Long = {
      val res = CPythonAPI.PyLong_AsLongLong(r)
      if (res == -1) {
        CPythonInterpreter.throwErrorIfOccured()
      }

      res
    }
  }

  implicit val doubleReader: Reader[Double] = new Reader[Double] {
    override def readNative(r: Platform.Pointer): Double = {
      val res = CPythonAPI.PyFloat_AsDouble(r)
      if (res == -1.0) {
        CPythonInterpreter.throwErrorIfOccured()
      }

      res
    }
  }

  implicit val floatReader: Reader[Float] = new Reader[Float] {
    override def readNative(r: Platform.Pointer): Float = {
      val res = CPythonAPI.PyFloat_AsDouble(r)
      if (res == -1.0) {
        CPythonInterpreter.throwErrorIfOccured()
      }

      res.toFloat
    }
  }

  implicit val booleanReader: Reader[Boolean] = new Reader[Boolean] {
    override def readNative(r: Platform.Pointer): Boolean = {
      if (r == CPythonInterpreter.falseValue.underlying) false
      else if (r == CPythonInterpreter.trueValue.underlying) true
      else {
        throw new IllegalAccessException("Cannot convert a non-boolean value to a boolean")
      }
    }
  }

  implicit val stringReader: Reader[String] = new Reader[String] {
    override def readNative(r: Platform.Pointer): String = {
      val cStr = CPythonAPI.PyUnicode_AsUTF8(r)
      CPythonInterpreter.throwErrorIfOccured()
      Platform.fromCString(cStr, java.nio.charset.Charset.forName("UTF-8"))
    }
  }

  implicit val charReader: Reader[Char] = new Reader[Char] {
    override def readNative(r: Platform.Pointer): Char = {
      val rStr = stringReader.readNative(r)
      if (rStr.length != 1) {
        throw new IllegalArgumentException("Cannot extract a char from a string with length != 1")
      } else {
        rStr.head
      }
    }
  }

  implicit def mutableSeqReader[T](implicit reader: Reader[T], writer: Writer[T]): Reader[mutable.Seq[T]] = new Reader[mutable.Seq[T]] {
    override def read(r: PyValue) = r.dup().getSeq(reader.readNative, writer.writeNative)
  }

  implicit def seqReader[T, C[A] <: Iterable[A]](implicit reader: Reader[T], bf: Factory[T, C[T]]): Reader[C[T]] = new Reader[C[T]] {
    override def readNative(r: Platform.Pointer) = {
      val builder = bf.newBuilder

      val iterator = CPythonAPI.PyObject_GetIter(r)
      CPythonInterpreter.throwErrorIfOccured()

      try {
        var item = CPythonAPI.PyIter_Next(iterator)
        CPythonInterpreter.throwErrorIfOccured()
        while (item != null) {
          try {
            builder += reader.readNative(item)
          } finally {
            CPythonAPI.Py_DecRef(item)
          }
          item = CPythonAPI.PyIter_Next(iterator)
          CPythonInterpreter.throwErrorIfOccured()
        }
      } finally {
        CPythonAPI.Py_DecRef(iterator)
      }

      builder.result()
    }
  }

  implicit def mapReader[I, O](implicit readerI: Reader[I], readerO: Reader[O]): Reader[Map[I, O]] = new Reader[Map[I, O]] {
    val readerT: Reader[(I, O)] = tuple2Reader

    override def readNative(r: Platform.Pointer): Map[I, O] = {
      val builder = Map.newBuilder[I, O]

      val items = CPythonAPI.PyDict_Items(r)
      CPythonInterpreter.throwErrorIfOccured()

      try {
        val iterator = CPythonAPI.PyObject_GetIter(items)
        CPythonInterpreter.throwErrorIfOccured()

        try {
          var item = CPythonAPI.PyIter_Next(iterator)
          CPythonInterpreter.throwErrorIfOccured()

          while (item != null) {
            try {
              builder += readerT.readNative(item)
            } finally {
              CPythonAPI.Py_DecRef(item)
            }
            item = CPythonAPI.PyIter_Next(iterator)
            CPythonInterpreter.throwErrorIfOccured()
          }
        } finally {
          CPythonAPI.Py_DecRef(iterator)
        }
      } finally  {
        CPythonAPI.Py_DecRef(items)
      }

      builder.result()
    }
  }

  implicit val bytesReader: Reader[Array[Byte]] = new Reader[Array[Byte]]{
    override def readNative(r: Platform.Pointer): Array[Byte] =
      Platform.Zone{ implicit zone =>
        val outputPointerData = Platform.allocPointer[Platform.Pointer]
        val outputPointerSize = Platform.allocPointer[Long]
        CPythonAPI.PyBytes_AsStringAndSize(r, outputPointerData, outputPointerSize)
        CPythonInterpreter.throwErrorIfOccured()

        val size: Long = Platform.dereferenceAsLong(outputPointerSize)
        if(size > Int.MaxValue.toLong) throw new IllegalArgumentException("Only arrays up to size Integer.MaxValue can be read")

        Platform.copyBytes(outputPointerData, size.toInt)
      }
  }
}
