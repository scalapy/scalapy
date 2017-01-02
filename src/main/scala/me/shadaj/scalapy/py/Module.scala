package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics
import scala.reflect.ClassTag

class Module private[py](private[py] val moduleName: String)(implicit jep: Jep) extends scala.Dynamic {
  def applyDynamic(method: String)(params: Ref*): DynamicObject = {
    if (method == "apply") {
      Object(s"$moduleName(${params.map(_.expr).mkString(",")})").asInstanceOf[DynamicObject]
    } else {
      Object(s"$moduleName.$method(${params.map(_.expr).mkString(",")})")(jep).asInstanceOf[DynamicObject]
    }
  }

  def selectDynamic(value: String): DynamicObject = {
    Object(s"$moduleName.$value").asInstanceOf[DynamicObject]
  }

  def updateDynamic(name: String)(value: Ref): Unit = {
    jep.eval(s"$moduleName.$name = ${value.expr}")
  }

  override def finalize(): Unit = {
    jep.eval(s"del $moduleName")
  }

  def as[T <: ObjectFascade](implicit classTag: ClassTag[T]): T = {
    classTag.runtimeClass.getConstructor(classOf[Object], classOf[Jep]).newInstance(Object(moduleName), jep).asInstanceOf[T]
  }
}

object Module {
  private var nextCounter: Int = 0
  def apply(module: String)(implicit jep: Jep): Module = {
    val moduleName = s"spy_m_$nextCounter"
    nextCounter += 1

    jep.eval(s"import $module as $moduleName")
    new Module(moduleName)
  }

  def apply(module: String, subname: String)(implicit jep: Jep): Module = {
    val moduleName = s"spy_m_$nextCounter"
    nextCounter += 1

    jep.eval(s"from $module import $subname as $moduleName")
    new Module(moduleName)
  }
}