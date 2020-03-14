package me.shadaj.scalapy.py

object CPythonInterpreter {
  CPythonAPI.Py_Initialize()

  val globals: Platform.Pointer = CPythonAPI.PyDict_New()
  CPythonAPI.Py_IncRef(globals)

  val builtins = CPythonAPI.PyEval_GetBuiltins()
  Platform.Zone { implicit zone =>
    CPythonAPI.PyDict_SetItemString(globals, Platform.toCString("__builtins__"), builtins)
    throwErrorIfOccured()
  }

  val falseValue = PyValue.fromNew(CPythonAPI.PyBool_FromLong(Platform.intToCLong(0)), true)
  val trueValue = PyValue.fromNew(CPythonAPI.PyBool_FromLong(Platform.intToCLong(1)), true)

  val noneValue: PyValue = PyValue.fromNew(CPythonAPI.Py_BuildValue(Platform.emptyCString), true)

  def eval(code: String): Unit = {
    Platform.Zone { implicit zone =>
      val Py_single_input = 256
      CPythonAPI.PyRun_String(Platform.toCString(code), Py_single_input, globals, globals)
      throwErrorIfOccured()
    }
  }

  def set(variable: String, value: PyValue): Unit = {
    Platform.Zone { implicit zone =>
      CPythonAPI.PyDict_SetItemString(globals, Platform.toCString(variable), value.underlying)
      throwErrorIfOccured()
    }
  }

  private var counter = 0
  def getVariableReference(value: PyValue): VariableReference = {
    val variableName = "spy_o_" + counter
    counter += 1

    Platform.Zone { implicit zone =>
      CPythonAPI.PyDict_SetItemString(globals, Platform.toCString(variableName), value.underlying)
      throwErrorIfOccured()
    }

    new VariableReference(variableName)
  }

  def valueFromBoolean(b: Boolean): PyValue = if (b) trueValue else falseValue
  def valueFromLong(long: Long): PyValue = PyValue.fromNew(CPythonAPI.PyLong_FromLongLong(long))
  def valueFromDouble(v: Double): PyValue = PyValue.fromNew(CPythonAPI.PyFloat_FromDouble(v))
  def valueFromString(v: String): PyValue = PyValue.fromNew(toNewString(v))

  // Hack to patch around Scala Native not letting us auto-box pointers
  private class PointerBox(val ptr: Platform.Pointer)

  private def toNewString(v: String) = {
    (Platform.Zone { implicit zone =>
      new PointerBox(CPythonAPI.PyUnicode_FromString(
        Platform.toCString(v, java.nio.charset.Charset.forName("UTF-8"))
      ))
    }).ptr
  }

  def createList(seq: Seq[PyValue]): PyValue = {
    // TODO: this is copying, should be replaced by a custom type
    val retPtr = CPythonAPI.PyList_New(seq.size)
    seq.zipWithIndex.foreach { case (v, i) =>
      CPythonAPI.Py_IncRef(v.underlying) // SetItem steals reference
      CPythonAPI.PyList_SetItem(retPtr, Platform.intToCLong(i), v.underlying)
    }

    PyValue.fromNew(retPtr)
  }

  def createTuple(seq: Seq[PyValue]): PyValue = {
    // TODO: this is copying, should be replaced by a custom type
    val retPtr = CPythonAPI.PyTuple_New(seq.size)
    seq.zipWithIndex.foreach { case (v, i) =>
      CPythonAPI.Py_IncRef(v.underlying) // SetItem steals reference
      CPythonAPI.PyTuple_SetItem(retPtr, Platform.intToCLong(i), v.underlying)
    }

    PyValue.fromNew(retPtr)
  }

  private def pointerPointerToString(pointer: Platform.PointerToPointer) = {
    Platform.fromCString(CPythonAPI.PyUnicode_AsUTF8(
      CPythonAPI.PyObject_Str(
        Platform.dereferencePointerToPointer(pointer)
      )
    ), java.nio.charset.Charset.forName("UTF-8"))
  }

