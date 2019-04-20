package me.shadaj.scalapy.py

import scala.language.dynamics

final class Dynamic(private[py] val value: PyValue) extends Object with scala.Dynamic {
  def applyDynamic(method: String)(params: Object*): Dynamic = {
    new Dynamic(interpreter.call(value, method, params.map(_.value)))
  }

  def applyDynamicNamed(method: String)(params: (String, Object)*): Dynamic = {
    Object(s"$expr.$method(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})").asDynamic
  }

  def selectDynamic(term: String): Dynamic = {
    new Dynamic(interpreter.select(value, term))
  }

  def arrayAccess(index: Int): Dynamic = {
    new Dynamic(interpreter.selectList(value, index))
  }

  def dictionaryAccess(key: Object): Dynamic = {
    new Dynamic(interpreter.selectDictionary(value, key.value))
  }

  def unary_+(): Dynamic = {
    Object(s"+$expr").asDynamic
  }

  def unary_-(): Dynamic = {
    Object(s"-$expr").asDynamic
  }

  def +(that: Object): Dynamic = {
    new Dynamic(interpreter.binaryAdd(value, that.value))
  }

  def -(that: Object): Dynamic = {
    new Dynamic(interpreter.binarySub(value, that.value))
  }

  def *(that: Object): Dynamic = {
    new Dynamic(interpreter.binaryMul(value, that.value))
  }

  def /(that: Object): Dynamic = {
    new Dynamic(interpreter.binaryDiv(value, that.value))
  }

  def %(that: Object): Dynamic = {
    new Dynamic(interpreter.binaryMod(value, that.value))
  }
}
