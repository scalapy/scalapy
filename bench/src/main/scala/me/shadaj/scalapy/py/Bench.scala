package me.shadaj.scalapy.py

import java.io.PrintWriter

object Bench {
  def test(name: String, batches: Int, batchSize: Int)(v: () => Unit): Unit = {
    val times = new Array[Long](batches)

    var i = 0
    while (i < batches) {
      val start = System.nanoTime()
      var j = 0
      while (j < batchSize) {
        v.apply()
        j += 1
      }

      times(i) = System.nanoTime() - start
      i += 1
    }

    val individualAverageTimes = times.map(_.toDouble / batchSize)
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
