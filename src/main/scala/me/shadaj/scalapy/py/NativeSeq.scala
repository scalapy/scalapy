package me.shadaj.scalapy.py

import jep.Jep

class NativeSeq[T] (private[py] val orig: Ref)(implicit reader: ObjectReader[T], jep: Jep) extends collection.immutable.Seq[T] {
  protected lazy val origDynamic = orig.toObject.asInstanceOf[DynamicObject]

  override lazy val length: Int = global.len(orig).as[Int]

  protected lazy val memo: Array[Any] = new Array[Any](length)

  override def apply(idx: Int): T = {
    if (memo(idx) == null) {
      memo(idx) = origDynamic.arrayAccess(idx).as[T]
    }

    memo(idx).asInstanceOf[T]
  }

  def mapNative[U](f: T => U)(implicit writer: ObjectWriter[U], reader: ObjectReader[U]): NativeSeq[U] = {
    val to = Object("[]")
    for (i <- 0 until length) {
      val v = this(i)
      val mapped = f(v)
      jep.eval(s"${to.expr}.append(${writer.write(mapped).expr})")
    }

    val ret = new NativeSeq[U](to)

    ret
  }

  override def iterator: Iterator[T] = (0 until length).toIterator.map(apply)
}
