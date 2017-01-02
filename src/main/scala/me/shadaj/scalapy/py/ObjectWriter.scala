package me.shadaj.scalapy.py

import jep.Jep
import me.shadaj.scalapy.py

import scala.reflect.ClassTag

trait ObjectWriter[T] {
  def write(v: T)(implicit jep: Jep): py.Ref
}

object ObjectWriter extends ObjectTupleWriters {
  implicit def pyRefWriter: ObjectWriter[Ref] = new ObjectWriter[Ref] {
    override def write(v: Ref)(implicit jep: Jep): py.Ref = {
      v
    }
  }

  implicit def pyDynamicRefWriter: ObjectWriter[DynamicRef] = new ObjectWriter[DynamicRef] {
    override def write(v: DynamicRef)(implicit jep: Jep): py.Ref = {
      v.asRef
    }
  }

  implicit def pyObjWriter: ObjectWriter[Object] = new ObjectWriter[Object] {
    override def write(v: Object)(implicit jep: Jep): py.Ref = {
      v.asRef
    }
  }

  implicit def noneWriter: ObjectWriter[None] = new ObjectWriter[None] {
    override def write(v: None)(implicit jep: Jep) = v
  }

  implicit def unionWriter[A, B](implicit aClass: ClassTag[A], bClass: ClassTag[B], aWriter: ObjectWriter[A], bWriter: ObjectWriter[B]): ObjectWriter[A | B] = new ObjectWriter[A | B] {
    override def write(v: A | B)(implicit jep: Jep): py.Ref = {
      aClass.unapply(v.value) match {
        case Some(a) => a
        case _ => v.value.asInstanceOf[B]
      }
    }
  }

  implicit def pyFascadeWriter[T <: ObjectFascade]: ObjectWriter[T] = new ObjectWriter[T] {
    override def write(v: T)(implicit jep: Jep): py.Ref = {
      v
    }
  }

  implicit def pyDynamicObjWriter: ObjectWriter[DynamicObject] = new ObjectWriter[DynamicObject] {
    override def write(v: DynamicObject)(implicit jep: Jep): py.Ref = {
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

  implicit def mapWriter[I, O](implicit iWriter: ObjectWriter[I], oWriter: ObjectWriter[O]) = new ObjectWriter[Map[I, O]] {
    override def write(v: Map[I, O])(implicit jep: Jep): py.Ref = {
      val dict = Object("{}")
      v.foreach { case (i, o) =>
        jep.eval(s"${dict.expr}[${Ref.from(i).expr}] = ${Ref.from(o).expr}")
      }

      dict.asRef
    }
  }
}
