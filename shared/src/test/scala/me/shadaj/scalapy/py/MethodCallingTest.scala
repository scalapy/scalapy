package me.shadaj.scalapy.py

import jep.Jep
import org.scalatest.{FunSuite, BeforeAndAfterAll}

class StringObjectFacade(obj: Object)(implicit jep: Jep) extends ObjectFacade(obj) {
  def replace(old: String, newValue: String): String = native
}

class MethodCallingTest extends FunSuite with BeforeAndAfterAll {
  implicit val jep = new Jep()

  test("Can access global variables") {
    val obj = Object("123")
    assert(global.selectDynamic(obj.expr).as[Int] == 123)
  }

  test("Can call global len with Scala sequence") {
    assert(global.len(Seq(1, 2, 3)).as[Int] == 3)
  }

  test("Can call dynamic + on integers") {
    val num1 = Object("1")
    val num2 = Object("2")
    assert((num1.asInstanceOf[DynamicObject] + num2).as[Int] == 3)
  }

  test("Can call object facade methods") {
    assert(Object.from("abcdef").as[StringObjectFacade].replace("bc", "12") == "a12def")
  }

  test("Can use with statement with file object") {
    `with`(global.open("README.md", "r")) { file =>
      assert(file.asInstanceOf[DynamicObject].encoding.as[String] == "UTF-8")
    }
  }

  override def afterAll = jep.close()
}
