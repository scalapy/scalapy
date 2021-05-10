package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter

import org.scalatest.funsuite.AnyFunSuite

class SpecialSyntaxTest extends AnyFunSuite {
  test("Can use with statement with file object") {
    local {
      val opened = if (interpreter.Platform.isNative) {
        Dynamic.global.open("./README.md", "r")
      } else Dynamic.global.open("../../README.md", "r")
      `with`(opened) { file =>
        assert(file.as[Dynamic].encoding.as[String] == "UTF-8")
      }
    }
  }

  test("Can select and update elements of a list dynamically") {
    local {
      val myList = py"[1, 2, 3]"
      assert(myList.bracketAccess(1).as[Int] == 2)
    }
  }

  test("Can select, update, and delete elements of a dictionary dynamically") {
    local {
      val myDict = Dynamic.global.dict()
      myDict.bracketUpdate("hello", "world")
      assert(myDict.bracketAccess("hello").as[String] == "world")
      myDict.bracketDelete("hello")
      assert(Dynamic.global.len(myDict).as[Int] == 0)
    }
  }

  test("Can imperatively delete a reference to a value") {
    local {
      val types = module("types")
      val myClass = types.new_class("MyClass")
      
      val weakref = module("weakref")
      val value =  myClass()
      var cleaned = false
      val reference = weakref.ref(value, (_: Any) => {
        cleaned = true
      })

      assert(!cleaned)
      value.del()
      assert(cleaned)
    }
  }
}
