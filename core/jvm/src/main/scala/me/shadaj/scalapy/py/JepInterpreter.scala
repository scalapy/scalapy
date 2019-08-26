package me.shadaj.scalapy.py

import scala.collection.JavaConverters._
import com.sun.jna.{Native, Pointer, NativeLong, Memory}
import com.sun.jna.ptr.PointerByReference

class CPythonAPIInterface {
  Native.register("python3.7m")

  @scala.native def Py_Initialize(): Unit

  @scala.native def PyRun_String(str: String, start: Int, globals: Pointer, locals: Pointer): Pointer 

  @scala.native def PyUnicode_FromString(cStr: String): Pointer 
  @scala.native def PyUnicode_AsUTF8(pyString: Pointer): String 

  @scala.native def PyBool_FromLong(long: NativeLong): Pointer 

  @scala.native def PyNumber_Negative(o1: Pointer): Pointer 
  @scala.native def PyNumber_Positive(o1: Pointer): Pointer 
  @scala.native def PyNumber_Add(o1: Pointer, o2: Pointer): Pointer 
  @scala.native def PyNumber_Subtract(o1: Pointer, o2: Pointer): Pointer 
  @scala.native def PyNumber_Multiply(o1: Pointer, o2: Pointer): Pointer 
  @scala.native def PyNumber_TrueDivide(o1: Pointer, o2: Pointer): Pointer 
  @scala.native def PyNumber_Remainder(o1: Pointer, o2: Pointer): Pointer 
  
  @scala.native def PyLong_FromLongLong(long: Long): Pointer 
  @scala.native def PyLong_AsLong(pyLong: Pointer): Int 
  @scala.native def PyLong_AsLongLong(pyLong: Pointer): Long 

  @scala.native def PyFloat_FromDouble(double: Double): Pointer 
  @scala.native def PyFloat_AsDouble(float: Pointer): Double 

  @scala.native def PyDict_New(): Pointer 
  @scala.native def PyDict_SetItem(dict: Pointer, key: Pointer, value: Pointer): Int 
  @scala.native def PyDict_SetItemString(dict: Pointer, key: String, value: Pointer): Int 
  @scala.native def PyDict_Contains(dict: Pointer, key: Pointer): Int 
  @scala.native def PyDict_GetItem(dict: Pointer, key: Pointer): Pointer 
  @scala.native def PyDict_GetItemString(dict: Pointer, key: Pointer): Pointer 
  @scala.native def PyDict_GetItemWithError(dict: Pointer, key: Pointer): Pointer 
  @scala.native def PyDict_DelItemString(dict: Pointer, key: String): Int 
  @scala.native def PyDict_Keys(dict: Pointer): Pointer 

  @scala.native def PyList_New(size: Int): Pointer 
  @scala.native def PyList_Size(list: Pointer): Long 
  @scala.native def PyList_GetItem(list: Pointer, index: Long): Pointer 
  @scala.native def PyList_SetItem(list: Pointer, index: Long, item: Pointer): Int 

  @scala.native def PyTuple_New(size: Int): Pointer 
  @scala.native def PyTuple_Size(tuple: Pointer): NativeLong 
  @scala.native def PyTuple_GetItem(tuple: Pointer, index: NativeLong): Pointer 
  @scala.native def PyTuple_SetItem(tuple: Pointer, index: NativeLong, item: Pointer): Int 

  @scala.native def PyObject_Str(obj: Pointer): Pointer 
  @scala.native def PyObject_GetAttr(obj: Pointer, name: Pointer): Pointer 
  @scala.native def PyObject_GetAttrString(obj: Pointer, name: String): Pointer 
  @scala.native def PyObject_SetAttr(obj: Pointer, name: Pointer, newValue: Pointer): Pointer 
  @scala.native def PyObject_SetAttrString(obj: Pointer, name: String, newValue: Pointer): Pointer 
  @scala.native def PyObject_Call(obj: Pointer, args: Pointer, kwArgs: Pointer): Pointer 

  @scala.native def PyErr_Occurred(): Pointer 
  @scala.native def PyErr_Fetch(pType: Pointer, pValue: Pointer, pTraceback: Pointer): Unit 
  @scala.native def PyErr_Print(): Unit 
  @scala.native def PyErr_Clear(): Unit 

  @scala.native def PyEval_GetBuiltins(): Pointer 

  @scala.native def Py_BuildValue(str: String): Pointer 

  @scala.native def Py_IncRef(ptr: Pointer): Unit 
  @scala.native def Py_DecRef(ptr: Pointer): Unit 
}

object CPythonAPIRef {
  val CPythonAPI = new CPythonAPIInterface
  CPythonAPI.Py_Initialize()
}

import CPythonAPIRef.CPythonAPI

