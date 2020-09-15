package me.shadaj.scalapy.py.interpreter

import me.shadaj.scalapy.py.Compat

final class PyValue private[PyValue](val underlying: Platform.Pointer, safeGlobal: Boolean = false) {
  if (Platform.isNative && PyValue.allocatedValues.isEmpty && !safeGlobal && !PyValue.disabledAllocationWarning) {
    println(s"Warning: the value ${this.getStringified} was allocated into a global space, which means it will not be garbage collected in Scala Native")
  }

  if (PyValue.allocatedValues.nonEmpty) {
    PyValue.allocatedValues = (this :: PyValue.allocatedValues.head) :: PyValue.allocatedValues.tail
  }

  def getStringified: String = CPythonInterpreter.withGil {
    val pyStr = CPythonAPI.PyObject_Str(underlying)
    CPythonInterpreter.throwErrorIfOccured()

    val cStr = CPythonAPI.PyUnicode_AsUTF8(pyStr)
    CPythonInterpreter.throwErrorIfOccured()

    val intoScala = Platform.fromCString(cStr, java.nio.charset.Charset.forName("UTF-8"))

    CPythonAPI.Py_DecRef(pyStr)

    intoScala
  }

  def getString: String = CPythonInterpreter.withGil {
    val cStr = CPythonAPI.PyUnicode_AsUTF8(underlying)
    CPythonInterpreter.throwErrorIfOccured()
    Platform.fromCString(cStr, java.nio.charset.Charset.forName("UTF-8"))
  }

  def getBoolean: Boolean = {
    if (underlying == CPythonInterpreter.falseValue.underlying) false
    else if (underlying == CPythonInterpreter.trueValue.underlying) true
    else {
      throw new IllegalAccessException("Cannot convert a non-boolean value to a boolean")
    }
  }

  def getLong: Long = CPythonInterpreter.withGil {
    val ret = CPythonAPI.PyLong_AsLongLong(underlying)
    CPythonInterpreter.throwErrorIfOccured()
    ret
  }

  def getDouble: Double = CPythonInterpreter.withGil {
    val ret = CPythonAPI.PyFloat_AsDouble(underlying)
    CPythonInterpreter.throwErrorIfOccured()
    ret
  }

  def getTuple: Seq[PyValue] = new Seq[PyValue] {
    def length: Int = CPythonInterpreter.withGil {
      val ret = Platform.cLongToLong(CPythonAPI.PyTuple_Size(underlying)).toInt
      CPythonInterpreter.throwErrorIfOccured()
      ret
    }

    def apply(idx: Int): PyValue = CPythonInterpreter.withGil {
      val ret = CPythonAPI.PyTuple_GetItem(underlying, Platform.intToCLong(idx))
      CPythonInterpreter.throwErrorIfOccured()
      PyValue.fromBorrowed(ret)
    }

    def iterator: Iterator[PyValue] = (0 until length).toIterator.map(apply)
  }

  def getSeq: Seq[PyValue] = new Seq[PyValue] {
    def length: Int = CPythonInterpreter.withGil {
      val ret = Platform.cLongToLong(CPythonAPI.PySequence_Length(underlying)).toInt
      CPythonInterpreter.throwErrorIfOccured()
      ret
    }

    def apply(idx: Int): PyValue = CPythonInterpreter.withGil {
      val ret = CPythonAPI.PySequence_GetItem(underlying, idx)
      CPythonInterpreter.throwErrorIfOccured()
      PyValue.fromNew(ret)
    }

    def iterator: Iterator[PyValue] = (0 until length).toIterator.map(apply)
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
      val keysList = new PyValue(CPythonAPI.PyDict_Keys(underlying))
      CPythonInterpreter.throwErrorIfOccured()
      keysList.getSeq.toIterator.map { k =>
        (k, get(k).get)
      }
    }

    override def addOne(kv: (PyValue, PyValue)): this.type = ???
    override def subtractOne(k: PyValue): this.type = ???
  }

  private[py] var cleaned = false

  def cleanup(): Unit = CPythonInterpreter.withGil {
    if (!cleaned) {
      cleaned = true
      CPythonAPI.Py_DecRef(underlying)
    }
  }

  override def finalize(): Unit = cleanup()
}

object PyValue {
  import scala.collection.mutable
  private[py] var allocatedValues: List[List[PyValue]] = List.empty
  private[py] var disabledAllocationWarning = false

  def fromNew(underlying: Platform.Pointer, safeGlobal: Boolean = false): PyValue = {
    new PyValue(underlying, safeGlobal)
  }

  def fromBorrowed(underlying: Platform.Pointer): PyValue = {
    CPythonInterpreter.withGil(CPythonAPI.Py_IncRef(underlying))
    new PyValue(underlying)
  }

  def disableAllocationWarning(): Unit = {
    disabledAllocationWarning = true
  }
}
