package me.shadaj.scalapy.py

class CPythonInterpreter extends Interpreter {
  CPythonAPI.Py_Initialize()

  val globals: Platform.Pointer = CPythonAPI.PyDict_New()
  val builtins = new CPyValue(CPythonAPI.PyEval_GetBuiltins(), true)
  set("__builtins__", builtins)

  val falsePtr: Platform.Pointer = CPythonAPI.PyBool_FromLong(Platform.intToCLong(0))
  val truePtr: Platform.Pointer = CPythonAPI.PyBool_FromLong(Platform.intToCLong(1))

  val noneValue: PyValue = new CPyValue(CPythonAPI.Py_BuildValue(Platform.emptyCString), true)
  
  override def eval(code: String): Unit = {
    Platform.Zone { implicit zone =>
      val Py_single_input = 256
      CPythonAPI.PyRun_String(Platform.toCString(code), Py_single_input, globals, globals)
      throwErrorIfOccured()
    }
  }

  override def set(variable: String, value: PyValue): Unit = {
    Platform.Zone { implicit zone =>
      CPythonAPI.PyDict_SetItemString(globals, Platform.toCString(variable), value.asInstanceOf[CPyValue].underlying)
      CPythonAPI.Py_IncRef(value.asInstanceOf[CPyValue].underlying)
      throwErrorIfOccured()
    }
  }

  private var counter = 0
  def getVariableReference(value: PyValue): VariableReference = {
    val variableName = "spy_o_" + counter
    counter += 1
    
    Platform.Zone { implicit zone =>
      CPythonAPI.PyDict_SetItemString(globals, Platform.toCString(variableName), value.asInstanceOf[CPyValue].underlying)
      CPythonAPI.Py_IncRef(value.asInstanceOf[CPyValue].underlying)
      throwErrorIfOccured()
    }

    new VariableReference(variableName)
  }
  
  def valueFromBoolean(b: Boolean): PyValue = new CPyValue(CPythonAPI.PyBool_FromLong(
    Platform.intToCLong(if (b) 1 else 0)
  ))
  def valueFromLong(long: Long): PyValue = new CPyValue(CPythonAPI.PyLong_FromLongLong(long))
  def valueFromDouble(v: Double): PyValue = new CPyValue(CPythonAPI.PyFloat_FromDouble(v))
  def valueFromString(v: String): PyValue = {
    var ret: PyValue = null
    Platform.Zone { implicit zone =>
      ret = new CPyValue(CPythonAPI.PyUnicode_FromString(
        Platform.toCString(v, java.nio.charset.Charset.forName("UTF-8"))
      ))
    }

    ret
  }

  def createList(seq: Seq[PyValue]): PyValue = {
    // TODO: this is copying, should be replaced by a custom type
    val retPtr = CPythonAPI.PyList_New(seq.size)
    seq.zipWithIndex.foreach { case (v, i) =>
      CPythonAPI.PyList_SetItem(retPtr, Platform.intToCLong(i), v.asInstanceOf[CPyValue].underlying)
      CPythonAPI.Py_IncRef(v.asInstanceOf[CPyValue].underlying)
    }

    new CPyValue(retPtr)
  }

  def createTuple(seq: Seq[PyValue]): PyValue = {
    // TODO: this is copying, should be replaced by a custom type
    val retPtr = CPythonAPI.PyTuple_New(seq.size)
    seq.zipWithIndex.foreach { case (v, i) =>
      CPythonAPI.PyTuple_SetItem(retPtr, Platform.intToCLong(i), v.asInstanceOf[CPyValue].underlying)
      CPythonAPI.Py_IncRef(v.asInstanceOf[CPyValue].underlying)
    }

    new CPyValue(retPtr)
  }

  def throwErrorIfOccured() = {
    if (Platform.pointerToLong(CPythonAPI.PyErr_Occurred()) != 0) {
      Platform.Zone { implicit zone =>
        val pType = Platform.allocPointerToPointer
        val pValue = Platform.allocPointerToPointer
        val pTraceback = Platform.allocPointerToPointer

        CPythonAPI.PyErr_Fetch(pType, pValue, pTraceback)

        val errorMessage = local {
          (new CPyValue(CPythonAPI.PyObject_Str(Platform.dereferencePointerToPointer(pType)))).getString + (if (Platform.dereferencePointerToPointer(pValue) != null) {
            " " + (new CPyValue(CPythonAPI.PyObject_Str(Platform.dereferencePointerToPointer(pValue)))).getString
          } else "")
        }

        throw new PythonException(errorMessage)
      }
    }
  }

