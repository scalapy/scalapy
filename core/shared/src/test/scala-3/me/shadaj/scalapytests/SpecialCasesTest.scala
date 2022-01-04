package me.shadaj.scalapytests

import me.shadaj.scalapy.py._
import org.scalatest.funsuite.AnyFunSuite

class SpecialCasesTest extends AnyFunSuite {
  test("Can apply lambda function as argument to the facade method") {
    local {
      val myList: Seq[Int] = Seq(1,2,3)
      val result = module("functools").as[ReduceFacade].reduce((acc: Int, value: Int) => acc+value, myList.toPythonProxy, 0)
      assert(result == 6)
    }
  }
}
