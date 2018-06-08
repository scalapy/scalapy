package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics

class Global private[py](implicit jep: Jep) extends scala.Dynamic {
  def applyDynamic(method: String)(params: Object*): Object = {
    Object(s"$method(${params.map(_.expr).mkString(",")})")
  }

  def selectDynamic(value: String): Object = {
    Object(s"$value")
  }
}