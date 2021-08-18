package me.shadaj.scalapy.py

import org.scalatest.funsuite.AnyFunSuite

@native class StringModuleFacade extends Module {
  def digits: String = native
}

@native object StringModuleStaticFacade extends StringModuleFacade with StaticModule("string")

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

  test("Can call methods on static facade") {
    local {
      assert(StringModuleStaticFacade.digits == "0123456789")
    }
  }

  test("Can import module attributes through subname") {
    local {
      assert(module("string", "digits").as[String] == "0123456789")
    }
  }
}
