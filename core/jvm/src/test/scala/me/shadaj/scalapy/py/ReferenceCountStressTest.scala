package me.shadaj.scalapy.py

import org.scalatest.funsuite.AnyFunSuite

class ReferenceCountStressTest extends AnyFunSuite {
  val gc = module("gc")
  val sys = module("sys")
  def referenceCount(obj: Dynamic): Int = sys.getrefcount(obj).as[Int]

  test("Repeated global function calls maintain constant reference counts") {
    val slice = global.slice(0)
    val baseCount = referenceCount(slice)
    (0 to 10000).foreach { _ =>
      System.gc()
      System.runFinalization()
      gc.collect()

      val count = referenceCount(slice)
      assert(baseCount == count)
    }
  }
}
