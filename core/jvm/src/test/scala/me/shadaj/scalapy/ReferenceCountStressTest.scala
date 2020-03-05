package me.shadaj.scalapy.py

import org.scalatest.funsuite.AnyFunSuite

class ReferenceCountStressTest extends AnyFunSuite {
  // val gc = py.module("gc")
  // val sys = py.module("sys")
  // def referenceCount(obj: py.Dynamic): Int = sys.getrefcount(obj).as[Int]

  test("Repeated global function calls maintain constant reference counts") {
    println("ComputePython...")

    val gc = module("gc")
    val sys = module("sys")

    def referenceCounts(obj: Dynamic): (Int, Int) =
      (sys.getrefcount(obj).as[Int], global.len(gc.get_referrers(obj)).as[Int])

    val s0 = global.slice(0)
      (0 to 2).foreach { index =>
        System.gc()
        System.runFinalization()
        gc.collect()
        println(s"$index : counts: ${referenceCounts(s0)}")
      }

    System.gc()
    System.runFinalization()
    gc.collect()
    println(s"counts: ${referenceCounts(s0)}")

    // py.local  {
    //   val sliceBase = py.global.slice(0)
    //   val baseCount = referenceCount(sliceBase)

    //   (0 to 4).foreach { _ =>
    //     System.gc()
    //     System.runFinalization()
    //     gc.collect()

    //     val slice = py.global.slice(0)
    //     val count = referenceCount(slice)

    //     assert(baseCount == count)
    //   }
    // }
  }
}
