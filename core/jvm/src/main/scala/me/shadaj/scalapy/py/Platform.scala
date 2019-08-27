package me.shadaj.scalapy.py

import com.sun.jna
import com.sun.jna.Native
import java.nio.charset.Charset

object Platform {
  type InterpreterImplementation = CPythonInterpreter
  def newInterpreter: InterpreterImplementation = new CPythonInterpreter
  final val isNative = false

  def Zone[T](fn: Unit => T): T = fn(())

  def fromCString(ptr: Pointer, charset: Charset): String = {
    ptr.getString(0, charset.name())
  }

  def toCString(str: String, charset: Charset = Charset.defaultCharset()): CString = str

  val emptyCString: CString = ""

  type CString = String
  type Pointer = jna.Pointer
  type PointerToPointer = jna.Pointer

  def allocPointerToPointer: PointerToPointer = {
    new jna.Memory(Native.POINTER_SIZE)
  }

  def pointerToLong(pointer: Pointer): Long = {
    jna.Pointer.nativeValue(pointer)
  }
  
  def cLongToLong(cLong: jna.NativeLong): Long = cLong.longValue()

  def intToCLong(int: Int): jna.NativeLong = new jna.NativeLong(int)

  def dereferencePointerToPointer(pointer: PointerToPointer): Pointer = pointer.getPointer(0)
}
