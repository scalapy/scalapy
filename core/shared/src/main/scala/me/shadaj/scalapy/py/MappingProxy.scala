package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter.CPythonInterpreter._
import me.shadaj.scalapy.interpreter.{CPythonAPI, CPythonInterpreter, Platform, PyValue}
import me.shadaj.scalapy.readwrite.{Reader, Writer}

object MappingProxy {
  private val mappingProxyClass =
    CPythonInterpreter.withGil {
      Platform.Zone { implicit zone =>

        CPythonAPI.PyRun_String(
          Platform.toCString(
            """import collections.abc
              |class MappingProxy(collections.abc.Mapping):
              |  def __init__(self, len_fn, get_fn, iter_fn):
              |    self.len_fn = len_fn
              |    self.get_fn = get_fn
              |    self.iter_fn = iter_fn
              |
              |  def __len__(self):
              |    return self.len_fn()
              |
              |  def __getitem__(self, key):
              |    return self.get_fn(key)
              |
              |  def __iter__(self):
              |    (has_next_fn, next_fn) = self.iter_fn()
              |    return MappingProxyIterator(has_next_fn, next_fn)
              |
              |class MappingProxyIterator():
              |  def __init__(self, has_next_fn, next_fn):
              |    self.has_next_fn = has_next_fn
              |    self.next_fn = next_fn
              |
              |  def __next__(self):
              |    if self.has_next_fn():
              |       return self.next_fn()
              |    else:
              |       raise StopIteration
              |
              |""".stripMargin
          ),
          257,
          globals,
          globals
        )

        throwErrorIfOccured()

        selectGlobal("MappingProxy", safeGlobal = true)
      }
    }

  def createMapProxy[K: Writer: Reader, V: Writer](data: scala.collection.Map[K, V]): PyValue = {
    call(mappingProxyClass, Seq(
      createLambda(_ => valueFromLong(data.size)),
      createLambda{args =>
        val key = implicitly[Reader[K]].read(args(0))
        val value = data(key)
        Writer.write(value)
      },
      createLambda{_ =>
        val iterator = data.keysIterator
        val hasNext = createLambda(_ => Writer.write(iterator.hasNext))
        val next = createLambda(_ => Writer.write(iterator.next()))
        createTuple(Seq(hasNext, next))
      }
    ), Seq())
  }
}
