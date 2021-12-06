package me.shadaj.scalapy.py.bench

import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit
import me.shadaj.scalapy.py
import me.shadaj.scalapy.interpreter.PyValue
import me.shadaj.scalapy.py.SeqConverters
import scala.collection.immutable

//@Warmup(iterations = 5, time = 100, timeUnit = MILLISECONDS)
//@Measurement(iterations = 100, time = 100, timeUnit = MILLISECONDS)
//@Fork(5)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class SumPythonCopyBenchmark  {
  PyValue.disableAllocationWarning()

  @Param(Array("100","100000","100000000"))
  var length :Int = _

  var values: Array[Double] = _

  var pythonSeq: py.Any = null

  @Setup
  def setup():Unit = {
   values = Array.fill(length)(math.random)

   pythonSeq = values.toPythonCopy
  }

  @Benchmark
  def run():Double = py.local {
    val backToScala = pythonSeq.as[immutable.ArraySeq[Double]]
    backToScala.sum
  } 
}

