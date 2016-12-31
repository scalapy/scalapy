package me.shadaj.scalapy

import jep.Jep

package object py {
  def module(name: String)(implicit jep: Jep) = Module(name)
  def module(name: String, subname: String)(implicit jep: Jep) = Module(name, subname)

  def global(implicit jep: Jep) = new Global()

  implicit class ToNativeSeq[T](seq: Seq[T])(implicit jep: Jep) {
    def mapNative[U](f: T => U)(implicit writer: ObjectWriter[U], reader: ObjectReader[U]): NativeSeq[U] = {
      val to = Object("[]")
      for (i <- 0 until seq.size) {
        val v = seq(i)
        val mapped = f(v)
        jep.eval(s"${to.expr}.append(${writer.write(mapped).expr})")
      }

      val ret = new NativeSeq[U](to)

      ret
    }
  }
}
