package me.shadaj.scalapy.py

import jep.Jep
import me.shadaj.scalapy.py

trait ObjectWriter[T] {
  def write(v: T)(implicit jep: Jep): py.Ref
}

object ObjectWriter {
  implicit def pyRefWriter: ObjectWriter[Ref] = new ObjectWriter[Ref] {
    override def write(v: Ref)(implicit jep: Jep): py.Ref = {
      v
    }
  }

  implicit def pyObjWriter: ObjectWriter[Object] = new ObjectWriter[Object] {
    override def write(v: Object)(implicit jep: Jep): py.Ref = {
      v.asRef
    }
  }

  implicit def intWriter: ObjectWriter[Int] = new ObjectWriter[Int] {
    override def write(v: Int)(implicit jep: Jep): py.Ref = {
      Ref(v.toString)
    }
  }

  implicit def doubleWriter: ObjectWriter[Double] = new ObjectWriter[Double] {
    override def write(v: Double)(implicit jep: Jep): py.Ref = {
      Ref(v.toString)
    }
  }

  implicit def floatWriter: ObjectWriter[Float] = new ObjectWriter[Float] {
    override def write(v: Float)(implicit jep: Jep): py.Ref = {
      Ref(v.toString)
    }
  }

  implicit def booleanWriter: ObjectWriter[Boolean] = new ObjectWriter[Boolean] {
    override def write(v: Boolean)(implicit jep: Jep): py.Ref = {
      Ref(if (v) "True" else "False")
    }
  }

  implicit def stringWriter: ObjectWriter[String] = new ObjectWriter[String] {
    override def write(v: String)(implicit jep: Jep): py.Ref = {
      Ref('\"' + v + '\"')
    }
  }

  implicit def seqWriter[T, C](implicit ev1: C => Seq[T], tWriter: ObjectWriter[T]): ObjectWriter[C] = new ObjectWriter[C] {
    override def write(v: C)(implicit jep: Jep): py.Ref = {
      v match {
        case ts: NativeSeq[T] =>
          ts.orig
        case _ =>
          val array = Object("[]")
          v.foreach { e =>
            jep.eval(s"${array.expr}.append(${Ref.from(e).expr})")
          }

          array.asRef
      }
    }
  }
}
