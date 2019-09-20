package me.shadaj.scalapy.py

import org.scalatest.FunSuite

class ErrorTest extends FunSuite {
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
}

