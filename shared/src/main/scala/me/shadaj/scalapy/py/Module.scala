package me.shadaj.scalapy.py

import scala.language.dynamics
import scala.reflect.ClassTag

final class Module(private[py] val value: PyValue) extends AnyVal with Any with AnyDynamics

object Module {
  private var nextCounter: Int = 0
  def apply(module: String): Module = {
    val moduleName = s"spy_m_$nextCounter"
    nextCounter += 1

    interpreter.eval(s"import $module as $moduleName")
    new Module(eval(moduleName).value)
  }

  def apply(module: String, subname: String): Module = {
    val moduleName = s"spy_m_$nextCounter"
    nextCounter += 1

    interpreter.eval(s"from $module import $subname as $moduleName")
    new Module(eval(moduleName).value)
  }
}
