package me.shadaj.scalapy.py

import java.util

import jep.Jep
import org.scalatest.{FunSuite, BeforeAndAfterAll}

import scala.collection.JavaConverters._

class ObjectWriterTest extends FunSuite with BeforeAndAfterAll {
  implicit val jep = new Jep()

  test("Writing a none value") {
    assert(Object.from(None).value == null)
    assert(Object.from(None).toString == "None") // make sure it is actually a Python `None`
  }

  test("Writing a boolean") {
    assert(Object.from(false).value == false)
    assert(Object.from(true).value == true)
  }

  test("Writing a byte") {
    assert(Object.from(5.toByte).value == 5.toByte)
  }

  test("Writing an integer") {
    assert(Object.from(123).value == 123)
  }

  test("Writing a long") {
    assert(Object.from(Long.MaxValue).value == Long.MaxValue)
  }

  test("Writing a float") {
    assert(Object.from(123.123f).value == 123.123f)
  }

  test("Writing a double") {
    assert(Object.from(123.123d).value == 123.123d)
  }

  test("Writing a string") {
    assert(Object.from("hello world!").value == "hello world!")
  }

  test("Writing a union") {
    assert(Object.from("hello world!": String | Int).value == "hello world!")
    assert(Object.from(123: String | Int).value == 123)
  }

  test("Writing an empty sequence") {
    assert(Object.from(Seq.empty[Int]).value.asInstanceOf[Array[Int]].length == 0)
  }

  test("Writing a sequence of ints") {
    assert(Object.from(Seq[Int](1, 2, 3)).value.asInstanceOf[Array[Int]].toSeq == Seq(1, 2, 3))
  }

  test("Writing a sequence of doubles") {
    assert(Object.from(Seq[Double](1.1, 2.2, 3.3)).value.asInstanceOf[Array[Double]].toSeq == Seq(1.1, 2.2, 3.3))
  }

  test("Writing a sequence of strings") {
    assert(Object.from(Seq[String]("hello", "world")).value.asInstanceOf[Array[String]].toSeq == Seq("hello", "world"))
  }

  test("Writing a sequence of arrays") {
    assert(Object.from(Seq[Array[Int]](Array(1), Array(2))).value.asInstanceOf[Array[Array[Int]]].toSeq.map(_.toSeq) == Seq(Seq(1), Seq(2)))
  }

  test("Writing a sequence of sequences") {
    assert(Object.from(Seq[Seq[Int]](Seq(1), Seq(2))).value.asInstanceOf[Array[java.lang.Object]].toSeq
      .map(_.asInstanceOf[Array[Int]].toSeq) == Seq(Seq(1), Seq(2)))
  }

  test("Sequences of sequences are natively writable") {
    assert(implicitly[ObjectWriter[Seq[Seq[Int]]]].write(Seq(Seq(1), Seq(2))).isLeft)
  }

  test("Writing a sequence of Python objects preserves original objects") {
    val objectsExpr = Object.from(Seq[Object](Object("object()"), Object("object()"))).expr
    assert(jep.getValue(s"str(type($objectsExpr[0]))").asInstanceOf[String] == "<class 'object'>")
  }

  test("Writing a map of int to int") {
    val written = Object.from(Map(1 -> 2, 2 -> 3)).value.asInstanceOf[java.util.Map[Long, Long]]
    assert(written.get(1L) == 2)
    assert(written.get(2L) == 3)
  }

  test("Writing a tuple") {
    val tupleValue = Object.from((1, 2)).value
    assert(tupleValue.asInstanceOf[util.List[Any]].toArray.toSeq == Seq(1, 2))
  }

  override def afterAll = jep.close()
}
