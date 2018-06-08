//package me.shadaj.scalapy.py
//
//import jep.Jep
//
//class NativeSeq[T] (private[py] val orig: Object)(implicit reader: ObjectReader[T], jep: Jep) extends collection.immutable.Seq[T] {
//  protected lazy val origDynamic = orig.asInstanceOf[DynamicObject]
//
//  override lazy val length: Int = global.len(orig).as[Int]
//
//  protected lazy val memo: Array[Any] = new Array[Any](length)
//
//  override def apply(idx: Int): T = {
//    if (memo(idx) == null) {
//      memo(idx) = origDynamic.arrayAccess(idx).as[T]
//    }
//
//    memo(idx).asInstanceOf[T]
//  }
//
//  def mapNative[U](f: T => U)(implicit writer: ObjectWriter[U], reader: ObjectReader[U]): NativeSeq[U] = {
//    val ret = new NativeSeq[U](Object(
//      (0 until length).view.map(i => writer.write(f(this(i))).expr).mkString("[", ",", "]")
//    ))
//
//    ret
//  }
//
//  override def iterator: Iterator[T] = (0 until length).toIterator.map(apply)
//}
