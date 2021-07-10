package me.shadaj.scalapy.interpreter

import scala.sys
import scala.util.Try

import com.sun.jna.{Native, NativeLong, Memory, WString}
import scala.util.{Success, Failure, Properties}

class CPythonAPIInterface {
  val pythonLibrariesToTry =
    Option(System.getenv("SCALAPY_PYTHON_LIBRARY"))
      .orElse(Properties.propOrNone("scalapy.python.library"))
      .toSeq ++
      Seq(
        "python3",
        "python3.7", "python3.7m",
        "python3.8", "python3.8m",
        "python3.9", "python3.9m"
      )

  val loadAttempts = pythonLibrariesToTry.toStream.map(n => try {
    Native.register(n)
    Success(true)
  } catch {
    case t: Throwable => Failure(t)
  })

  loadAttempts.find(_.isSuccess).getOrElse {
    loadAttempts.foreach(_.failed.get.printStackTrace())
    throw new Exception(s"Unable to locate Python library, tried ${pythonLibrariesToTry.mkString(", ")}")
  }

  @scala.native def Py_SetProgramName(str: WString): Unit
  @scala.native def Py_Initialize(): Unit

  @scala.native def Py_DecodeLocale(str: String, size: Platform.Pointer): WString

  @scala.native def PyMem_RawFree(p: Platform.Pointer): Unit

  @scala.native def PyEval_SaveThread(): Platform.Pointer
  @scala.native def PyGILState_Ensure(): Int
  @scala.native def PyGILState_Release(state: Int): Unit

  @scala.native def PyRun_String(str: String, start: Int, globals: Platform.Pointer, locals: Platform.Pointer): Platform.Pointer

  @scala.native def PyUnicode_FromString(cStr: String): Platform.Pointer
  @scala.native def PyUnicode_AsUTF8(pyString: Platform.Pointer): Platform.Pointer

  @scala.native def PyBool_FromLong(long: NativeLong): Platform.Pointer

  @scala.native def PyNumber_Negative(o1: Platform.Pointer): Platform.Pointer
  @scala.native def PyNumber_Positive(o1: Platform.Pointer): Platform.Pointer
  @scala.native def PyNumber_Add(o1: Platform.Pointer, o2: Platform.Pointer): Platform.Pointer
  @scala.native def PyNumber_Subtract(o1: Platform.Pointer, o2: Platform.Pointer): Platform.Pointer
  @scala.native def PyNumber_Multiply(o1: Platform.Pointer, o2: Platform.Pointer): Platform.Pointer
  @scala.native def PyNumber_TrueDivide(o1: Platform.Pointer, o2: Platform.Pointer): Platform.Pointer
  @scala.native def PyNumber_Remainder(o1: Platform.Pointer, o2: Platform.Pointer): Platform.Pointer

  @scala.native def PyLong_FromLongLong(long: Long): Platform.Pointer
  @scala.native def PyLong_AsLong(pyLong: Platform.Pointer): Int
  @scala.native def PyLong_AsLongLong(pyLong: Platform.Pointer): Long

  @scala.native def PyFloat_FromDouble(double: Double): Platform.Pointer
  @scala.native def PyFloat_AsDouble(float: Platform.Pointer): Double

  @scala.native def PyDict_New(): Platform.Pointer
  @scala.native def PyDict_SetItem(dict: Platform.Pointer, key: Platform.Pointer, value: Platform.Pointer): Int
  @scala.native def PyDict_SetItemString(dict: Platform.Pointer, key: String, value: Platform.Pointer): Int
  @scala.native def PyDict_Contains(dict: Platform.Pointer, key: Platform.Pointer): Int
  @scala.native def PyDict_GetItem(dict: Platform.Pointer, key: Platform.Pointer): Platform.Pointer
  @scala.native def PyDict_GetItemString(dict: Platform.Pointer, key: String): Platform.Pointer
  @scala.native def PyDict_GetItemWithError(dict: Platform.Pointer, key: Platform.Pointer): Platform.Pointer
  @scala.native def PyDict_DelItemString(dict: Platform.Pointer, key: String): Int
  @scala.native def PyDict_Keys(dict: Platform.Pointer): Platform.Pointer

  @scala.native def PyList_New(size: Int): Platform.Pointer
  @scala.native def PyList_Size(list: Platform.Pointer): NativeLong
  @scala.native def PyList_GetItem(list: Platform.Pointer, index: NativeLong): Platform.Pointer
  @scala.native def PyList_SetItem(list: Platform.Pointer, index: NativeLong, item: Platform.Pointer): Int

  @scala.native def PyTuple_New(size: Int): Platform.Pointer
  @scala.native def PyTuple_Size(tuple: Platform.Pointer): NativeLong
  @scala.native def PyTuple_GetItem(tuple: Platform.Pointer, index: NativeLong): Platform.Pointer
  @scala.native def PyTuple_SetItem(tuple: Platform.Pointer, index: NativeLong, item: Platform.Pointer): Int

  @scala.native def PyObject_Str(obj: Platform.Pointer): Platform.Pointer
  @scala.native def PyObject_GetItem(obj: Platform.Pointer, idx: Platform.Pointer): Platform.Pointer
  @scala.native def PyObject_SetItem(obj: Platform.Pointer, key: Platform.Pointer, newValue: Platform.Pointer): Int
  @scala.native def PyObject_DelItem(obj: Platform.Pointer, idx: Platform.Pointer): Int
  @scala.native def PyObject_GetAttr(obj: Platform.Pointer, name: Platform.Pointer): Platform.Pointer
  @scala.native def PyObject_GetAttrString(obj: Platform.Pointer, name: String): Platform.Pointer
  @scala.native def PyObject_SetAttr(obj: Platform.Pointer, name: Platform.Pointer, newValue: Platform.Pointer): Platform.Pointer
  @scala.native def PyObject_SetAttrString(obj: Platform.Pointer, name: String, newValue: Platform.Pointer): Platform.Pointer
  @scala.native def PyObject_Call(obj: Platform.Pointer, args: Platform.Pointer, kwArgs: Platform.Pointer): Platform.Pointer
  @scala.native def PyObject_Length(obj: Platform.Pointer): NativeLong

  @scala.native def PySequence_GetItem(obj: Platform.Pointer, idx: Int): Platform.Pointer
  @scala.native def PySequence_SetItem(obj: Platform.Pointer, idx: Int, v: Platform.Pointer): Platform.Pointer
  @scala.native def PySequence_Length(obj: Platform.Pointer): NativeLong

  @scala.native def PyErr_Occurred(): Platform.Pointer
  @scala.native def PyErr_Fetch(pType: Platform.PointerToPointer, pValue: Platform.PointerToPointer, pTraceback: Platform.PointerToPointer): Unit
  @scala.native def PyErr_Print(): Unit
  @scala.native def PyErr_Clear(): Unit

  @scala.native def PyEval_GetBuiltins(): Platform.Pointer

  @scala.native def Py_BuildValue(str: String): Platform.Pointer

  @scala.native def PyLong_FromVoidPtr(ptr: Platform.Pointer): Unit
  @scala.native def PyCFunction_NewEx(ptr: Platform.Pointer, self: Platform.Pointer, module: Platform.Pointer): Platform.Pointer
  @scala.native def PyImport_ImportModule(str: String): Platform.Pointer

  @scala.native def PyErr_SetString(tpe: Platform.Pointer, message: String): Unit

  @scala.native def Py_IncRef(ptr: Platform.Pointer): Unit
  @scala.native def Py_DecRef(ptr: Platform.Pointer): Unit
}

object CPythonAPI extends CPythonAPIInterface
