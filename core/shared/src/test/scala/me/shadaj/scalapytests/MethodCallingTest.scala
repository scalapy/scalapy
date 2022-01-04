package me.shadaj.scalapytests

import me.shadaj.scalapy.py._

import me.shadaj.scalapy.interpreter

import org.scalatest.funsuite.AnyFunSuite

class MethodCallingTest extends AnyFunSuite {
  test("Can access global values") {
    local {
      assert(Dynamic.global.selectDynamic("True").as[Boolean])
    }
  }

  test("Can call global len with Scala sequence") {
    local {
      assert(Dynamic.global.len(Seq(1, 2, 3).toPythonProxy).as[Int] == 3)
    }
  }

  test("Can call dynamic + on integers") {
    local {
      val num1 = py"1"
      val num2 = py"2"
      assert((num1.as[Dynamic] + num2).as[Int] == 3)
    }
  }

  test("Can call dynamic / on integers") {
    local {
      val num1 = py"4"
      val num2 = py"2"
      assert((num1.as[Dynamic] / num2).as[Double] == 2)
    }
  }

  test("Can select value property on an object") {
    try {
      val obj = py""
      val other = obj.value
    } catch {
      case e: Throwable => // compile-only test
    }
  }

  test("Can mix positional and keyword arguments of global functions") {
    local {
      val numbers = py"[3, 2, 1]"
      val sorted = Dynamic.global.sorted(numbers, reverse=py"True")
      assert(sorted.as[Seq[Int]] == Seq(3, 2, 1))
    }
  }

  test("Can mix positional and keyword arguments of methods") {
    local {
      val numbers = py"[3, 2, 1]"
      numbers.as[Dynamic].sort(reverse=py"True")
      assert(numbers.as[Seq[Int]] == Seq(3, 2, 1))
    }
  }

  test("Can call object facade methods") {
    local {
      assert(py"'abcdef'".as[StringObjectFacade].replace("bc", "12") == "a12def")
    }
  }
}
