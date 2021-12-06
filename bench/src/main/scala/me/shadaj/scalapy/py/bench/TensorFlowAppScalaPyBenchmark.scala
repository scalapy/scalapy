package me.shadaj.scalapy.py.bench

import me.shadaj.scalapy.py
import java.util.concurrent.TimeUnit
import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.py.SeqConverters
import org.openjdk.jmh.annotations._

//@Warmup(iterations = 5, time = 100, timeUnit = MILLISECONDS)
//@Measurement(iterations = 100, time = 100, timeUnit = MILLISECONDS)
//@Fork(5)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class TensorFlowAppScalaPyBenchmark {
  PyValue.disableAllocationWarning()

  @Param(Array("100","1000","10000"))
  var length :Int = _

  @Benchmark
  def run(): Unit = py.local {
    val tf = py.module("tensorflow")
    val np = py.module("numpy")

    val xData = Seq.fill(length)(math.random)
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

    var i = 0
    while(i < 50) {
      sess.run(train)
      i+=1
    }
  }
}
