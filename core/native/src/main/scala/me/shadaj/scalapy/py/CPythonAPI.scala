package me.shadaj.scalapy.py

import scala.scalanative.native._

@extern
object CPythonAPI {
  def Py_Initialize(): Unit = extern

  def PyEval_SaveThread(): Platform.Pointer = extern
  def PyGILState_Ensure(): Int = extern
  def PyGILState_Release(state: Int): Unit = extern

  def PyRun_String(str: CString, start: Int, globals: Platform.Pointer, locals: Platform.Pointer): Platform.Pointer = extern

  def PyUnicode_FromString(cStr: CString): Platform.Pointer = extern
  def PyUnicode_AsUTF8(pyString: Platform.Pointer): CString = extern

  def PyBool_FromLong(long: CLong): Platform.Pointer = extern

  def PyNumber_Negative(o1: Platform.Pointer): Platform.Pointer = extern
  def PyNumber_Positive(o1: Platform.Pointer): Platform.Pointer = extern
  def PyNumber_Add(o1: Platform.Pointer, o2: Platform.Pointer): Platform.Pointer = extern
  def PyNumber_Subtract(o1: Platform.Pointer, o2: Platform.Pointer): Platform.Pointer = extern
  def PyNumber_Multiply(o1: Platform.Pointer, o2: Platform.Pointer): Platform.Pointer = extern
  def PyNumber_TrueDivide(o1: Platform.Pointer, o2: Platform.Pointer): Platform.Pointer = extern
  def PyNumber_Remainder(o1: Platform.Pointer, o2: Platform.Pointer): Platform.Pointer = extern
  
  def PyLong_FromLongLong(long: CLongLong): Platform.Pointer = extern
  def PyLong_AsLong(pyLong: Platform.Pointer): CLong = extern
  def PyLong_AsLongLong(pyLong: Platform.Pointer): CLongLong = extern

  def PyFloat_FromDouble(double: CDouble): Platform.Pointer = extern
  def PyFloat_AsDouble(float: Platform.Pointer): Double = extern

  def PyDict_New(): Platform.Pointer = extern
  def PyDict_SetItem(dict: Platform.Pointer, key: Platform.Pointer, value: Platform.Pointer): Int = extern
  def PyDict_SetItemString(dict: Platform.Pointer, key: CString, value: Platform.Pointer): Int = extern
  def PyDict_Contains(dict: Platform.Pointer, key: Platform.Pointer): Int = extern
  def PyDict_GetItem(dict: Platform.Pointer, key: Platform.Pointer): Platform.Pointer = extern
  def PyDict_GetItemString(dict: Platform.Pointer, key: Platform.Pointer): Platform.Pointer = extern
  def PyDict_GetItemWithError(dict: Platform.Pointer, key: Platform.Pointer): Platform.Pointer = extern
  def PyDict_DelItemString(dict: Platform.Pointer, key: CString): Int = extern
  def PyDict_Keys(dict: Platform.Pointer): Platform.Pointer = extern

  def PyList_New(size: Int): Platform.Pointer = extern
  def PyList_Size(list: Platform.Pointer): CSize = extern
  def PyList_GetItem(list: Platform.Pointer, index: CSize): Platform.Pointer = extern
  def PyList_SetItem(list: Platform.Pointer, index: CSize, item: Platform.Pointer): Int = extern

  def PyTuple_New(size: Int): Platform.Pointer = extern
  def PyTuple_Size(tuple: Platform.Pointer): CSize = extern
  def PyTuple_GetItem(tuple: Platform.Pointer, index: CSize): Platform.Pointer = extern
  def PyTuple_SetItem(tuple: Platform.Pointer, index: CSize, item: Platform.Pointer): Int = extern

  def PyObject_Str(obj: Platform.Pointer): Platform.Pointer = extern
  def PyObject_GetItem(obj: Platform.Pointer, idx: Platform.Pointer): Platform.Pointer = extern
  def PyObject_GetAttr(obj: Platform.Pointer, name: Platform.Pointer): Platform.Pointer = extern
  def PyObject_GetAttrString(obj: Platform.Pointer, name: CString): Platform.Pointer = extern
  def PyObject_SetAttr(obj: Platform.Pointer, name: Platform.Pointer, newValue: Platform.Pointer): Platform.Pointer = extern
  def PyObject_SetAttrString(obj: Platform.Pointer, name: CString, newValue: Platform.Pointer): Platform.Pointer = extern
  def PyObject_CallMethodObjArgs(obj: Platform.Pointer, name: Platform.Pointer, args: CVararg*): Platform.Pointer = extern
  def PyObject_Call(obj: Platform.Pointer, args: Platform.Pointer, kwArgs: Platform.Pointer): Platform.Pointer = extern
  def PyObject_Length(obj: Platform.Pointer): CLong = extern

  def PyErr_Occurred(): Platform.Pointer = extern
  def PyErr_Fetch(pType: Platform.PointerToPointer, pValue: Platform.PointerToPointer, pTraceback: Platform.PointerToPointer): Unit = extern
  def PyErr_Print(): Unit = extern
  def PyErr_Clear(): Unit = extern

  def PyEval_GetBuiltins(): Platform.Pointer = extern

  def Py_BuildValue(str: CString): Platform.Pointer = extern

  def Py_IncRef(ptr: Platform.Pointer): Unit = extern
  def Py_DecRef(ptr: Platform.Pointer): Unit = extern
}
