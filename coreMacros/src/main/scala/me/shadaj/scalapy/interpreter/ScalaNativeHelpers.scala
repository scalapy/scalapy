package me.shadaj.scalapy.interpreter

import scala.reflect.macros.blackbox
import scala.language.experimental.macros

object ScalaNativeHelpers {
  def inlineFnPtr2(c: blackbox.Context)(fn: c.Tree): c.Tree = {
    import c.universe._

    q"""(null, _root_.scala.scalanative.runtime.fromRawPtr(_root_.scala.scalanative.runtime.Intrinsics.resolveCFuncPtr(new _root_.scala.scalanative.unsafe.CFuncPtr2[_root_.me.shadaj.scalapy.interpreter.Platform.Pointer, _root_.me.shadaj.scalapy.interpreter.Platform.Pointer, _root_.me.shadaj.scalapy.interpreter.Platform.Pointer] {
      def apply(in1: _root_.me.shadaj.scalapy.interpreter.Platform.Pointer, in2: _root_.me.shadaj.scalapy.interpreter.Platform.Pointer) = $fn(in1, in2)
    })))"""
  }
}