  def throwErrorIfOccured() = {
    if (Platform.pointerToLong(CPythonAPI.PyErr_Occurred()) != 0) {
      Platform.Zone { implicit zone =>
        val pType = Platform.allocPointerToPointer
        val pValue = Platform.allocPointerToPointer
        val pTraceback = Platform.allocPointerToPointer

        CPythonAPI.PyErr_Fetch(pType, pValue, pTraceback)

        val pTypeStringified = pointerPointerToString(pType)

        val pValueObject = Platform.dereferencePointerToPointer(pValue)
        val pValueStringified = if (pValueObject != null) {
          " " + pointerPointerToString(pValue)
        } else ""

        throw new PythonException(pTypeStringified + pValueStringified)
      }
    }
  }

  def load(code: String): PyValue = {
    Platform.Zone { implicit zone =>
      val Py_eval_input = 258
      val result = CPythonAPI.PyRun_String(Platform.toCString(code), Py_eval_input, globals, globals)
      throwErrorIfOccured()

      PyValue.fromNew(result)
    }
  }

  def unaryNeg(a: PyValue): PyValue = {
    val ret = CPythonAPI.PyNumber_Negative(
      a.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def unaryPos(a: PyValue): PyValue = {
    val ret = CPythonAPI.PyNumber_Positive(
      a.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def binaryAdd(a: PyValue, b: PyValue): PyValue = {
    val ret = CPythonAPI.PyNumber_Add(
      a.underlying,
      b.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def binarySub(a: PyValue, b: PyValue): PyValue = {
    val ret = CPythonAPI.PyNumber_Subtract(
      a.underlying,
      b.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def binaryMul(a: PyValue, b: PyValue): PyValue = {
    val ret = CPythonAPI.PyNumber_Multiply(
      a.underlying,
      b.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def binaryDiv(a: PyValue, b: PyValue): PyValue = {
    val ret = CPythonAPI.PyNumber_TrueDivide(
      a.underlying,
      b.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def binaryMod(a: PyValue, b: PyValue): PyValue = {
    val ret = CPythonAPI.PyNumber_Remainder(
      a.underlying,
      b.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  private def runCallableAndDecref(callable: Platform.Pointer, args: Seq[PyValue]): PyValue = {
    val result = CPythonAPI.PyObject_Call(
      callable,
      createTuple(args).underlying,
      null
    )

    CPythonAPI.Py_DecRef(callable)

    throwErrorIfOccured()

    PyValue.fromNew(result)
  }

  def callGlobal(method: String, args: Seq[PyValue]): PyValue = {
    Platform.Zone { implicit zone =>
      val methodString = toNewString(method)
      var callable = CPythonAPI.PyDict_GetItemWithError(globals, methodString)
      if (callable == null) {
        CPythonAPI.PyErr_Clear()
        callable = CPythonAPI.PyDict_GetItemWithError(builtins, methodString)
      }

      CPythonAPI.Py_DecRef(methodString)

      throwErrorIfOccured()

      runCallableAndDecref(callable, args)
    }
  }

  def call(on: PyValue, method: String, args: Seq[PyValue]): PyValue = {
    Platform.Zone { implicit zone =>
      val callable = CPythonAPI.PyObject_GetAttrString(on.underlying, Platform.toCString(method))
      throwErrorIfOccured()

      runCallableAndDecref(callable, args)
    }
  }

  def selectGlobal(name: String): PyValue = {
    Platform.Zone { implicit zone =>
      val nameString = toNewString(name)
      var gottenValue = CPythonAPI.PyDict_GetItemWithError(globals, nameString)
      if (gottenValue == null) {
        CPythonAPI.PyErr_Clear()
        gottenValue = CPythonAPI.PyDict_GetItemWithError(builtins, nameString)
      }

      CPythonAPI.Py_DecRef(nameString)

      throwErrorIfOccured()

      PyValue.fromNew(gottenValue)
    }
  }

  def select(on: PyValue, value: String): PyValue = {
    val valueString = toNewString(value)
    val underlying = CPythonAPI.PyObject_GetAttr(
      on.underlying,
      valueString
    )

    CPythonAPI.Py_DecRef(valueString)

    throwErrorIfOccured()

    PyValue.fromNew(underlying)
  }

  def update(on: PyValue, value: String, newValue: PyValue): Unit = {
    val valueString = toNewString(value)

    CPythonAPI.PyObject_SetAttr(
      on.underlying,
      valueString,
      newValue.underlying
    )

    CPythonAPI.Py_DecRef(valueString)
  }

  def selectList(on: PyValue, index: Int): PyValue = {
    val ret = CPythonAPI.PyList_GetItem(
      on.underlying,
      Platform.intToCLong(index)
    )

    throwErrorIfOccured()

    PyValue.fromBorrowed(ret)
  }

  def selectDictionary(on: PyValue, key: PyValue): PyValue = {
    val ret = CPythonAPI.PyDict_GetItemWithError(
      on.underlying,
      key.underlying
    )

    throwErrorIfOccured()

    PyValue.fromBorrowed(ret)
  }
}

final class PyValue private[PyValue](val underlying: Platform.Pointer, safeGlobal: Boolean = false) {
  if (Platform.isNative && PyValue.allocatedValues.isEmpty && !safeGlobal) {
    println(s"Warning: the value ${this.getStringified} was allocated into a global space, which means it will not be garbage collected in Scala Native")
  }

  if (PyValue.allocatedValues.nonEmpty) {
    PyValue.allocatedValues = (this :: PyValue.allocatedValues.head) :: PyValue.allocatedValues.tail
  }

  def getStringified: String = {
    val pyStr = CPythonAPI.PyObject_Str(underlying)
    CPythonInterpreter.throwErrorIfOccured()

    val cStr = CPythonAPI.PyUnicode_AsUTF8(pyStr)
    CPythonAPI.Py_DecRef(pyStr)
    CPythonInterpreter.throwErrorIfOccured()

    Platform.fromCString(cStr, java.nio.charset.Charset.forName("UTF-8"))
  }

  def getString: String = {
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

  def getLong: Long = {
    val ret = CPythonAPI.PyLong_AsLongLong(underlying)
    CPythonInterpreter.throwErrorIfOccured()
    ret
  }

  def getDouble: Double = {
    val ret = CPythonAPI.PyFloat_AsDouble(underlying)
    CPythonInterpreter.throwErrorIfOccured()
    ret
  }

  def getTuple: Seq[PyValue] = new Seq[PyValue] {
    def length: Int = {
      val ret = Platform.cLongToLong(CPythonAPI.PyTuple_Size(underlying)).toInt
      CPythonInterpreter.throwErrorIfOccured()
      ret
    }

    def apply(idx: Int): PyValue = new PyValue({
      val ret = CPythonAPI.PyTuple_GetItem(underlying, Platform.intToCLong(idx))
      CPythonInterpreter.throwErrorIfOccured()
      ret
    })

    def iterator: Iterator[PyValue] = (0 until length).toIterator.map(apply)
  }

  def getSeq: Seq[PyValue] = new Seq[PyValue] {
    def length: Int = {
      val ret = Platform.cLongToLong(CPythonAPI.PyObject_Length(underlying)).toInt
      CPythonInterpreter.throwErrorIfOccured()
      ret
    }

    def apply(idx: Int): PyValue = {
      val indexValue = CPythonAPI.PyLong_FromLongLong(idx)
      val ret = CPythonAPI.PyObject_GetItem(underlying, indexValue)
      CPythonAPI.Py_DecRef(indexValue)

      PyValue.fromBorrowed(ret)
    }

    def iterator: Iterator[PyValue] = (0 until length).toIterator.map(apply)
  }

  import scala.collection.mutable
  def getMap: mutable.Map[PyValue, PyValue] = new Compat.MutableMap[PyValue, PyValue] {
    def get(key: PyValue): Option[PyValue] = {
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

    def iterator: Iterator[(PyValue, PyValue)] = {
      val keysList = new PyValue(CPythonAPI.PyDict_Keys(underlying))
      CPythonInterpreter.throwErrorIfOccured()
      keysList.getSeq.toIterator.map { k =>
        (k, get(k).get)
      }
    }

    override def addOne(kv: (PyValue, PyValue)): this.type = ???
    override def subtractOne(k: PyValue): this.type = ???
  }

  private var cleaned = false

  def cleanup(): Unit = {
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

  def fromNew(underlying: Platform.Pointer, safeGlobal: Boolean = false): PyValue = {
    new PyValue(underlying, safeGlobal)
  }

  def fromBorrowed(underlying: Platform.Pointer): PyValue = {
    CPythonAPI.Py_IncRef(underlying)
    new PyValue(underlying)
  }
}
