package me.shadaj.scalapy.py

import org.scalatest.{FunSuite, BeforeAndAfterAll}

class ObjectReaderTest extends FunSuite with BeforeAndAfterAll {
  test("Reading a boolean") {
    local {
      assert(Object("False").as[Boolean] == false)
      assert(Object("True").as[Boolean] == true)
    }
  }

  test("Reading a byte") {
    local {
      assert(Object("5").as[Byte] == 5.toByte)
    }
  }

  test("Reading an integer") {
    local {
      assert(Object("123").as[Int] == 123)
    }
  }

  test("Reading a long") {
    local {
      assert(Object(s"${Long.MaxValue}").as[Long] == Long.MaxValue)
    }
  }

  test("Reading a float") {
    local {
      assert(Object("123.123").as[Float] == 123.123f)
    }
  }

  test("Reading a double") {
    local {
      assert(Object("123.123").as[Double] == 123.123d)
    }
  }

  test("Reading a string") {
    local {
      assert(Object("'hello world!'").as[String] == "hello world!")
    }
  }

  test("Reading an empty sequence") {
    local {
      assert(Object("[]").as[Seq[Int]].isEmpty)
    }
  }

  test("Reading a sequence of ints") {
    local {
      assert(Object("[1, 2, 3]").as[Seq[Int]] == Seq(1, 2, 3))
    }
  }

  test("Reading a sequence of doubles") {
    local {
      assert(Object("[1.1, 2.2, 3.3]").as[Seq[Double]] == Seq(1.1, 2.2, 3.3))
    }
  }

  test("Reading a sequence of strings") {
    local {
      assert(Object("['hello', 'world']").as[Seq[String]] == Seq("hello", "world"))
    }
  }

  test("Reading a sequence of sequences") {
    local {
      assert(Object("[[1], [2]]").as[Seq[Seq[Int]]] == Seq(Seq(1), Seq(2)))
    }
  }

  test("Reading a sequence of objects preserves original object") {
    local {
      val datetimeExpr = module("datetime").moduleName
      val datesSeq = Object(s"[$datetimeExpr.date.today(), $datetimeExpr.date.today().replace(year = 1000)]").as[Seq[Object]]
      assert(datesSeq.head.asDynamic.year.as[Int] > 2000)
      assert(datesSeq.last.asDynamic.year.as[Int] == 1000)
    }
  }

  test("Reading a map of int to int") {
    local {
      val read = Object("{ 1: 2, 2: 3 }").as[Map[Int, Int]]
      assert(read(1) == 2)
      assert(read(2) == 3)
    }
  }

  test("Reading a tuple") {
    local {
      assert(Object("(1, 2)").as[(Int, Int)] == (1, 2))
    }
  }
}
