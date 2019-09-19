package me.shadaj.scalapy.py

import scala.scalanative.{unsafe => snu}
import scala.scalanative.unsafe.{Ptr, CQuote}
import java.nio.charset.Charset

object Platform {
  final val isNative = true

  def Zone[T](fn: snu.Zone => T): T = snu.Zone(fn)

  def fromCString(ptr: Pointer, charset: Charset): String = {
    snu.fromCString(ptr, charset)
  }

  def toCString(str: String, charset: Charset = Charset.defaultCharset())(implicit zone: snu.Zone): CString = snu.toCString(str, charset)

  val emptyCString: CString = c""

  type CString = snu.CString
  type Pointer = Ptr[Byte]
  type PointerToPointer = Ptr[Ptr[Byte]]

  def allocPointerToPointer(implicit zone: snu.Zone): PointerToPointer = {
    snu.alloc[Ptr[Byte]]
  }

  def pointerToLong(pointer: Pointer): Long = {
    pointer.toLong
  }

  def cLongToLong(cLong: snu.CLong): Long = cLong

  def intToCLong(int: Int): snu.CLong = int

  def dereferencePointerToPointer(pointer: PointerToPointer): Pointer = !pointer
}
