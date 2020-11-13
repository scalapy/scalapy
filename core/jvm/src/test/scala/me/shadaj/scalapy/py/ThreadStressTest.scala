package me.shadaj.scalapy.py

import org.scalatest.funsuite.AnyFunSuite

class ThreadStressTest extends AnyFunSuite {
  test("Accessing the same list from many different threads works") {
    val list = Dynamic.global.list()

    val threads = (1 to 5000).map { i =>
      val t = new Thread(() => {
        list.append(i)
      })

      t.start()

      t
    }

    threads.foreach(_.join())

    assert(list.as[Seq[Int]].sorted == (1 to 5000).toSeq)
  }
}
