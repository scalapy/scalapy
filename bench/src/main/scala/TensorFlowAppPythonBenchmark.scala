import me.shadaj.scalapy.py
import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.interpreter.CPythonInterpreter
import me.shadaj.scalapy.py.AnyConverters

object TensorFlowAppPythonBenchmark extends communitybench.Benchmark {
  PyValue.disableAllocationWarning()

  def run(input: String): Unit = py.local {
    CPythonInterpreter.execManyLines(
      """import tensorflow as tf
        |import numpy as np
        |
        |xData = np.random.rand(100).astype(np.float32)
        |yData = (xData * 0.1) + 0.3
        |
        |W = tf.Variable(tf.random_uniform([1], -1, 1))
        |b = tf.Variable(tf.zeros([1]))
        |y = (W * xData) + b
        |
        |loss = tf.reduce_mean(tf.square(y - yData))
        |optimizer = tf.train.GradientDescentOptimizer(0.5)
        |train = optimizer.minimize(loss)
        |
        |init = tf.global_variables_initializer()
        |sess = tf.Session()
        |sess.run(init)
        |
        |for step in range(50):
        |  sess.run(train)""".stripMargin
    )
  }

  override def main(args: Array[String]): Unit = {
    super.main(args.init)
  }
}
