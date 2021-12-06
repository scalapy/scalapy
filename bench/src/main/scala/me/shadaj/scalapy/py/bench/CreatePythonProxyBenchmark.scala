
package me.shadaj.scalapy.py.bench

import me.shadaj.scalapy.py
import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.py.SeqConverters
import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

//@Warmup(iterations = 5, time = 100, timeUnit = MILLISECONDS)
//@Measurement(iterations = 100, time = 100, timeUnit = MILLISECONDS)
//@Fork(5)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class CreatePythonProxyBenchmark  {
  PyValue.disableAllocationWarning()

  @Param(Array("100","100000","100000000"))
  var length :Int = _

  var values: Array[Double] = _

  @Setup
  def setup():Unit = {
   values = Array.fill(length)(math.random)
  }


  @Benchmark
  def run():Int = py.local {
    val pySequence = values.toPythonProxy
    py.Dynamic.global.len(pySequence).as[Int]
  } 
}
