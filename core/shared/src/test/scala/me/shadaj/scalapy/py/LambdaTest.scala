package me.shadaj.scalapy.py

import org.scalatest.funsuite.AnyFunSuite

import me.shadaj.scalapy.py.interpreter.CPythonInterpreter
import me.shadaj.scalapy.py.interpreter.PyValue

class LambdaTest extends AnyFunSuite {
  test("Calls to Python proxy to Scala lambda have correct results") {
    local {
      var count = 0
      val testLambda = Any.populateWith(CPythonInterpreter.createLambda0(() => {
        count += 1
        (s"count: $count": Any).value
      }))

      assert(py"$testLambda()".as[String] == "count: 1")
      assert(py"$testLambda()".as[String] == "count: 2")
    }
  }

  test("Create list proxy") {
    local {
      val mySeq = Seq(CPythonInterpreter.valueFromLong(1), CPythonInterpreter.valueFromLong(2), CPythonInterpreter.valueFromLong(3))
      val proxy = Any.populateWith(CPythonInterpreter.createListProxy[PyValue](mySeq, identity))
      assert(py"len($proxy)".as[Int] == 3)
      assert(py"$proxy[0]".as[Int] == 1)
      assert(py"$proxy[1]".as[Int] == 2)
      assert(py"$proxy[2]".as[Int] == 3)
    }
  }
}
