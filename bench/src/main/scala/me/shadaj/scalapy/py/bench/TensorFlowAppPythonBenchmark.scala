package me.shadaj.scalapy.py.bench

import me.shadaj.scalapy.py
import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit
import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.py.SeqConverters
import me.shadaj.scalapy.interpreter.CPythonInterpreter

//@Warmup(iterations = 5, time = 100, timeUnit = MILLISECONDS)
//@Measurement(iterations = 100, time = 100, timeUnit = MILLISECONDS)
//@Fork(5)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class TensorFlowAppPythonBenchmark {
  PyValue.disableAllocationWarning()
  
  @Param(Array("100","1000","10000"))
  var length :Int = _
  
  @Benchmark
  def run(): Unit = py.local {
    CPythonInterpreter.execManyLines(
     s"""import tensorflow as tf
        |import numpy as np
        |
        |xData = np.random.rand($length).astype(np.float32)
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
}
