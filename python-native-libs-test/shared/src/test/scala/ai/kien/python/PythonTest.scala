package ai.kien.python

import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.{PyQuote, SeqConverters}

import org.scalatest.funsuite.AnyFunSuite

class PythonTest extends AnyFunSuite with PythonSuite {

  test("ScalaPy runs successfully") {
    py.Dynamic.global.list(Seq(1, 2, 3).toPythonCopy)
    py"'Hello from ScalaPy!'"
  }

  test("Loading package installed in virtualenv") {
    py.module(Config.module)
  }
}
