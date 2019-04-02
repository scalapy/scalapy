package me.shadaj.scalapy.py

import java.util

import org.scalatest.{FunSuite, BeforeAndAfterAll}

import scala.collection.JavaConverters._

class ObjectWriterTest extends FunSuite with BeforeAndAfterAll {
  test("Writing a none value") {
    assert(Object.from(None).toString == "None") // make sure it is actually a Python `None`
  }

  test("Writing a boolean") {
    assert(Object.from(false).value.getBoolean == false)
    assert(Object.from(true).value.getBoolean == true)
  }

  test("Writing a byte") {
    assert(Object.from(5.toByte).value.getLong == 5.toByte)
  }

  test("Writing an integer") {
    assert(Object.from(123).value.getLong == 123)
  }

  test("Writing a long") {
    assert(Object.from(Long.MaxValue).value.getLong == Long.MaxValue)
  }

  test("Writing a float") {
    assert(Object.from(123.123f).value.getDouble == 123.123f)
  }

  test("Writing a double") {
    assert(Object.from(123.123d).value.getDouble == 123.123d)
  }

  test("Writing a string") {
    assert(Object.from("hello world!").value.getString == "hello world!")
  }

  test("Writing a union") {
    assert(Object.from("hello world!": String | Int).value.getString == "hello world!")
    assert(Object.from(123: String | Int).value.getLong == 123)
  }

  test("Writing an empty sequence") {
    assert(Object.from(Seq.empty[Int]).value.getSeq.length == 0)
  }

  test("Writing a sequence of ints") {
    assert(Object.from(Seq[Int](1, 2, 3)).value.getSeq.map(_.getLong) == Seq(1, 2, 3))
  }

  test("Writing a sequence of doubles") {
    assert(Object.from(Seq[Double](1.1, 2.2, 3.3)).value.getSeq.map(_.getDouble) == Seq(1.1, 2.2, 3.3))
  }

  test("Writing a sequence of strings") {
    assert(Object.from(Seq[String]("hello", "world")).value.getSeq.map(_.getString) == Seq("hello", "world"))
  }

  test("Writing a sequence of arrays") {
    assert(Object.from(Seq[Array[Int]](Array(1), Array(2))).value.getSeq.map(_.getSeq.map(_.getLong)) == Seq(Seq(1), Seq(2)))
  }

  test("Writing a sequence of sequences") {
    assert(Object.from(Seq[Seq[Int]](Seq(1), Seq(2))).value.getSeq.map(_.getSeq.map(_.getLong)) == Seq(Seq(1), Seq(2)))
  }

  test("Sequences of sequences are natively writable") {
    assert(implicitly[ObjectWriter[Seq[Seq[Int]]]].write(Seq(Seq(1), Seq(2))).isLeft)
  }

  test("Writing a sequence of Python objects preserves original objects") {
    val objectsExpr = Object.from(Seq[Object](Object("object()"), Object("object()"))).expr
    assert(interpreter.load(s"str(type($objectsExpr[0]))").getString == "<class 'object'>")
  }

  test("Writing a map of int to int") {
    val written = Object.from(Map(1 -> 2, 2 -> 3)).value.getAny.asInstanceOf[java.util.Map[Long, Long]]
    assert(written.get(1L) == 2)
    assert(written.get(2L) == 3)
  }

  test("Writing a tuple") {
    val tupleValue = Object.from((1, 2)).value
    assert(tupleValue.getTuple.map(_.getLong) == Seq(1, 2))
  }
}
