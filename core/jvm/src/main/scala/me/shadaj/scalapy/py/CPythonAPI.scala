package me.shadaj.scalapy.py

import com.sun.jna.{Native, Pointer, NativeLong, Memory}
import com.sun.jna.ptr.PointerByReference

class CPythonAPIInterface {
  Native.register("python3.7m")

  @scala.native def Py_Initialize(): Unit

  @scala.native def PyRun_String(str: String, start: Int, globals: Pointer, locals: Pointer): Pointer 

  @scala.native def PyUnicode_FromString(cStr: String): Pointer 
  @scala.native def PyUnicode_AsUTF8(pyString: Pointer): Pointer 

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
  @scala.native def PyList_Size(list: Pointer): NativeLong 
  @scala.native def PyList_GetItem(list: Pointer, index: NativeLong): Pointer 
  @scala.native def PyList_SetItem(list: Pointer, index: NativeLong, item: Pointer): Int 

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

object CPythonAPI extends CPythonAPIInterface