class CPythonInterpreter extends Interpreter {
  val globals: Pointer = CPythonAPI.PyDict_New()
  val builtins = new CPyValue(CPythonAPI.PyEval_GetBuiltins(), true)
  set("__builtins__", builtins)

  val falsePtr: Pointer = CPythonAPI.PyBool_FromLong(new NativeLong(0))
  val truePtr: Pointer = CPythonAPI.PyBool_FromLong(new NativeLong(1))

  val noneValue: PyValue = new CPyValue(CPythonAPI.Py_BuildValue(""), true)
  
  override def eval(code: String): Unit = {
    val Py_single_input = 256
    CPythonAPI.PyRun_String(code, Py_single_input, globals, globals)
    throwErrorIfOccured()
  }

  override def set(variable: String, value: PyValue): Unit = {
    CPythonAPI.PyDict_SetItemString(globals, variable, value.asInstanceOf[CPyValue].underlying)
    CPythonAPI.Py_IncRef(value.asInstanceOf[CPyValue].underlying)
    throwErrorIfOccured()
  }

  private var counter = 0
  def getVariableReference(value: PyValue): VariableReference = {
    val variableName = "spy_o_" + counter
    counter += 1
    
    CPythonAPI.PyDict_SetItemString(globals, variableName, value.asInstanceOf[CPyValue].underlying)
    CPythonAPI.Py_IncRef(value.asInstanceOf[CPyValue].underlying)
    throwErrorIfOccured()

    new VariableReference(variableName)
  }
  
  def valueFromBoolean(b: Boolean): PyValue = new CPyValue(CPythonAPI.PyBool_FromLong(
    new NativeLong(if (b) 1 else 0)
  ))
  def valueFromLong(long: Long): PyValue = new CPyValue(CPythonAPI.PyLong_FromLongLong(long))
  def valueFromDouble(v: Double): PyValue = new CPyValue(CPythonAPI.PyFloat_FromDouble(v))
  def valueFromString(v: String): PyValue = {
    var ret: PyValue = null
    ret = new CPyValue(CPythonAPI.PyUnicode_FromString(
      v
    ))

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
      CPythonAPI.PyTuple_SetItem(retPtr, new NativeLong(i), v.asInstanceOf[CPyValue].underlying)
      CPythonAPI.Py_IncRef(v.asInstanceOf[CPyValue].underlying)
    }

    new CPyValue(retPtr)
  }

  def throwErrorIfOccured() = {
    if (Pointer.nativeValue(CPythonAPI.PyErr_Occurred()) != 0) {
      val pType = new Memory(Native.POINTER_SIZE)
      val pValue = new Memory(Native.POINTER_SIZE)
      val pTraceback = new Memory(Native.POINTER_SIZE)

      CPythonAPI.PyErr_Fetch(pType, pValue, pTraceback)

      val errorMessage = local {
        (new CPyValue(CPythonAPI.PyObject_Str(pType.getPointer(0)))).getString + (if (pValue.getPointer(0) != null) {
          " " + (new CPyValue(CPythonAPI.PyObject_Str(pValue.getPointer(0)))).getString
        } else "")
      }

      throw new PythonException(errorMessage)
    }
  }

  override def load(code: String): PyValue = {
    var ret: CPyValue = null
    val Py_eval_input = 258
    val result = CPythonAPI.PyRun_String(code, Py_eval_input, globals, globals)
    throwErrorIfOccured()

    CPythonAPI.Py_IncRef(result)

    ret = new CPyValue(result)

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
    var callable: CPyValue = {
      val gottenAttr = CPythonAPI.PyObject_GetAttrString(on.asInstanceOf[CPyValue].underlying, method)
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
}

class CPyValue(val underlying: Pointer, safeGlobal: Boolean = false) extends PyValue {
  if (PyValue.allocatedValues.isEmpty && !safeGlobal) {
    println(s"Warning: the value ${this.getStringified} was allocated into a global space, which means it will not be garbage collected in Scala Native")
  }

  def getStringified: String = {
    val pyStr = CPythonAPI.PyObject_Str(underlying)
    interpreter.throwErrorIfOccured()
    val cStr = CPythonAPI.PyUnicode_AsUTF8(pyStr)
    interpreter.throwErrorIfOccured()
    val ret = cStr
    CPythonAPI.Py_DecRef(pyStr)
    ret
  }

  def getString: String = {
    val cStr = CPythonAPI.PyUnicode_AsUTF8(underlying)
    interpreter.throwErrorIfOccured()
    cStr
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
      val ret = CPythonAPI.PyTuple_Size(underlying).intValue
      interpreter.throwErrorIfOccured()
      ret
    }
    
    def apply(idx: Int): PyValue = new CPyValue({
      val ret = CPythonAPI.PyTuple_GetItem(underlying, new NativeLong(idx))
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
