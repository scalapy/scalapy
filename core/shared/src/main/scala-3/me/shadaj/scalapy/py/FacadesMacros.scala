package me.shadaj.scalapy.py

trait FacadesCreatorMacros {
  inline implicit def getCreator[F <: Any]: FacadeCreator[F] = ${FacadeImpl.creator[F]}
}

trait PyMacros {
  inline def native[T]: T = ${FacadeImpl.native_impl[T]}
  inline def nativeNamed[T]: T = ${FacadeImpl.native_named_impl[T]}
}