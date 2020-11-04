package me.shadaj.scalapy.py

import me.shadaj.scalapy.interpreter.Platform

import java.io.PrintWriter

object Bench {
  def test(name: String, warmup: Int, batches: Int, batchSize: Int)(v: () => scala.Any): Unit = {
    val individualAverageTimes = new Array[Double](batches)

    var lastOut: scala.Any = null

    var wI = 0
    while (wI < warmup * batchSize) {
      lastOut = v.apply()
      wI += 1
    }

    var i = 0
    while (i < batches) {
      val start = System.nanoTime()
      var j = 0
      while (j < batchSize) {
        lastOut = v.apply()
        j += 1
      }

      individualAverageTimes(i) = (System.nanoTime() - start).toDouble / batchSize
      i += 1
    }

    val averageTime = individualAverageTimes.sum / batches.toDouble
    val standardDeviation = math.sqrt(
      individualAverageTimes.map(t => math.pow(t - averageTime, 2)).sum / batches.toDouble
    )

    println(s"$name: mean $averageTime ns, std dev $standardDeviation ns")

    val writer = new PrintWriter(s"$name-${if (Platform.isNative) "native" else "jvm"}.bench.txt")
    individualAverageTimes.foreach(writer.println)
    writer.close()
  }
}
