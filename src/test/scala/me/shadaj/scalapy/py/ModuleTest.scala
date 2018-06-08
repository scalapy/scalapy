package me.shadaj.scalapy.py

import jep.Jep
import org.scalatest.FunSuite

class StringModuleFacade(o: Object)(implicit jep: Jep) extends ObjectFascade(o) {
  def digits: String = native
}


class ModuleTest extends FunSuite {
  implicit val jep = new Jep()

  test("Can read value from module") {
    assert(module("string").digits.to[String] == "0123456789")
  }

  test("Can convert to facade and call methods") {
    assert(module("string").as[StringModuleFacade].digits == "0123456789")
  }
}
