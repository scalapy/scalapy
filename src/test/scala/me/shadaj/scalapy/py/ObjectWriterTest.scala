package me.shadaj.scalapy.py

import java.util

import jep.Jep
import org.scalatest.FunSuite

class ObjectWriterTest extends FunSuite {
  implicit val jep = new Jep()

  test("Writing a none value") {
    assert(jep.getValue(Object.from(None).expr) == null)
    assert(Object.from(None).toString == "None") // make sure it is actually a Python `None`
  }

  test("Writing a boolean") {
    assert(jep.getValue(Object.from(false).expr) == false)
    assert(jep.getValue(Object.from(true).expr) == true)
  }

  test("Writing a byte") {
    assert(jep.getValue(Object.from(5.toByte).expr) == 5.toByte)
  }

  test("Writing an integer") {
    assert(jep.getValue(Object.from(123).expr) == 123)
  }

  test("Writing a long") {
    assert(jep.getValue(Object.from(Long.MaxValue).expr) == Long.MaxValue)
  }

  test("Writing a float") {
    assert(jep.getValue(Object.from(123.123f).expr) == 123.123f)
  }

  test("Writing a double") {
    assert(jep.getValue(Object.from(123.123d).expr) == 123.123d)
  }

  test("Writing a string") {
    assert(jep.getValue(Object.from("hello world!").expr) == "hello world!")
  }

  test("Writing a union") {
    assert(jep.getValue(Object.from("hello world!": String | Int).expr) == "hello world!")
    assert(jep.getValue(Object.from(123: String | Int).expr) == 123)
  }

  test("Writing an empty sequence") {
    assert(jep.getValue(Object.from(Seq.empty[Int]).expr).asInstanceOf[Array[Int]].length == 0)
  }

  test("Writing a sequence of ints") {
    assert(jep.getValue(Object.from(Seq[Int](1, 2, 3)).expr).asInstanceOf[Array[Int]].toSeq == Seq(1, 2, 3))
  }

  test("Writing a sequence of doubles") {
    assert(jep.getValue(Object.from(Seq[Double](1.1, 2.2, 3.3)).expr).asInstanceOf[Array[Double]].toSeq == Seq(1.1, 2.2, 3.3))
  }

  test("Writing a sequence of strings") {
    assert(jep.getValue(Object.from(Seq[String]("hello", "world")).expr).asInstanceOf[Array[String]].toSeq == Seq("hello", "world"))
  }

  test("Writing a sequence of arrays") {
    assert(jep.getValue(Object.from(Seq[Array[Int]](Array(1), Array(2))).expr).asInstanceOf[Array[Array[Int]]].map(_.toSeq).toSeq == Seq(Seq(1), Seq(2)))
  }

  test("Writing a sequence of sequences") {
    assert(jep.getValue(Object.from(Seq[Seq[Int]](Seq(1), Seq(2))).expr).asInstanceOf[Array[java.lang.Object]].map(_.asInstanceOf[Array[Int]].toSeq)
      .toSeq == Seq(Seq(1), Seq(2)))
  }

  test("Writing a sequence of Python objects preserves original objects") {
    val objectsExpr = Object.from(Seq[Object](Object("object()"), Object("object()"))).expr
    assert(jep.getValue(s"str(type($objectsExpr[0]))").asInstanceOf[String] == "<class 'object'>")
  }

  test("Writing a map of int to int") {
    val written = jep.getValue(Object.from(Map(1 -> 2, 2 -> 3)).expr).asInstanceOf[java.util.Map[Int, Int]]
    assert(written.get(1) == 2)
    assert(written.get(2) == 3)
  }

  test("Writing a tuple") {
    val tupleValue = jep.getValue(Object.from((1, 2)).expr)
    assert(tupleValue.asInstanceOf[util.List[Any]].toArray.toSeq == Seq(1, 2))
  }
}
