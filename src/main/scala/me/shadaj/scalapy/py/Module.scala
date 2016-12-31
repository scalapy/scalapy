package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics

class Module private[py](private[py] val moduleName: String)(implicit jep: Jep) extends scala.Dynamic {
  def applyDynamic(method: String)(params: Ref*): Object = {
    if (method == "apply") {
      Object(s"$moduleName(${params.map(_.expr).mkString(",")})")
    } else {
      Object(s"$moduleName.$method(${params.map(_.expr).mkString(",")})")(jep)
    }
  }

  def selectDynamic(value: String): Object = {
    Object(s"$moduleName.$value")
  }

  def updateDynamic(name: String)(value: Ref): Unit = {
    jep.eval(s"$moduleName.$name = ${value.expr}")
  }

  override def finalize(): Unit = {
    jep.eval(s"del $moduleName")
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