package me.shadaj.scalapy.py

import org.scalatest.funsuite.AnyFunSuite

import me.shadaj.scalapy.interpreter.CPythonInterpreter
import me.shadaj.scalapy.interpreter.PyValue

class LambdaTest extends AnyFunSuite {
  test("Calls to Python proxy to Scala lambda have correct results") {
    local {
      var count = 0
      val testLambda = Any.from(() => {
        count += 1
        s"count: $count"
      })

      assert(py"$testLambda()".as[String] == "count: 1")
      assert(py"$testLambda()".as[String] == "count: 2")
    }
  }

  test("Calls to Python lambda through read Scala function have correct results") {
    local {
      val lambdaToScala = Dynamic.global.len.as[Any => Int]
      assert(lambdaToScala(Seq[Any]().toPythonProxy) == 0)
      assert(lambdaToScala(Seq(1, 2, 3).toPythonProxy) == 3)
    }
  }
}
