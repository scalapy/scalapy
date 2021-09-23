package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter

import org.scalatest.funsuite.AnyFunSuite

@native trait IntList extends Any {
  @PyBracketAccess
  def apply(index: Int): Int = native

  @PyBracketAccess
  def update(index: Int, newValue: Int): Unit = native
}

trait CrossScalaSpecialSyntaxTest extends AnyFunSuite {
  test("Can access and update the list elements using brackets") {
    local {
      val myList = py"[1, 2, 3]".as[IntList]
      assert(myList(1) == 2)
      myList(1) = 3
      assert(myList(1) == 3)
    }
  }
}
