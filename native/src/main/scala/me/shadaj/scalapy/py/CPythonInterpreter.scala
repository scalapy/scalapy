package me.shadaj.scalapy.py

import scala.scalanative.native._

trait JepInterpreter {
  def valueFromAny(v: Any): PyValue
}

@link("python3.7m")
@extern
object CPythonAPI {
  def Py_Initialize(): Unit = extern

  def PyRun_String(str: CString, start: Int, globals: Ptr[Byte], locals: Ptr[Byte]): Ptr[Byte] = extern

  def PyUnicode_FromString(cStr: CString): Ptr[Byte] = extern
  def PyUnicode_AsUTF8(pyString: Ptr[Byte]): CString = extern

  def PyBool_FromLong(long: CLong): Ptr[Byte] = extern
  
  def PyLong_FromLongLong(long: CLongLong): Ptr[Byte] = extern
  def PyLong_AsLong(pyLong: Ptr[Byte]): CLong = extern
  def PyLong_AsLongLong(pyLong: Ptr[Byte]): CLongLong = extern

  def PyFloat_FromDouble(double: CDouble): Ptr[Byte] = extern
  def PyFloat_AsDouble(float: Ptr[Byte]): Double = extern

  def PyDict_New(): Ptr[Byte] = extern
  def PyDict_SetItem(dict: Ptr[Byte], key: Ptr[Byte], value: Ptr[Byte]): Int = extern
  def PyDict_SetItemString(dict: Ptr[Byte], key: CString, value: Ptr[Byte]): Int = extern

  def PyObject_Str(obj: Ptr[Byte]): Ptr[Byte] = extern

  def PyErr_Occurred(): Ptr[Byte] = extern
  def PyErr_Fetch(pType: Ptr[Ptr[Byte]], pValue: Ptr[Ptr[Byte]], pTraceback: Ptr[Ptr[Byte]]): Unit = extern
  def PyErr_Print(): Unit = extern

  def PyEval_GetBuiltins(): Ptr[Byte] = extern

  def Py_BuildValue(str: CString): Ptr[Byte] = extern
}

class CPythonInterpreter extends Interpreter {
  CPythonAPI.Py_Initialize()

  val globals: Ptr[Byte] = CPythonAPI.PyDict_New()
  set("__builtins__", new CPyValue(CPythonAPI.PyEval_GetBuiltins()))

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
      throwErrorIfOccured()
    }
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

  def noneValue: PyValue = new CPyValue(CPythonAPI.Py_BuildValue(c""))

  def throwErrorIfOccured() = {
    if (CPythonAPI.PyErr_Occurred().cast[Int] != 0) {
      val pType = stackalloc[Ptr[Byte]]
      val pValue = stackalloc[Ptr[Byte]]
      val pTraceback = stackalloc[Ptr[Byte]]

      CPythonAPI.PyErr_Fetch(pType, pValue, pTraceback)

      val errorMessage = (new CPyValue(CPythonAPI.PyObject_Str(!pType))).getString

      throw new Exception(errorMessage)
    }
  }

  override def load(code: String): PyValue = {
    var ret: CPyValue = null
    Zone { implicit zone =>
      val Py_eval_input = 258
      val result = CPythonAPI.PyRun_String(toCString(code), Py_eval_input, globals, globals)
      throwErrorIfOccured()
      ret = new CPyValue(result)
    }

    ret
  }
}

class CPyValue(val underlying: Ptr[Byte]) extends PyValue {
  def getString: String = {
    val cStr = CPythonAPI.PyUnicode_AsUTF8(underlying)
    interpreter.asInstanceOf[CPythonInterpreter].throwErrorIfOccured()
    fromCString(cStr, java.nio.charset.Charset.forName("UTF-8"))
  }
  
  def getBoolean: Boolean = {
    if (underlying == interpreter.asInstanceOf[CPythonInterpreter].falsePtr) false
    else if (underlying == interpreter.asInstanceOf[CPythonInterpreter].truePtr) true
    else {
      throw new IllegalAccessException("Cannot convert a non-boolean value to a boolean")
    }
  }
  def getLong: Long = {
    val ret = CPythonAPI.PyLong_AsLongLong(underlying)
    interpreter.asInstanceOf[CPythonInterpreter].throwErrorIfOccured()
    ret
  }
  
  def getDouble: Double = {
    val ret = CPythonAPI.PyFloat_AsDouble(underlying)
    interpreter.asInstanceOf[CPythonInterpreter].throwErrorIfOccured()
    ret
  }

  def getSeq: Seq[PyValue] = ???

  def getAny: Any = ???
}
