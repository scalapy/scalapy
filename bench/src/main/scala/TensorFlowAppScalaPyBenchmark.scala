import me.shadaj.scalapy.py
import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.py.SeqConverters

object TensorFlowAppScalaPyBenchmark extends communitybench.Benchmark {
  PyValue.disableAllocationWarning()

  def run(input: String): Unit = py.local {
    val tf = py.module("tensorflow")
    val np = py.module("numpy")

    val xData = Seq.fill(100)(math.random)
    val yData = xData.map(x => x * 0.1 + 0.3)

    val xDataPython = xData.toPythonCopy
    val yDataPython = yData.toPythonCopy

    val W = tf.Variable(tf.random_uniform(Seq(1).toPythonCopy, -1, -1))
    val b = tf.Variable(tf.zeros(Seq(1).toPythonCopy))
    val y = (W * xDataPython) + b

    val loss = tf.reduce_mean(tf.square(y - yDataPython))
    val optimizer = tf.train.GradientDescentOptimizer(0.5)
    val train = optimizer.minimize(loss)

    val init = tf.global_variables_initializer()

    val sess = tf.Session()
    sess.run(init)
    
    (0 until 50).foreach { step =>
      sess.run(train)
    }
  }

  override def main(args: Array[String]): Unit = {
    super.main(args.init)
  }
}
