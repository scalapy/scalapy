package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter.CPythonInterpreter._
import me.shadaj.scalapy.interpreter.{CPythonAPI, CPythonInterpreter, Platform, PyValue}
import me.shadaj.scalapy.readwrite.Writer

object SequenceProxy {
  private val seqProxyClass =
    CPythonInterpreter.withGil {
    Platform.Zone { implicit zone =>
      val ptr = CPythonAPI.PyRun_String(
        Platform.toCString(
          """import collections.abc
            |class SequenceProxy(collections.abc.Sequence):
            |  def __init__(self, len_fn, get_fn):
            |    self.len_fn = len_fn
            |    self.get_fn = get_fn
            |  def __len__(self):
            |    return self.len_fn()
            |  def __getitem__(self, idx):
            |    return self.get_fn(idx)""".stripMargin
        ),
        257,
        globals,
        globals
      )

      throwErrorIfOccured()

      selectGlobal("SequenceProxy", safeGlobal = true)
    }
  }

  def createListProxy[T](seq: scala.collection.Seq[T])(implicit elemWriter: Writer[T]): PyValue = {
    call(seqProxyClass, Seq(
      createLambda(_ => valueFromLong(seq.size)),
      createLambda(args => {
        val index = args(0).getLong.toInt
        if (index < seq.size) {
          Writer.write(seq(index))
        } else {
          throw new IndexError(s"Scala sequence proxy index out of range: $index")
        }
      })
    ), Seq())
  }
}
