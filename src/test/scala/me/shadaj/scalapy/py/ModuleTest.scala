package me.shadaj.scalapy.py

import jep.Jep
import org.scalatest.{FunSuite, BeforeAndAfterAll}

class StringModuleFacade(o: Object)(implicit jep: Jep) extends ObjectFacade(o) {
  def digits: String = native
}

class ModuleTest extends FunSuite with BeforeAndAfterAll {
  implicit val jep = new Jep()

  test("Can read value from module") {
    assert(module("string").digits.as[String] == "0123456789")
  }

  test("Can convert to facade and call methods") {
    assert(module("string").as[StringModuleFacade].digits == "0123456789")
  }

  override def afterAll = jep.close()
}
