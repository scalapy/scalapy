package me.shadaj.scalapy.py

import scala.scalanative.{native => sn}
import scala.scalanative.native.Ptr
import java.nio.charset.Charset
import scala.scalanative.native.CQuote

object Platform {
  final val isNative = true

  def Zone[T](fn: sn.Zone => T): T = sn.Zone(fn)

  def fromCString(ptr: Pointer, charset: Charset): String = {
    sn.fromCString(ptr, charset)
  }

  def toCString(str: String,
    charset: Charset = Charset.defaultCharset())(implicit zone: sn.Zone): CString =
    sn.toCString(str, charset)

  def toCStringNativeStringJVM(str: String,
    charset: Charset = Charset.defaultCharset())(implicit zone: sn.Zone): CString =
    toCString(str, charset)

  val emptyCString: CString = c""

  type CString = sn.CString
  type Pointer = Ptr[Byte]
  type PointerToPointer = Ptr[Ptr[Byte]]

  def allocPointerToPointer(implicit zone: sn.Zone): PointerToPointer = {
    sn.alloc[Ptr[Byte]]
  }

  def pointerToLong(pointer: Pointer): Long = {
    pointer.cast[Long]
  }

  def cLongToLong(cLong: sn.CLong): Long = cLong

  def intToCLong(int: Int): sn.CLong = int

  def dereferencePointerToPointer(pointer: PointerToPointer): Pointer = !pointer
}
