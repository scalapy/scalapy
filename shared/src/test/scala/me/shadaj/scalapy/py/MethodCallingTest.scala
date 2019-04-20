package me.shadaj.scalapy.py

import org.scalatest.{FunSuite, BeforeAndAfterAll}

trait StringObjectFacade extends ObjectFacade {
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
      val num1 = Object("1")
      val num2 = Object("2")
      assert((num1.asDynamic + num2).as[Int] == 3)
    }
  }

  test("Can call object facade methods") {
    assert(Object.from("abcdef").as[StringObjectFacade].replace("bc", "12") == "a12def")
  }

  test("Can use with statement with file object") {
    local {
      val opened = if (Platform.isNative) {
        global.open("./README.md", "r")
      } else global.open("../README.md", "r")
      `with`(opened) { file =>
        assert(file.asDynamic.encoding.as[String] == "UTF-8")
      }
    }
  }
}
