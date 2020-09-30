package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter.CPythonInterpreter

object PyConverters {
  implicit class SeqConverters[T, C <% Seq[T]](seq: C) {
    def toPythonCopy(implicit elemWriter: Writer[T]): Any = {
      Any.populateWith(CPythonInterpreter.createListCopy(seq, elemWriter.write))
    }

    def toPythonProxy(implicit elemWriter: Writer[T]): Any = {
      Any.populateWith(CPythonInterpreter.createListProxy(seq, elemWriter.write))
    }
  }
}
