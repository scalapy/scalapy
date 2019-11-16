package me.shadaj.scalapy.py

import Reader._
import org.scalatest.funsuite.AnyFunSuite

@native trait StringObjectFacade extends Object {
  def replace(old: String, newValue: String): String = native
}

class MethodCallingTest extends AnyFunSuite {
  test("Can access global values") {
    local {
      assert(global.selectDynamic("True").as[Boolean] == true)
    }
  }

  test("Can call global len with Scala sequence") {
    local {
      assert(global.len(Seq(1, 2, 3)).as[Int] == 3)
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

  test("Can mix positional and keyword arguments of global functions") {
    local {
      val numbers = py"[3, 2, 1]"
      val sorted = global.sorted(numbers, reverse=py"True")
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

  test("Can use with statement with file object") {
    local {
      val opened = if (Platform.isNative) {
        global.open("./README.md", "r")
      } else global.open("../../README.md", "r")
      `with`(opened) { file =>
        assert(file.as[Dynamic].encoding.as[String] == "UTF-8")
      }
    }
  }
}
