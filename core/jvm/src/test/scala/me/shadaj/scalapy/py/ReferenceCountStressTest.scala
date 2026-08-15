package me.shadaj.scalapy.py

import org.scalatest.funsuite.AnyFunSuite

class ReferenceCountStressTest extends AnyFunSuite {
  val gc = module("gc")
  val sys = module("sys")
  def referenceCount(obj: Dynamic): Int = sys.getrefcount(obj).as[Int]

  def assertReferenceCountSettles(obj: Dynamic, to: Int) = {
    // add 1 because getrefcount takes one reference during exectuion
    assert((1 to 500).exists { _ =>
      // make sure any reference count changes that need to happen do happen
      System.gc()
      System.runFinalization()
      gc.collect()

      referenceCount(obj) == to + 1
    })
  }

  test("Repeated global function calls maintain constant reference counts") {
    val slice = Dynamic.global.slice(0)
    assertReferenceCountSettles(slice, 1)

    (1 to 500).foreach { i =>
      val myNewSlice = Dynamic.global.slice(0)
      assertReferenceCountSettles(slice, 1)
    }
  }

  test("Repeated bracket access on a list maintains constant reference counts") {
    val element = Dynamic.global.slice(0)

    val list = Dynamic.global.list()
    list.append(element)
    // one reference held by `element` itself, one by the list
    assertReferenceCountSettles(element, 2)

    (1 to 500).foreach { _ =>
      list.bracketAccess(0)
    }

    assertReferenceCountSettles(element, 2)
  }

  test("Repeated bracket access on a dict maintains constant reference counts") {
    val element = Dynamic.global.slice(0)

    val dict = Dynamic.global.dict()
    dict.bracketUpdate("key", element)
    // one reference held by `element` itself, one by the dict
    assertReferenceCountSettles(element, 2)

    (1 to 500).foreach { _ =>
      dict.bracketAccess("key")
    }

    assertReferenceCountSettles(element, 2)
  }
}