  override def load(code: String): PyValue = {
    var ret: CPyValue = null
    Platform.Zone { implicit zone =>
      val Py_eval_input = 258
      val result = CPythonAPI.PyRun_String(Platform.toCString(code), Py_eval_input, globals, globals)
      throwErrorIfOccured()

      CPythonAPI.Py_IncRef(result)

      ret = new CPyValue(result)
    }

    ret
  }

  def unaryNeg(a: PyValue): PyValue = {
    new CPyValue({
      val ret = CPythonAPI.PyNumber_Negative(
        a.asInstanceOf[CPyValue].underlying
      )

      throwErrorIfOccured()

      ret
    })
  }

  def unaryPos(a: PyValue): PyValue = {
    new CPyValue({
      val ret = CPythonAPI.PyNumber_Positive(
        a.asInstanceOf[CPyValue].underlying
      )

      throwErrorIfOccured()

      ret
    })
  }

  def binaryAdd(a: PyValue, b: PyValue): PyValue = {
    new CPyValue({
      val ret = CPythonAPI.PyNumber_Add(
        a.asInstanceOf[CPyValue].underlying,
        b.asInstanceOf[CPyValue].underlying
      )

      throwErrorIfOccured()

      ret
    })
  }

  def binarySub(a: PyValue, b: PyValue): PyValue = {
    new CPyValue({
      val ret = CPythonAPI.PyNumber_Subtract(
        a.asInstanceOf[CPyValue].underlying,
        b.asInstanceOf[CPyValue].underlying
      )

      throwErrorIfOccured()

      ret
    })
  }

  def binaryMul(a: PyValue, b: PyValue): PyValue = {
    new CPyValue({
      val ret = CPythonAPI.PyNumber_Multiply(
        a.asInstanceOf[CPyValue].underlying,
        b.asInstanceOf[CPyValue].underlying
      )

      throwErrorIfOccured()

      ret
    })
  }

  def binaryDiv(a: PyValue, b: PyValue): PyValue = {
    new CPyValue({
      val ret = CPythonAPI.PyNumber_TrueDivide(
        a.asInstanceOf[CPyValue].underlying,
        b.asInstanceOf[CPyValue].underlying
      )

      throwErrorIfOccured()

      ret
    })
  }

  def binaryMod(a: PyValue, b: PyValue): PyValue = {
    new CPyValue({
      val ret = CPythonAPI.PyNumber_Remainder(
        a.asInstanceOf[CPyValue].underlying,
        b.asInstanceOf[CPyValue].underlying
      )

      throwErrorIfOccured()

      ret
    })
  }

  def callGlobal(method: String, args: PyValue*): PyValue = {
    var callable: CPyValue = local {
      var gottenAttr = CPythonAPI.PyDict_GetItemWithError(globals, valueFromString(method).asInstanceOf[CPyValue].underlying)
      if (gottenAttr == null) {
        CPythonAPI.PyErr_Clear()
        gottenAttr = CPythonAPI.PyDict_GetItemWithError(builtins.underlying, valueFromString(method).asInstanceOf[CPyValue].underlying)
      }

      throwErrorIfOccured()
      new CPyValue(gottenAttr)
    }

    val result = CPythonAPI.PyObject_Call(
      callable.underlying,
      createTuple(args).asInstanceOf[CPyValue].underlying,
      null
    )

    throwErrorIfOccured()

    new CPyValue(result)
  }

  def call(on: PyValue, method: String, args: Seq[PyValue]): PyValue = {
    var callable: CPyValue = Platform.Zone { implicit zone =>
      val gottenAttr = CPythonAPI.PyObject_GetAttrString(on.asInstanceOf[CPyValue].underlying, Platform.toCString(method))
      throwErrorIfOccured()
      new CPyValue(gottenAttr)
    }

    val result = CPythonAPI.PyObject_Call(
      callable.underlying,
      createTuple(args).asInstanceOf[CPyValue].underlying,
      null
    )

    throwErrorIfOccured()

    new CPyValue(result)
  }

  def select(on: PyValue, value: String): PyValue = {
    local(new CPyValue(CPythonAPI.PyObject_GetAttr(
      on.asInstanceOf[CPyValue].underlying,
      valueFromString(value).asInstanceOf[CPyValue].underlying
    )))
  }

  def update(on: PyValue, value: String, newValue: PyValue): Unit = {
    local(CPythonAPI.PyObject_SetAttr(
      on.asInstanceOf[CPyValue].underlying,
      valueFromString(value).asInstanceOf[CPyValue].underlying,
      newValue.asInstanceOf[CPyValue].underlying
    ))
  }

