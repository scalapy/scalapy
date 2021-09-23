package me.shadaj.scalapy.py

import org.scalatest.funsuite.AnyFunSuite


class ErrorTest extends AnyFunSuite {
  test("Gets exception when running Python fails") {
    local {
      assertThrows[PythonException] {
        py"123[0]"
      }
    }
  }

  test("Throwing a SyntaxError") {
    local {
      val exp = intercept[PythonException](py"(1 2)")
      assert(exp.getMessage.contains("SyntaxError"))
    }
  }

  test("Throwing a ZeroDivisionError") {
    local {
      val exp = intercept[PythonException](py"1/0")
      assert(exp.getMessage.contains("ZeroDivisionError"))
    }
  }

  test("Throwing a AttributeError") {
    local {
      val exp = intercept[PythonException](py"[1].fake_attr")
      assert(exp.getMessage.contains("AttributeError"))
    }
  }

  test("Throwing a NameError when accessing a non-existent global variable") {
    local {
      val exp = intercept[PythonException](Dynamic.global.xyz)
      assert(exp.getMessage.contains("NameError"))
    }
  }

  test("Throwing a NameError when accessing an attribute of a non-existent global variable") {
    local {
      val exp = intercept[PythonException](Dynamic.global.xyz.abc)
      assert(exp.getMessage.contains("NameError"))
    }
  }

  test("Throwing a NameError when calling a non-existent global function") {
    local {
      val exp = intercept[PythonException](Dynamic.global.xyz())
      assert(exp.getMessage.contains("NameError"))
    }
  }
}
