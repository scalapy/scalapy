package me.shadaj.scalapy.interpreter

import scala.scalanative.unsafe.{CFuncPtr, CFuncPtr2, Ptr}

trait PlatformMacros {
  inline def getFnPtr2(inline fn: (Ptr[Byte], Ptr[Byte]) => Ptr[Byte]): (scala.Any, Ptr[Byte]) =
    (null, CFuncPtr.toPtr(fn: CFuncPtr2[Ptr[Byte], Ptr[Byte], Ptr[Byte]]).asInstanceOf[Ptr[Byte]])
}
