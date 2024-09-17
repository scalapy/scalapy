package me.shadaj.scalapytests

import org.scalatest.funsuite.AnyFunSuite
import me.shadaj.scalapy.py._
import me.shadaj.scalapy.readwrite._
import ProxyWriter._
import me.shadaj.scalapy.readwrite

import scala.collection.mutable

class ProxyWriterTest extends AnyFunSuite {
  test("Maps can be proxied as a python Mapping"){
    val data: Map[Int, String] = Map(1 -> "hello", 2 -> "world")

    // Assuming these instances exists. For better debugging
    implicitly[Reader[Int]]
    implicitly[Writer[Int]]
    implicitly[Writer[String]]
    implicitly[ProxyWriter[Map[Int, String]]]

    val proxy = data.toPythonProxy

    assert(py"$proxy[1]".toString == "hello")
    assert(py"$proxy[2]".toString == "world")
    assertThrows[PythonException](py"$proxy[3]")
    assert(py"len(${proxy})".as[Int] == 2)
    assert(py"${proxy}[1]".as[String] == "hello")
    assert(py"{1: 'hello', 2: 'world'}.keys()".as[Set[Int]] == data.keySet)
    assert(py"dict($proxy).keys()".as[Set[Int]] == data.keySet)
    assert(py"{1: 'hello', 2: 'world'} == dict($proxy)".as[Boolean])
  }

  test("Sequences can be proxied as a python Sequence"){
    val data: Seq[Double] = Seq(1.1, 2.2, 3.3, 4.4)

    // Assuming these instances exists. For better debugging
    implicitly[Writer[Double]]
    implicitly[ProxyWriter[Seq[Double]]]

    val proxy = data.toPythonProxy

    assert(py"$proxy[0]".as[Double] == 1.1)
    assert(py"$proxy[1]".as[Double] == 2.2)
    assertThrows[PythonException](py"$proxy[5]")
    assert(py"len($proxy)".as[Int] == 4)
    assert(py"sum($proxy)".as[Double] == data.sum)
    assert(py"list($proxy) == [1.1, 2.2, 3.3, 4.4]".as[Boolean])
    assert(py"type($proxy)".toString == "<class 'SequenceProxy'>")
  }

  test("Map proxies can be recursive with sequences") {
    val data: Map[Int, Seq[Int]] = Map(13 -> (1 to 100))

    val proxy = data.toPythonProxy

    assert(py"type($proxy)".toString == "<class 'MappingProxy'>")
    assert(py"type($proxy[13])".toString == "<class 'SequenceProxy'>")

  }

  test("Map proxies can be recursive with other maps") {
    val data: Map[Int, Map[String, Double]] = Map(1 -> Map("1" -> 1.3))
    val proxy = data.toPythonProxy

    assert(py"type($proxy)".toString == "<class 'MappingProxy'>")
    assert(py"type($proxy[1])".toString == "<class 'MappingProxy'>")
    assert(py"$proxy[1]['1']".as[Double] == 1.3)
  }

  test("Seq proxies can be recursive with other sequences") {
    val data: Seq[Seq[Double]] = Seq(Seq.empty, Seq(1.1, 2.2), Seq(3.3))

    val proxy = data.toPythonProxy

    assert(py"type($proxy)".toString == "<class 'SequenceProxy'>")
    assert(py"type($proxy[0])".toString == "<class 'SequenceProxy'>")
    assert(py"map(len, $proxy)".as[Seq[Int]] == Seq(0, 2, 1))
    assert(py"map(sum, $proxy)".as[Seq[Double]] == data.map(_.sum))
  }

  test("Seq proxies can be recursive with maps") {
    val data: Seq[Map[Int, String]] = Seq(Map(1 -> "hello"), Map.empty, Map(2 -> "world"))

    val proxy = data.toPythonProxy

    assert(py"type($proxy)".toString == "<class 'SequenceProxy'>")
    assert(py"type($proxy[0])".toString == "<class 'MappingProxy'>")
    assert(py"$proxy[0][1]".as[String] == "hello")
    assert(py"len($proxy[1])".as[Int] == 0)
    assert(py"$proxy[2][2]".as[String] == "world")
  }

  test("mutable maps can be proxied") {
    val mutMap: mutable.Map[Int, String] = mutable.Map.empty

    val proxy = mutMap.toPythonProxy

    assert(py"len($proxy)".as[Int] == 0)

    mutMap += (1 -> "hello")
    assert(mutMap.size == 1)
    assert(py"len($proxy)".as[Int] == 1)
    assert(py"$proxy[1]".as[String] == "hello")

    mutMap.remove(1)
    assert(py"len($proxy)".as[Int] == 0)
  }

  test("mutable sequences can be proxied") {
    val mutSeq: mutable.Seq[Int] = mutable.Seq.fill(100)(-1)

    val proxy = mutSeq.toPythonProxy

    assert(py"len($proxy)".as[Int] == 100)

    mutSeq.update(0, 1337)
    assert(py"$proxy[0]".as[Int] == 1337)
    assert(py"$proxy[90]".as[Int] == -1)

    mutSeq.update(90, 1338)
    assert(py"$proxy[90]".as[Int] == 1338)
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
}
