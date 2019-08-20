package me.shadaj.scalapy.py

import org.scalatest.{FunSuite, BeforeAndAfterAll}

@native trait StringObjectFacade extends Object {
  def replace(old: String, newValue: String): String = native
}

class MethodCallingTest extends FunSuite with BeforeAndAfterAll {
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
