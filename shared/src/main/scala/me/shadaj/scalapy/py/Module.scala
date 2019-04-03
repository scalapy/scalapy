package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.reflect.ClassTag

class Module private[py](private[py] val moduleName: String) extends scala.Dynamic {
  def applyDynamic(method: String)(params: Object*): DynamicObject = {
    if (method == "apply") {
      Object(s"$moduleName(${params.map(_.expr).mkString(",")})").asInstanceOf[DynamicObject]
    } else {
      Object(s"$moduleName.$method(${params.map(_.expr).mkString(",")})").asInstanceOf[DynamicObject]
    }
  }

  def applyDynamicNamed(method: String)(params: (String, Object)*): DynamicObject = {
    if (method == "apply") {
      Object(s"$moduleName(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})").asInstanceOf[DynamicObject]
    } else {
      Object(s"$moduleName.$method(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})").asInstanceOf[DynamicObject]
    }
  }

  def selectDynamic(value: String): DynamicObject = {
    Object(s"$moduleName.$value").asInstanceOf[DynamicObject]
  }

  def updateDynamic(name: String)(value: Object): Unit = {
    interpreter.eval(s"$moduleName.$name = ${value.expr}")
  }

  override def finalize(): Unit = {
    interpreter.eval(s"del $moduleName")
  }

  def as[T <: ObjectFacade](implicit classTag: ClassTag[T]): T = {
    classTag.runtimeClass.getConstructor(classOf[Object]).newInstance(Object(moduleName)).asInstanceOf[T]
  }
}

object Module {
  private var nextCounter: Int = 0
  def apply(module: String): Module = {
    val moduleName = s"spy_m_$nextCounter"
    nextCounter += 1

    interpreter.eval(s"import $module as $moduleName")
    new Module(moduleName)
  }

  def apply(module: String, subname: String): Module = {
    val moduleName = s"spy_m_$nextCounter"
    nextCounter += 1

    interpreter.eval(s"from $module import $subname as $moduleName")
    new Module(moduleName)
  }
}
