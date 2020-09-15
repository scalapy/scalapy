package me.shadaj.scalapy.py

import PyConverters._
import me.shadaj.scalapy.py.interpreter.PyValue

object DataSendingBench extends App {
  PyValue.disableAllocationWarning()

  val listSizes = Seq(1, 5, 10, 50, 100)

  val warmUp = 100
  val batches = 2000
  val batchSize = 200

  listSizes.foreach { s =>
    val scalaArray = List.fill(s)(math.random)
    Bench.test(s"Create Python copy (size $s)", warmUp, batches / 2, batchSize / 2) {
      () => local(scalaArray.toPythonCopy)
    }
  }

  listSizes.foreach { s =>
    val scalaArray = List.fill(s)(math.random)
    Bench.test(s"Create Python proxy (size $s)", warmUp, batches / 2, batchSize / 2) {
      () => local(scalaArray.toPythonProxy)
    }
  }

  listSizes.foreach { s =>
    local {
      val scalaArray = List.fill(s)(math.random)
      val pyArray = scalaArray.toPythonCopy
      val backToScala = pyArray.as[Seq[Double]]

      val expected = scalaArray.sum
      Bench.test(s"Sum elements of Python list (copy, size $s)", warmUp, batches, batchSize) {
        () => {
          assert(backToScala.sum == expected)
        }
      }
    }
  }

  listSizes.foreach { s =>
    local {
      val scalaArray = List.fill(s)(math.random)
      val pyArray = scalaArray.toPythonProxy
      val backToScala = pyArray.as[Seq[Double]]

      val expected = scalaArray.sum
      Bench.test(s"Sum elements of Python list (proxy, size $s)", warmUp, batches, batchSize) {
        () => {
          assert(backToScala.sum == expected)
        }
      }
    }
  }

  listSizes.foreach { s =>
    local {
      val scalaArray = List.fill(s)(math.random)

      val expected = scalaArray.sum
      Bench.test(s"Sum elements of Scala list (size $s)", warmUp, batches, batchSize) {
        () => {
          assert(scalaArray.sum == expected)
        }
      }
    }
  }
}
