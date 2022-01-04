package me.shadaj.scalapytests

import me.shadaj.scalapy.py._

import org.scalatest.funsuite.AnyFunSuite

class WriterTest extends AnyFunSuite {
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

  test("Writing an empty sequence as a proxy") {
    local {
      assert(Dynamic.global.list(Seq.empty[Int].toPythonProxy).toString == "[]")
    }
  }

  test("Writing a sequence of ints as a proxy") {
    local {
      assert(Dynamic.global.list(Seq[Int](1, 2, 3).toPythonProxy).toString == "[1, 2, 3]")
    }
  }

  test("Writing a sequence of doubles as a proxy") {
    local {
      assert(Dynamic.global.list(Seq[Double](1.1, 2.2, 3.3).toPythonProxy).toString == "[1.1, 2.2, 3.3]")
    }
  }

  test("Writing a sequence of strings as a proxy") {
    local {
      assert(Dynamic.global.list(Seq[String]("hello", "world").toPythonProxy).toString == "['hello', 'world']")
    }
  }

  test("Writing a sequence of arrays as a copy") {
    local {
      assert(Dynamic.global.list(
        Dynamic.global.map(
          Dynamic.global.list,
          Seq[Array[Int]](Array(1), Array(2)).toPythonCopy
        )
      ).toString == "[[1], [2]]")
    }
  }

  test("Writing a sequence of arrays as a proxy") {
    local {
      val seq = Seq[Array[Int]](Array(1), Array(2))
      val proxy = seq.toPythonProxy
      assert(Dynamic.global.list(
        Dynamic.global.map(Dynamic.global.list, proxy)
      ).toString == "[[1], [2]]")

      seq(0)(0) = 100

      assert(Dynamic.global.list(
        Dynamic.global.map(Dynamic.global.list, proxy)
      ).toString == "[[100], [2]]")
    }
  }

  test("Writing a sequence of sequences as a copy") {
    local {
      assert(Dynamic.global.list(
        Dynamic.global.map(
          Dynamic.global.list,
          Seq[Seq[Int]](Seq(1), Seq(2)).toPythonCopy
        )
      ).toString == "[[1], [2]]")
    }
  }

  test("Writing a sequence of Python objects as a copy preserves original object elements") {
    local {
      val objects = Any.from(Seq[Any](py"object()", py"object()").toPythonCopy)
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
      assert(Dynamic.global.tuple(tuple).toString == "(1, 2)")
    }
  }
}
