package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.reflect.ClassTag

class Module private[py](private[py] val moduleName: String) extends scala.Dynamic {
  def applyDynamic(method: String)(params: Any*): Dynamic = {
    if (method == "apply") {
      py"$moduleName(${params.map(_.expr).mkString(",")})"
    } else {
      py"$moduleName.$method(${params.map(_.expr).mkString(",")})"
    }
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    if (method == "apply") {
      py"$moduleName(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})"
    } else {
      py"$moduleName.$method(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})"
    }
  }

  def selectDynamic(value: String): Dynamic = {
    py"$moduleName.$value"
  }

  def updateDynamic(name: String)(value: Any): Unit = {
    interpreter.eval(s"$moduleName.$name = ${value.expr}")
  }

  override def finalize(): Unit = {
    interpreter.eval(s"del $moduleName")
  }

  def as[T: ObjectReader]: T = {
    val obj = py"$moduleName"
    implicitly[ObjectReader[T]].read(new ValueAndRequestObject(obj.value) {
      override def getObject: Any = obj
    })
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
