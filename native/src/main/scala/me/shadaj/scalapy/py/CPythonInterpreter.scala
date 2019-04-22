package me.shadaj.scalapy.py

import scala.scalanative.native._

trait JepInterpreter {
  def valueFromAny(v: Any): PyValue
}

@extern
object CPythonAPI {
  def Py_Initialize(): Unit = extern

  def PyRun_String(str: CString, start: Int, globals: Ptr[Byte], locals: Ptr[Byte]): Ptr[Byte] = extern

  def PyUnicode_FromString(cStr: CString): Ptr[Byte] = extern
  def PyUnicode_AsUTF8(pyString: Ptr[Byte]): CString = extern

  def PyBool_FromLong(long: CLong): Ptr[Byte] = extern

  def PyNumber_Negative(o1: Ptr[Byte]): Ptr[Byte] = extern
  def PyNumber_Positive(o1: Ptr[Byte]): Ptr[Byte] = extern
  def PyNumber_Add(o1: Ptr[Byte], o2: Ptr[Byte]): Ptr[Byte] = extern
  def PyNumber_Subtract(o1: Ptr[Byte], o2: Ptr[Byte]): Ptr[Byte] = extern
  def PyNumber_Multiply(o1: Ptr[Byte], o2: Ptr[Byte]): Ptr[Byte] = extern
  def PyNumber_TrueDivide(o1: Ptr[Byte], o2: Ptr[Byte]): Ptr[Byte] = extern
  def PyNumber_Remainder(o1: Ptr[Byte], o2: Ptr[Byte]): Ptr[Byte] = extern
  
  def PyLong_FromLongLong(long: CLongLong): Ptr[Byte] = extern
  def PyLong_AsLong(pyLong: Ptr[Byte]): CLong = extern
  def PyLong_AsLongLong(pyLong: Ptr[Byte]): CLongLong = extern

  def PyFloat_FromDouble(double: CDouble): Ptr[Byte] = extern
  def PyFloat_AsDouble(float: Ptr[Byte]): Double = extern

  def PyDict_New(): Ptr[Byte] = extern
  def PyDict_SetItem(dict: Ptr[Byte], key: Ptr[Byte], value: Ptr[Byte]): Int = extern
  def PyDict_SetItemString(dict: Ptr[Byte], key: CString, value: Ptr[Byte]): Int = extern
  def PyDict_Contains(dict: Ptr[Byte], key: Ptr[Byte]): Int = extern
  def PyDict_GetItem(dict: Ptr[Byte], key: Ptr[Byte]): Ptr[Byte] = extern
  def PyDict_GetItemString(dict: Ptr[Byte], key: Ptr[Byte]): Ptr[Byte] = extern
  def PyDict_GetItemWithError(dict: Ptr[Byte], key: Ptr[Byte]): Ptr[Byte] = extern
  def PyDict_DelItemString(dict: Ptr[Byte], key: CString): Int = extern
  def PyDict_Keys(dict: Ptr[Byte]): Ptr[Byte] = extern

  def PyList_New(size: Int): Ptr[Byte] = extern
  def PyList_Size(list: Ptr[Byte]): CSize = extern
  def PyList_GetItem(list: Ptr[Byte], index: CSize): Ptr[Byte] = extern
  def PyList_SetItem(list: Ptr[Byte], index: CSize, item: Ptr[Byte]): Int = extern

  def PyTuple_New(size: Int): Ptr[Byte] = extern
  def PyTuple_Size(tuple: Ptr[Byte]): CSize = extern
  def PyTuple_GetItem(tuple: Ptr[Byte], index: CSize): Ptr[Byte] = extern
  def PyTuple_SetItem(tuple: Ptr[Byte], index: CSize, item: Ptr[Byte]): Int = extern

