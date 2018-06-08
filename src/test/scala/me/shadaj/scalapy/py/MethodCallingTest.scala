package me.shadaj.scalapy.py

import jep.Jep
import org.scalatest.FunSuite

class MethodCallingTest extends FunSuite {
  implicit val jep = new Jep()

  test("Can call global len with Scala sequence") {
    assert(global.len(Seq(1, 2, 3)).to[Int] == 3)
  }
}
