package me.shadaj.scalapy.py

import java.util

import org.scalatest.{FunSuite, BeforeAndAfterAll}

import scala.collection.JavaConverters._

class ObjectWriterTest extends FunSuite with BeforeAndAfterAll {
  test("Writing a none value") {
    local {
      assert(Any.from(None).toString == "None")
    }
  }

  test("Writing a boolean") {
    local {
      assert(Any.from(false).toString == "False")
      assert(Any.from(true).toString == "True")
    }
  }

  test("Writing a byte") {
    local {
      assert(Any.from(5.toByte).toString == "5")
    }
  }

  test("Writing an integer") {
    local {
      assert(Any.from(123).toString == "123")
    }
  }

  test("Writing a long") {
    local {
      assert(Any.from(Long.MaxValue).toString == Long.MaxValue.toString)
    }
  }

  test("Writing a float") {
    local {
      assert(Any.from(123.123f).toString.take(7) == "123.123") // floating point error
    }
  }

  test("Writing a double") {
    local {
      assert(Any.from(123.123d).toString.take(7) == "123.123") // floating point error
    }
  }

  test("Writing a string") {
    local {
      assert(Any.from("hello world!").toString == "hello world!")
    }
  }

  test("Writing a union") {
    local {
      assert(Any.from("hello world!": String | Int).toString == "hello world!")
      assert(Any.from(123: String | Int).toString == "123")
    }
  }

  test("Writing an empty sequence") {
    local {
      assert(global.list(Any.from(Seq.empty[Int])).toString == "[]")
    }
  }

  test("Writing a sequence of ints") {
    local {
      assert(global.list(Any.from(Seq[Int](1, 2, 3))).toString == "[1, 2, 3]")
    }
  }

  test("Writing a sequence of doubles") {
    local {
      assert(global.list(Any.from(Seq[Double](1.1, 2.2, 3.3))).toString == "[1.1, 2.2, 3.3]")
    }
  }

  test("Writing a sequence of strings") {
    local {
      assert(global.list(Any.from(Seq[String]("hello", "world"))).toString == "['hello', 'world']")
    }
  }

  test("Writing a sequence of arrays") {
    local {
      assert(global.list(
        global.map(
          global.list,
          Any.from(Seq[Array[Int]](Array(1), Array(2)))
        )
      ).toString == "[[1], [2]]")
    }
  }

  test("Writing a sequence of sequences") {
    local {
      assert(global.list(
        global.map(
          global.list,
          Any.from(Seq[Seq[Int]](Seq(1), Seq(2)))
        )
      ).toString == "[[1], [2]]")
    }
  }

  test("Sequences of sequences are natively writable") {
    local {
      assert(implicitly[ObjectWriter[Seq[Seq[Int]]]].write(Seq(Seq(1), Seq(2))).isLeft)
    }
  }

  test("Writing a sequence of Python objects preserves original objects") {
    local {
      val objects = Any.from(Seq[Any](py"object()", py"object()"))
      assert(py"type($objects[0])".toString == "<class 'object'>")
    }
  }

  test("Writing a map of int to int") {
    local {
      val written = Any.from(Map(1 -> 2, 2 -> 3))
      assert(written.toString == "{1: 2, 2: 3}")
    }
  }

  test("Writing a tuple") {
    local {
      val tuple = Any.from((1, 2))
      assert(py"type($tuple)".toString == "<class 'tuple'>")
      assert(global.tuple(tuple).toString == "(1, 2)")
    }
  }
}
