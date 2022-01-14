package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue}
import me.shadaj.scalapy.interpreter.Platform

import me.shadaj.scalapy.readwrite.Writer

trait ConvertableToSeqElem[T] {
  def convertCopy(v: T): Platform.Pointer
  def convertProxy(v: T): PyValue
}

object ConvertableToSeqElem {
  implicit def seqConvertableSeqElem[T, C](implicit ev: C => scala.collection.Seq[T], elemConvertable: ConvertableToSeqElem[T]): ConvertableToSeqElem[C] = new ConvertableToSeqElem[C] {
    def convertCopy(v: C): Platform.Pointer = {
      CPythonInterpreter.createListCopy(ev(v), elemConvertable.convertCopy)
    }

    def convertProxy(v: C): PyValue = {
      CPythonInterpreter.createListProxy(ev(v), elemConvertable.convertProxy)
    }
  }

  implicit def writableSeqElem[T](implicit writer: Writer[T]): ConvertableToSeqElem[T] = new ConvertableToSeqElem[T] {
    def convertCopy(v: T): Platform.Pointer = writer.writeNative(v)
    def convertProxy(v: T): PyValue = writer.write(v)
  }
}
