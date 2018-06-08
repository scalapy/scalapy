package me.shadaj.scalapy.py

import java.util

import jep.Jep

import scala.collection.mutable
import scala.reflect.ClassTag

abstract class ObjectWriter[T] {
  def write(v: T)(implicit jep: Jep): Either[Any, Object]
}

object ObjectWriter extends ObjectTupleWriters {
  implicit val pyObjWriter: ObjectWriter[Object] = new ObjectWriter[Object] {
    override def write(v: Object)(implicit jep: Jep): Either[Any, Object] = {
      Right(v)
    }
  }

  implicit val noneWriter: ObjectWriter[None.type] = new ObjectWriter[None.type] {
    override def write(v: None.type)(implicit jep: Jep): Either[Any, Object] = Left(null)
  }

  implicit def unionWriter[A, B](implicit aClass: ClassTag[A], bClass: ClassTag[B], aWriter: ObjectWriter[A], bWriter: ObjectWriter[B]): ObjectWriter[A | B] = new ObjectWriter[A | B] {
    override def write(v: A | B)(implicit jep: Jep): Either[Any, Object] = {
      aClass.unapply(v.value) match {
        case Some(a) => aWriter.write(a)
        case _ => bWriter.write(v.value.asInstanceOf[B])
      }
    }
  }

  implicit def pyFascadeWriter[T <: ObjectFacade]: ObjectWriter[T] = new ObjectWriter[T] {
    override def write(v: T)(implicit jep: Jep): Either[Any, Object] = {
      Right(v)
    }
  }

  implicit val pyDynamicObjWriter: ObjectWriter[DynamicObject] = new ObjectWriter[DynamicObject] {
    override def write(v: DynamicObject)(implicit jep: Jep): Either[Any, Object] = {
      Right(v)
    }
  }

  implicit val byteWriter: ObjectWriter[Byte] = new ObjectWriter[Byte] {
    override def write(v: Byte)(implicit jep: Jep): Either[Any, Object] = {
      Left(v)
    }
  }

  implicit val intWriter: ObjectWriter[Int] = new ObjectWriter[Int] {
    override def write(v: Int)(implicit jep: Jep): Either[Any, Object] = {
      Left(v)
    }
  }

  implicit val longWriter: ObjectWriter[Long] = new ObjectWriter[Long] {
    override def write(v: Long)(implicit jep: Jep): Either[Any, Object] = {
      Left(v)
    }
  }

  implicit val doubleWriter: ObjectWriter[Double] = new ObjectWriter[Double] {
    override def write(v: Double)(implicit jep: Jep): Either[Any, Object] = {
      Left(v)
    }
  }

  implicit val floatWriter: ObjectWriter[Float] = new ObjectWriter[Float] {
    override def write(v: Float)(implicit jep: Jep): Either[Any, Object] = {
      Left(v)
    }
  }

  implicit val booleanWriter: ObjectWriter[Boolean] = new ObjectWriter[Boolean] {
    override def write(v: Boolean)(implicit jep: Jep): Either[Any, Object] = {
      Left(v)
    }
  }

  implicit val stringWriter: ObjectWriter[String] = new ObjectWriter[String] {
    override def write(v: String)(implicit jep: Jep): Either[Any, Object] = {
      Left(v)
    }
  }

  private val supportedObjectTypes = Set[Class[_]](classOf[String])
  implicit def seqWriter[T: ClassTag, C](implicit ev1: C => Seq[T], tWriter: ObjectWriter[T]): ObjectWriter[C] = new ObjectWriter[C] {
    def isNativeWritable(clazz: Class[_]): Boolean = {
      clazz.isPrimitive || supportedObjectTypes.contains(clazz) ||
        (clazz.isArray && isNativeWritable(clazz.getComponentType))
    }

    override def write(v: C)(implicit jep: Jep): Either[Any, Object] = {
      v match {
        case _ =>
          val coll = ev1(v)
          val elemClass = implicitly[ClassTag[T]].runtimeClass
          if (isNativeWritable(elemClass)) {
            Left(coll.toArray[T])
          } else {
            val writtenElems = v.view.map { e =>
              tWriter.write(e)
            }

            if (writtenElems.forall(_.isLeft)) Left(writtenElems.map(_.left.get).toArray)
            else {
              val writtenObjects = writtenElems.map(_.left.map(Object.populateWith).merge.expr)
              Right(Object(writtenObjects.mkString("[", ",", "]")))
            }
          }
      }
    }
  }

  implicit def mapWriter[I, O](implicit iWriter: ObjectWriter[I], oWriter: ObjectWriter[O]) = new ObjectWriter[Map[I, O]] {
    override def write(map: Map[I, O])(implicit jep: Jep): Either[Any, Object] = {
      val toAddLater = mutable.Queue.empty[(Object, Object)]

      val javaMap = new util.HashMap[Any, Any]()
      map.foreach { case (i, o) =>
        (iWriter.write(i), oWriter.write(o)) match {
          case (Left(k), Left(v)) => javaMap.put(k, v)
          case (Left(k), Right(vo)) => toAddLater.enqueue((Object.populateWith(k), vo))
          case (Right(ko), Left(v)) => toAddLater.enqueue((ko, Object.populateWith(v)))
          case (Right(ko), Right(vo)) => toAddLater.enqueue((ko, vo))
        }
      }

      if (toAddLater.isEmpty) {
        Left(javaMap)
      } else {
        val obj = Object.populateWith(javaMap)
        toAddLater.foreach { case (ko, vo) =>
          jep.eval(s"${obj.expr}[${ko.expr}] = ${vo.expr}")
        }

        Right(obj)
      }
    }
  }
}