  def PyObject_Str(obj: Ptr[Byte]): Ptr[Byte] = extern
  def PyObject_GetAttr(obj: Ptr[Byte], name: Ptr[Byte]): Ptr[Byte] = extern
  def PyObject_GetAttrString(obj: Ptr[Byte], name: CString): Ptr[Byte] = extern
  def PyObject_CallMethodObjArgs(obj: Ptr[Byte], name: Ptr[Byte], args: CVararg*): Ptr[Byte] = extern
  def PyObject_Call(obj: Ptr[Byte], args: Ptr[Byte], kwArgs: Ptr[Byte]): Ptr[Byte] = extern

  def PyErr_Occurred(): Ptr[Byte] = extern
  def PyErr_Fetch(pType: Ptr[Ptr[Byte]], pValue: Ptr[Ptr[Byte]], pTraceback: Ptr[Ptr[Byte]]): Unit = extern
  def PyErr_Print(): Unit = extern
  def PyErr_Clear(): Unit = extern

  def PyEval_GetBuiltins(): Ptr[Byte] = extern

  def Py_BuildValue(str: CString): Ptr[Byte] = extern

  def Py_IncRef(ptr: Ptr[Byte]): Unit = extern
  def Py_DecRef(ptr: Ptr[Byte]): Unit = extern
}

class CPythonInterpreter extends Interpreter {
  CPythonAPI.Py_Initialize()

  val globals: Ptr[Byte] = CPythonAPI.PyDict_New()
  val builtins = new CPyValue(CPythonAPI.PyEval_GetBuiltins())
  set("__builtins__", builtins)

  val falsePtr: Ptr[Byte] = CPythonAPI.PyBool_FromLong(0)
  val truePtr: Ptr[Byte] = CPythonAPI.PyBool_FromLong(1)
  
  override def eval(code: String): Unit = {
    Zone { implicit zone =>
      val Py_single_input = 256
      CPythonAPI.PyRun_String(toCString(code), Py_single_input, globals, globals)
      throwErrorIfOccured()
    }
  }

  override def set(variable: String, value: PyValue): Unit = {
    Zone { implicit zone =>
      CPythonAPI.PyDict_SetItemString(globals, toCString(variable), value.asInstanceOf[CPyValue].underlying)
      CPythonAPI.Py_IncRef(value.asInstanceOf[CPyValue].underlying)
      throwErrorIfOccured()
    }
  }

  private var counter = 0
  def getVariableReference(value: PyValue): VariableReference = {
    val variableName = "spy_o_" + counter
    counter += 1
    
    Zone { implicit zone =>
      CPythonAPI.PyDict_SetItemString(globals, toCString(variableName), value.asInstanceOf[CPyValue].underlying)
      CPythonAPI.Py_IncRef(value.asInstanceOf[CPyValue].underlying)
      throwErrorIfOccured()
    }

    new VariableReference(variableName)
  }
  
  def valueFromBoolean(b: Boolean): PyValue = new CPyValue(CPythonAPI.PyBool_FromLong(
    if (b) 1 else 0
  ))
  def valueFromLong(long: Long): PyValue = new CPyValue(CPythonAPI.PyLong_FromLongLong(long))
  def valueFromDouble(v: Double): PyValue = new CPyValue(CPythonAPI.PyFloat_FromDouble(v))
  def valueFromString(v: String): PyValue = {
    var ret: PyValue = null
    Zone { implicit zone =>
      ret = new CPyValue(CPythonAPI.PyUnicode_FromString(
        toCString(v, java.nio.charset.Charset.forName("UTF-8"))
      ))
    }

    ret
  }

  def createList(seq: Seq[PyValue]): PyValue = {
    // TODO: this is copying, should be replaced by a custom type
    val retPtr = CPythonAPI.PyList_New(seq.size)
    seq.zipWithIndex.foreach { case (v, i) =>
      CPythonAPI.PyList_SetItem(retPtr, i, v.asInstanceOf[CPyValue].underlying)
      CPythonAPI.Py_IncRef(v.asInstanceOf[CPyValue].underlying)
    }

    new CPyValue(retPtr)
  }

