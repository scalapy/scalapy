package me.shadaj.scalapy.py

import java.util

import jep.Jep
import me.shadaj.scalapy.py

import scala.reflect.ClassTag
import scala.collection.JavaConverters._

abstract class ObjectWriter[T] {
  def write(v: T)(implicit jep: Jep): Any
}

object ObjectWriter extends ObjectTupleWriters {
  implicit val pyObjWriter: ObjectWriter[Object] = new ObjectWriter[Object] {
    override def write(v: Object)(implicit jep: Jep): Any = {
      jep.getValue(v.expr)
    }
  }

  implicit val noneWriter: ObjectWriter[None.type] = new ObjectWriter[None.type] {
    override def write(v: None.type)(implicit jep: Jep) = null
  }

  implicit def unionWriter[A, B](implicit aClass: ClassTag[A], bClass: ClassTag[B], aWriter: ObjectWriter[A], bWriter: ObjectWriter[B]): ObjectWriter[A | B] = new ObjectWriter[A | B] {
    override def write(v: A | B)(implicit jep: Jep): Any = {
      aClass.unapply(v.value) match {
        case Some(a) => aWriter.write(a)
        case _ => bWriter.write(v.value.asInstanceOf[B])
      }
    }
  }

  implicit def pyFascadeWriter[T <: ObjectFascade]: ObjectWriter[T] = new ObjectWriter[T] {
    override def write(v: T)(implicit jep: Jep): Any = {
      jep.getValue(v.expr)
    }
  }

  implicit val pyDynamicObjWriter: ObjectWriter[DynamicObject] = new ObjectWriter[DynamicObject] {
    override def write(v: DynamicObject)(implicit jep: Jep): Any = {
      jep.getValue(v.expr)
    }
  }

  implicit val byteWriter: ObjectWriter[Byte] = new ObjectWriter[Byte] {
    override def write(v: Byte)(implicit jep: Jep): Any = {
      v
    }
  }

  implicit val intWriter: ObjectWriter[Int] = new ObjectWriter[Int] {
    override def write(v: Int)(implicit jep: Jep): Int = {
      v
    }
  }

  implicit val longWriter: ObjectWriter[Long] = new ObjectWriter[Long] {
    override def write(v: Long)(implicit jep: Jep): Any = {
      v
    }
  }

  implicit val doubleWriter: ObjectWriter[Double] = new ObjectWriter[Double] {
    override def write(v: Double)(implicit jep: Jep): Any = {
      v
    }
  }

  implicit val floatWriter: ObjectWriter[Float] = new ObjectWriter[Float] {
    override def write(v: Float)(implicit jep: Jep): Any = {
      v
    }
  }

  implicit val booleanWriter: ObjectWriter[Boolean] = new ObjectWriter[Boolean] {
    override def write(v: Boolean)(implicit jep: Jep): Boolean = {
      v
    }
  }

  implicit val stringWriter: ObjectWriter[String] = new ObjectWriter[String] {
    override def write(v: String)(implicit jep: Jep): Any = {
      v
    }
  }

  private val supportedObjectTypes = Set[Class[_]](classOf[String])
  implicit def seqWriter[T: ClassTag, C](implicit ev1: C => Seq[T], tWriter: ObjectWriter[T]): ObjectWriter[C] = new ObjectWriter[C] {
    def isNativeWritable(clazz: Class[_]): Boolean = {
      clazz.isPrimitive || supportedObjectTypes.contains(clazz) ||
        (clazz.isArray && isNativeWritable(clazz.getComponentType))
    }

    override def write(v: C)(implicit jep: Jep): Any = {
      v match {
//        case ts: NativeSeq[_] =>
//          ts.orig
        case _ =>
          val coll = ev1(v)
          val elemClass = implicitly[ClassTag[T]].runtimeClass
          if (isNativeWritable(elemClass)) {
            coll.toArray[T]
          } else {
            v.view.map { e =>
              tWriter.write(e)
            }.toArray
          }
      }
    }
  }

  implicit def mapWriter[I, O](implicit iWriter: ObjectWriter[I], oWriter: ObjectWriter[O]) = new ObjectWriter[Map[I, O]] {
    override def write(v: Map[I, O])(implicit jep: Jep): Any = {
      val javaMap = new util.HashMap[Any, Any]()
      v.foreach { case (i, o) =>
        javaMap.put(iWriter.write(i), oWriter.write(o))
      }
      javaMap
    }
  }
}
