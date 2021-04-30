import me.shadaj.scalapy.py
import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.py.SeqConverters

object CreatePythonCopyBenchmark extends communitybench.Benchmark {
  PyValue.disableAllocationWarning()
  
  var values: Vector[Double] = null
  def run(input: String): Int = py.local {
    val pySequence = values.toPythonCopy
    py.Dynamic.global.len(pySequence).as[Int]
  }

  override def main(args: Array[String]): Unit = {
    values = Vector.fill(args.last.toInt)(math.random)
    super.main(args.init)
  }
}
