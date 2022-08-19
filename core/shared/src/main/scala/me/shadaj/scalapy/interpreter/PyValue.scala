package me.shadaj.scalapy.interpreter

import me.shadaj.scalapy.util.Compat

import scala.collection.mutable
import scala.collection.mutable.Stack
import scala.collection.mutable.Queue

final class PyValue private[PyValue](var underlying: Platform.Pointer, safeGlobal: Boolean = false) {
  val myAllocatedValues = PyValue.allocatedValues.get
  if (Platform.isNative && myAllocatedValues.isEmpty && !safeGlobal && !PyValue.disabledAllocationWarning) {
    System.err.println(s"Warning: the value ${this.getStringified} was allocated into a global space, which means it will not be garbage collected in Scala Native")
  }

  if (!safeGlobal && myAllocatedValues.nonEmpty) {
    myAllocatedValues.head.enqueue(this)
  }

  override def equals(obj: scala.Any): Boolean = {
    obj.isInstanceOf[PyValue] && underlying == obj.asInstanceOf[PyValue].underlying
  }

  override def hashCode(): Int = underlying.hashCode()

  def getStringified: String = CPythonInterpreter.withGil {
    val pyStr = CPythonAPI.PyObject_Str(underlying)
    CPythonInterpreter.throwErrorIfOccured()

    val cStr = CPythonAPI.PyUnicode_AsUTF8(pyStr)
    CPythonInterpreter.throwErrorIfOccured()

    val intoScala = Platform.fromCString(cStr, java.nio.charset.Charset.forName("UTF-8"))

    CPythonAPI.Py_DecRef(pyStr)

    intoScala
  }

  def getLong: Long = CPythonInterpreter.withGil {
    val ret = CPythonAPI.PyLong_AsLongLong(underlying)
    CPythonInterpreter.throwErrorIfOccured()
    ret
  }

  def getTuple: Seq[PyValue] = new Seq[PyValue] {
    def length: Int = CPythonInterpreter.withGil {
      val ret = Platform.cSizeToLong(CPythonAPI.PyTuple_Size(underlying)).toInt
      CPythonInterpreter.throwErrorIfOccured()
      ret
    }

    def apply(idx: Int): PyValue = CPythonInterpreter.withGil {
      val ret = CPythonAPI.PyTuple_GetItem(underlying, Platform.intToCSize(idx))
      CPythonInterpreter.throwErrorIfOccured()
      PyValue.fromBorrowed(ret)
    }

    def iterator: Iterator[PyValue] = (0 until length).toIterator.map(apply)
  }

  def getSeq[T](read: Platform.Pointer => T, write: T => Platform.Pointer): mutable.Seq[T] = new mutable.Seq[T] {
    override def length: Int = CPythonInterpreter.withGil {
      val ret = Platform.cSizeToLong(CPythonAPI.PySequence_Length(underlying)).toInt
      CPythonInterpreter.throwErrorIfOccured()
      ret
    }

    override def apply(idx: Int): T = CPythonInterpreter.withGil {
      val ret = CPythonAPI.PySequence_GetItem(underlying, idx)
      CPythonInterpreter.throwErrorIfOccured()
      try {
        read(ret)
      } finally {
        CPythonAPI.Py_DecRef(ret)
      }
    }

    override def update(idx: Int, elem: T): Unit = CPythonInterpreter.withGil {
      val written = write(elem)
      try {
        CPythonAPI.PySequence_SetItem(underlying, idx, written)
        CPythonInterpreter.throwErrorIfOccured()
      } finally {
        CPythonAPI.Py_DecRef(written)
      }
    }

    def iterator: Iterator[T] = (0 until length).toIterator.map(apply)
  }

  import scala.collection.mutable
  def getMap: mutable.Map[PyValue, PyValue] = new Compat.MutableMap[PyValue, PyValue] {
    def get(key: PyValue): Option[PyValue] = CPythonInterpreter.withGil {
      val contains = CPythonAPI.PyDict_Contains(
        underlying,
        key.underlying
      ) == 1

      CPythonInterpreter.throwErrorIfOccured()

      if (contains) {
        val value = CPythonAPI.PyDict_GetItem(underlying, key.underlying)
        CPythonInterpreter.throwErrorIfOccured()
        Some(PyValue.fromBorrowed(value))
      } else Option.empty[PyValue]
    }

    def iterator: Iterator[(PyValue, PyValue)] = CPythonInterpreter.withGil {
      val keysList = PyValue.fromNew(CPythonAPI.PyDict_Keys(underlying))
      CPythonInterpreter.throwErrorIfOccured()
      keysList.getSeq(PyValue.fromBorrowed(_), null).toIterator.map { k =>
        (k, get(k).get)
      }
    }

    override def addOne(kv: (PyValue, PyValue)): this.type = ???
    override def subtractOne(k: PyValue): this.type = ???
  }

  def cleanup(): Unit = CPythonInterpreter.withGil {
    if (underlying != null) {
      CPythonAPI.Py_DecRef(underlying)
      underlying = null
    } else {
      throw new IllegalStateException("This PyValue has already been cleaned up")
    }
  }

  private[scalapy] def dup(): PyValue = {
    if (underlying != null) {
      PyValue.fromBorrowed(underlying)
    } else {
      throw new IllegalStateException("Cannot dup a PyValue that has been cleaned")
    }
  }

  override def finalize(): Unit = CPythonInterpreter.withGil {
    if (underlying != null) {
      CPythonAPI.Py_DecRef(underlying)
      underlying = null
    }
  }
}

object PyValue {
  import scala.collection.mutable
  private[scalapy] val allocatedValues: Platform.ThreadLocal[Stack[Queue[PyValue]]] = Platform.threadLocalWithInitial(
    () => Stack.empty[Queue[PyValue]]
  )
  private[scalapy] var disabledAllocationWarning = false

  def fromNew(underlying: Platform.Pointer, safeGlobal: Boolean = false): PyValue = {
    new PyValue(underlying, safeGlobal)
  }

  def fromBorrowed(underlying: Platform.Pointer, safeGlobal: Boolean = false): PyValue = {
    CPythonInterpreter.withGil(CPythonAPI.Py_IncRef(underlying))
    new PyValue(underlying, safeGlobal)
  }

  def disableAllocationWarning(): Unit = {
    disabledAllocationWarning = true
  }
}
