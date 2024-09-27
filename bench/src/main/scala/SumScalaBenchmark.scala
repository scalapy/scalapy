import me.shadaj.scalapy.py
import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.py.AnyConverters

object SumScalaBenchmark extends communitybench.Benchmark {
  PyValue.disableAllocationWarning()
  
  var scalaSeq: Seq[Double] = null

  def run(input: String): Double = {
    scalaSeq.sum
  }

  override def main(args: Array[String]): Unit = {
    scalaSeq = Vector.fill(args.last.toInt)(math.random)
    super.main(args.init)
  }
}
