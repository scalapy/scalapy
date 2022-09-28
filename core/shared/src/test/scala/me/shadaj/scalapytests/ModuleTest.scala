package me.shadaj.scalapytests

import me.shadaj.scalapy.py._

import org.scalatest.funsuite.AnyFunSuite

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

  test("Calling an empty parens facade function does not read attributes") {
    local {
      assertThrows[PythonException] {
        StringModuleStaticFacade.ascii_letters()
      }
    }
  }

  test("Can import module attributes through subname") {
    local {
      assert(module("string", "digits").as[String] == "0123456789")
    }
  }
}
