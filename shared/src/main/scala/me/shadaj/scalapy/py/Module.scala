package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.reflect.ClassTag

class Module private[py](private[py] val moduleName: String) extends scala.Dynamic {
  def applyDynamic(method: String)(params: Object*): Dynamic = {
    if (method == "apply") {
      Object(s"$moduleName(${params.map(_.expr).mkString(",")})").asDynamic
    } else {
      Object(s"$moduleName.$method(${params.map(_.expr).mkString(",")})").asDynamic
    }
  }

  def applyDynamicNamed(method: String)(params: (String, Object)*): Dynamic = {
    if (method == "apply") {
      Object(s"$moduleName(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})").asDynamic
    } else {
      Object(s"$moduleName.$method(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})").asDynamic
    }
  }

  def selectDynamic(value: String): Dynamic = {
    Object(s"$moduleName.$value").asDynamic
  }

  def updateDynamic(name: String)(value: Object): Unit = {
    interpreter.eval(s"$moduleName.$name = ${value.expr}")
  }

  override def finalize(): Unit = {
    interpreter.eval(s"del $moduleName")
  }

  def as[T <: ObjectFacade](implicit facadeCreator: FacadeCreator[T]): T = {
    val inst = facadeCreator.create
    inst.value = Object(moduleName).value
    inst
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