  def selectList(on: PyValue, index: Int): PyValue = {
    val ret = new CPyValue(CPythonAPI.PyList_GetItem(
      on.asInstanceOf[CPyValue].underlying,
      Platform.intToCLong(index)
    ))

    throwErrorIfOccured()

    ret
  }

  def selectDictionary(on: PyValue, key: PyValue): PyValue = {
    val ret = new CPyValue(CPythonAPI.PyDict_GetItemWithError(
      on.asInstanceOf[CPyValue].underlying,
      key.asInstanceOf[CPyValue].underlying
    ))

    throwErrorIfOccured()

    ret
  }
}

class CPyValue(val underlying: Platform.Pointer, safeGlobal: Boolean = false) extends PyValue {
  if (Platform.isNative && PyValue.allocatedValues.isEmpty && !safeGlobal) {
    println(s"Warning: the value ${this.getStringified} was allocated into a global space, which means it will not be garbage collected in Scala Native")
  }

  def getStringified: String = {
    val pyStr = CPythonAPI.PyObject_Str(underlying)
    interpreter.throwErrorIfOccured()
    val cStr = CPythonAPI.PyUnicode_AsUTF8(pyStr)
    interpreter.throwErrorIfOccured()
    val ret = Platform.fromCString(cStr, java.nio.charset.Charset.forName("UTF-8"))
    CPythonAPI.Py_DecRef(pyStr)
    ret
  }

  def getString: String = {
    val cStr = CPythonAPI.PyUnicode_AsUTF8(underlying)
    interpreter.throwErrorIfOccured()
    Platform.fromCString(cStr, java.nio.charset.Charset.forName("UTF-8"))
  }
  
  def getBoolean: Boolean = {
    if (underlying == interpreter.falsePtr) false
    else if (underlying == interpreter.truePtr) true
    else {
      throw new IllegalAccessException("Cannot convert a non-boolean value to a boolean")
    }
  }
  
  def getLong: Long = {
    val ret = CPythonAPI.PyLong_AsLongLong(underlying)
    interpreter.throwErrorIfOccured()
    ret
  }
  
  def getDouble: Double = {
    val ret = CPythonAPI.PyFloat_AsDouble(underlying)
    interpreter.throwErrorIfOccured()
    ret
  }

  def getTuple: Seq[PyValue] = new Seq[PyValue] {
    def length: Int = {
      val ret = Platform.cLongToLong(CPythonAPI.PyTuple_Size(underlying)).toInt
      interpreter.throwErrorIfOccured()
      ret
    }
    
    def apply(idx: Int): PyValue = new CPyValue({
      val ret = CPythonAPI.PyTuple_GetItem(underlying, Platform.intToCLong(idx))
      interpreter.throwErrorIfOccured()
      ret
    })

    def iterator: Iterator[PyValue] = (0 until length).toIterator.map(apply)
  }

  def getSeq: Seq[PyValue] = new Seq[PyValue] {
    def length: Int = {
      val ret = Platform.cLongToLong(CPythonAPI.PyList_Size(underlying)).toInt
      interpreter.throwErrorIfOccured()
      ret
    }
    
    def apply(idx: Int): PyValue = new CPyValue({
      val ret = CPythonAPI.PyList_GetItem(underlying, Platform.intToCLong(idx))
      interpreter.throwErrorIfOccured()
      CPythonAPI.Py_IncRef(ret)
      ret
    })

    def iterator: Iterator[PyValue] = (0 until length).toIterator.map(apply)
  }

  import scala.collection.mutable
  def getMap: mutable.Map[PyValue, PyValue] = new mutable.Map[PyValue, PyValue] {
    def get(key: PyValue): Option[PyValue] = {
      val contains = CPythonAPI.PyDict_Contains(
        underlying,
        key.asInstanceOf[CPyValue].underlying
      ) == 1
      
      interpreter.throwErrorIfOccured()

      if (contains) {
        val value = CPythonAPI.PyDict_GetItem(underlying, key.asInstanceOf[CPyValue].underlying)
        interpreter.throwErrorIfOccured()
        CPythonAPI.Py_IncRef(value)
        Some(new CPyValue(value))
      } else Option.empty[PyValue]
    }
    
    def iterator: Iterator[(PyValue, PyValue)] = {
      val keysList = new CPyValue(CPythonAPI.PyDict_Keys(underlying))
      interpreter.throwErrorIfOccured()
      keysList.getSeq.toIterator.map { k =>
        (k, get(k).get)
      }
    }

    def +=(kv: (PyValue, PyValue)): this.type = ???
    def -=(k: PyValue): this.type = ???
  }

  private var cleaned = false

  override def cleanup(): Unit = {
    if (!cleaned) {
      cleaned = true
      CPythonAPI.Py_DecRef(underlying)
    }
  }
}
