package me.shadaj.scalapy.py

import scala.annotation.StaticAnnotation

trait FacadesCreatorMacros {
  inline implicit def getCreator[F <: Any]: FacadeCreator[F] = ${macros.FacadeImpl.creator[F]}
}

trait PyMacros {
  class PyBracketAccess extends StaticAnnotation
  class native extends StaticAnnotation

  inline def native[T]: T = ${macros.FacadeImpl.native_impl[T]}
  inline def nativeNamed[T]: T = ${macros.FacadeImpl.native_named_impl[T]}
}
