package me.shadaj.scalapy.interpreter

import com.sun.jna
import com.sun.jna.Native
import java.nio.charset.Charset

object Platform {
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
  type FunctionPointer = jna.Callback
  type ThreadLocal[T] = java.lang.ThreadLocal[T]

  def threadLocalWithInitial[T](initial: () => T) = java.lang.ThreadLocal.withInitial(() => initial())

  def allocPointerToPointer: PointerToPointer = {
    new jna.Memory(Native.POINTER_SIZE)
  }

  def pointerToLong(pointer: Pointer): Long = {
    jna.Pointer.nativeValue(pointer)
  }

  def cLongToLong(cLong: jna.NativeLong): Long = cLong.longValue()
  def cSizeToLong(cSize: jna.NativeLong): Long = cSize.longValue()

  def intToCLong(int: Int): jna.NativeLong = new jna.NativeLong(int)
  def intToCSize(int: Int): jna.NativeLong = new jna.NativeLong(int)

  def dereferencePointerToPointer(pointer: PointerToPointer): Pointer = pointer.getPointer(0)

  // hack when using old JNA versions in Ammonite
  val cbkRef = Class.forName("com.sun.jna.CallbackReference")
  val meth = cbkRef.getMethod("getFunctionPointer", classOf[jna.Callback])
  meth.setAccessible(true)

  class Callback0(fn: () => Unit) extends jna.Callback {
    def callback(): Unit = fn.apply()
  }

  def getFnPtr0(fn: () => Unit): Pointer = {
    jna.CallbackReference.getFunctionPointer(new Callback0(fn))
  }

  class Callback1(fn: Pointer => Unit) extends jna.Callback {
    def callback(in: Pointer): Unit = fn.apply(in)
  }

  def getFnPtr1(fn: Pointer => Unit): Pointer = {
    jna.CallbackReference.getFunctionPointer(new Callback1(fn))
  }

  class Callback2(fn: (Pointer, Pointer) => Pointer) extends jna.Callback {
    def callback(in1: Pointer, in2: Pointer): Pointer = fn.apply(in1, in2)
  }

  def getFnPtr2(fn: (Pointer, Pointer) => Pointer): (scala.Any, Pointer) = {
    val cbk = new Callback2(fn)
    (cbk, meth.invoke(null, cbk).asInstanceOf[Pointer])
  }

  def alloc(size: Int): Pointer = new jna.Memory(size)

  def ptrSize: Int = Native.POINTER_SIZE

  def setPtrFnPtr(ptr: Pointer, offset: Int, value: Long): Unit = ptr.setLong(offset, value)
  def setPtrLong(ptr: Pointer, offset: Int, value: Long): Unit = ptr.setLong(offset, value)
  def setPtrInt(ptr: Pointer, offset: Int, value: Int): Unit = ptr.setInt(offset, value)
  def setPtrByte(ptr: Pointer, offset: Int, value: Byte): Unit = ptr.setByte(offset, value)

  def toCWideString[T](str: String)(fn: jna.WString => T): T = fn(new jna.WString(str))
}
