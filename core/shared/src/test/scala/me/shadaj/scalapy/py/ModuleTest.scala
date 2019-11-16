package me.shadaj.scalapy.py

import org.scalatest.funsuite.AnyFunSuite

@native trait StringModuleFacade extends Object {
  def digits: String = native
}

class ModuleTest extends AnyFunSuite {
  test("Can read value from module") {
    local {
      assert(module("string").digits.as[String] == "0123456789")
    }
  }

  test("Can convert to facade and call methods") {
    local {
      assert(module("string").as[StringModuleFacade].digits == "0123456789")
    }
  }
}
