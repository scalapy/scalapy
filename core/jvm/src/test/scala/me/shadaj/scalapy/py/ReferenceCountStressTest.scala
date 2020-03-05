package me.shadaj.scalapy

import org.scalatest.funsuite.AnyFunSuite

class ReferenceCountStressTest extends AnyFunSuite {
  val gc = py.module("gc")
  val sys = py.module("sys")
  def referenceCount(obj: py.Dynamic): Int = sys.getrefcount(obj).as[Int]

  test("Repeated global function calls maintain constant reference counts") {
    py.local  {
      val sliceBase = py.global.slice(0)
      val baseCount = referenceCount(sliceBase)

      (0 to 4).foreach { _ =>
        System.gc()
        System.runFinalization()
        gc.collect()

        val slice = py.global.slice(0)
        val count = referenceCount(slice)

        assert(baseCount == count)
      }
    }
  }
}