  def createTuple(seq: Seq[PyValue]): PyValue = {
    // TODO: this is copying, should be replaced by a custom type
    val retPtr = CPythonAPI.PyTuple_New(seq.size)
    seq.zipWithIndex.foreach { case (v, i) =>
      CPythonAPI.PyTuple_SetItem(retPtr, i, v.asInstanceOf[CPyValue].underlying)
      CPythonAPI.Py_IncRef(v.asInstanceOf[CPyValue].underlying)
    }

    new CPyValue(retPtr)
  }

  val noneValue: PyValue = new CPyValue(CPythonAPI.Py_BuildValue(c""))

  def throwErrorIfOccured() = {
    if (CPythonAPI.PyErr_Occurred().cast[Int] != 0) {
      val pType = stackalloc[Ptr[Byte]]
      val pValue = stackalloc[Ptr[Byte]]
      val pTraceback = stackalloc[Ptr[Byte]]

      CPythonAPI.PyErr_Fetch(pType, pValue, pTraceback)

      val errorMessage = local {
        (new CPyValue(CPythonAPI.PyObject_Str(!pType))).getString + (if (!pValue != null) {
          " " + (new CPyValue(CPythonAPI.PyObject_Str(!pValue))).getString
        } else "")
      }

      throw new Exception(errorMessage)
    }
  }

  override def load(code: String): PyValue = {
    var ret: CPyValue = null
    Zone { implicit zone =>
      val Py_eval_input = 258
      val result = CPythonAPI.PyRun_String(toCString(code), Py_eval_input, globals, globals)
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
    var callable: CPyValue = Zone { implicit zone =>
      val gottenAttr = CPythonAPI.PyObject_GetAttrString(on.asInstanceOf[CPyValue].underlying, toCString(method))
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

  def selectList(on: PyValue, index: Int): PyValue = {
    val ret = new CPyValue(CPythonAPI.PyList_GetItem(
      on.asInstanceOf[CPyValue].underlying,
      index
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

  def binaryOp(op: String, a: PyValue, b: PyValue): PyValue = ???
}

class CPyValue(val underlying: Ptr[Byte]) extends PyValue {
  def getStringified: String = {
    val pyStr = CPythonAPI.PyObject_Str(underlying)
    interpreter.throwErrorIfOccured()
    val cStr = CPythonAPI.PyUnicode_AsUTF8(pyStr)
    interpreter.throwErrorIfOccured()
    val ret = fromCString(cStr, java.nio.charset.Charset.forName("UTF-8"))
    CPythonAPI.Py_DecRef(pyStr)
    ret
  }

  def getString: String = {
    val cStr = CPythonAPI.PyUnicode_AsUTF8(underlying)
    interpreter.throwErrorIfOccured()
    fromCString(cStr, java.nio.charset.Charset.forName("UTF-8"))
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
      val ret = CPythonAPI.PyTuple_Size(underlying).toInt
      interpreter.throwErrorIfOccured()
      ret
    }
    
    def apply(idx: Int): PyValue = new CPyValue({
      val ret = CPythonAPI.PyTuple_GetItem(underlying, idx)
      interpreter.throwErrorIfOccured()
      ret
    })

    def iterator: Iterator[PyValue] = (0 until length).toIterator.map(apply)
  }

  def getSeq: Seq[PyValue] = new Seq[PyValue] {
    def length: Int = {
      val ret = CPythonAPI.PyList_Size(underlying).toInt
      interpreter.throwErrorIfOccured()
      ret
    }
    
    def apply(idx: Int): PyValue = new CPyValue({
      val ret = CPythonAPI.PyList_GetItem(underlying, idx)
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

  override def cleanup(): Unit = {
    CPythonAPI.Py_DecRef(underlying)
  }
}
