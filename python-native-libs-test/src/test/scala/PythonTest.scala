import ai.kien.python.Python
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.{PyQuote, SeqConverters}
import org.scalatest.funsuite.AnyFunSuite

class PythonTest extends AnyFunSuite {
  Python().scalapyProperties.get.foreach { case (k, v) => System.setProperty(k, v) }

  test("ScalaPy runs successfully") {
    py.Dynamic.global.list(Seq(1, 2, 3).toPythonCopy)
    py"'Hello from ScalaPy!'"
  }
}
