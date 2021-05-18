import me.shadaj.scalapy.py
import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.py.SeqConverters

object SumPythonProxyBenchmark extends communitybench.Benchmark {
  PyValue.disableAllocationWarning()
  
  var pythonSeq: py.Any = null

  def run(input: String): Double = py.local {
    val backToScala = pythonSeq.as[Seq[Double]]
    backToScala.sum
  }

  override def main(args: Array[String]): Unit = {
    val values = Array.fill(args.last.toInt)(math.random)
    pythonSeq = values.toPythonProxy
    super.main(args.init)
  }
}
