package me.shadaj.scalapy.py

object DataSendingBench extends App {
  Seq(1, 5, 10, 50, 100, 500).foreach { s =>
    Bench.test(s"Send an Array from Scala to Python (size $s)", batches = 500, batchSize = 100) {
      val array = Array.fill(s)(math.random)
  
      () => {
        local {
          val inPython = Any.from(array)
        }
      }
    }
  }

  Seq(1, 5, 10, 50, 100, 500).foreach { s =>
    local {
      Bench.test(s"Read data from Python list (size $s)", batches = 500, batchSize = 100) {
        val pyArray = Any.from(Array.fill(s)(math.random))
    
        () => {
          pyArray.as[Seq[Double]].sum
        }
      }
    }
  }
}
