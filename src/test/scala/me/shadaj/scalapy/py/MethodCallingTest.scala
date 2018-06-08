package me.shadaj.scalapy.py

import jep.Jep
import org.scalatest.FunSuite

class StringObjectFacade(obj: Object)(implicit jep: Jep) extends ObjectFascade(obj) {
  def replace(old: String, newValue: String): String = native
}

class MethodCallingTest extends FunSuite {
  implicit val jep = new Jep()

  test("Can call global len with Scala sequence") {
    assert(global.len(Seq(1, 2, 3)).to[Int] == 3)
  }

  test("Can call dynamic + on integers") {
    val num1 = Object("1")
    val num2 = Object("2")
    assert((num1.asInstanceOf[DynamicObject] + num2).to[Int] == 3)
  }

  test("Can call object facade methods") {
    assert(Object.from("abcdef").as[StringObjectFacade].replace("bc", "12") == "a12def")
  }
}
