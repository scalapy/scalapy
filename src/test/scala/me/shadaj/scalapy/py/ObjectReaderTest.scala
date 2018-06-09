package me.shadaj.scalapy.py

import jep.Jep
import org.scalatest.FunSuite

class ObjectReaderTest extends FunSuite {
  implicit val jep = new Jep()

  test("Reading a boolean") {
    assert(Object.from(false).as[Boolean] == false)
    assert(Object.from(true).as[Boolean] == true)
  }

  test("Reading a byte") {
    assert(Object.from(5.toByte).as[Byte] == 5.toByte)
  }

  test("Reading an integer") {
    assert(Object.from(123).as[Int] == 123)
  }

  test("Reading a long") {
    assert(Object.from(Long.MaxValue).as[Long] == Long.MaxValue)
  }

  test("Reading a float") {
    assert(Object.from(123.123f).as[Float] == 123.123f)
  }

  test("Reading a double") {
    assert(Object.from(123.123d).as[Double] == 123.123d)
  }

  test("Reading a string") {
    assert(Object.from("hello world!").as[String] == "hello world!")
  }

  test("Reading an empty sequence") {
    assert(Object.from(Seq.empty[Int]).as[Seq[Int]].isEmpty)
  }

  test("Reading a sequence of ints") {
    assert(Object.from(Seq[Int](1, 2, 3)).as[Seq[Int]] == Seq(1, 2, 3))
  }

  test("Reading a sequence of doubles") {
    assert(Object.from(Seq[Double](1.1, 2.2, 3.3)).as[Seq[Double]] == Seq(1.1, 2.2, 3.3))
  }

  test("Reading a sequence of strings") {
    assert(Object.from(Seq[String]("hello", "world")).as[Seq[String]] == Seq("hello", "world"))
  }

  test("Reading a sequence of arrays") {
    assert(Object.from(Seq[Array[Int]](Array(1), Array(2))).as[Seq[Seq[Int]]] == Seq(Seq(1), Seq(2)))
  }

  test("Reading a sequence of sequences") {
    assert(Object.from(Seq[Seq[Int]](Seq(1), Seq(2))).as[Seq[Seq[Int]]] == Seq(Seq(1), Seq(2)))
  }

  test("Reading a map of int to int") {
    val read = Object.from(Map(1 -> 2, 2 -> 3)).as[Map[Int, Int]]
    assert(read(1) == 2)
    assert(read(2) == 3)
  }

  test("Reading a tuple") {
    assert(Object.from((1, 2)).as[(Int, Int)] == (1, 2))
  }
}
