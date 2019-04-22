package me.shadaj.scalapy.py

import scala.language.dynamics

final class Dynamic(private[py] val value: PyValue) extends AnyVal with Any with scala.Dynamic {
  def applyDynamic(method: String)(params: Any*): Dynamic = {
    new Dynamic(interpreter.call(value, method, params.map(_.value)))
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    py"$this.$method(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})"
  }

  def selectDynamic(term: String): Dynamic = {
    new Dynamic(interpreter.select(value, term))
  }

  def arrayAccess(index: Int): Dynamic = {
    new Dynamic(interpreter.selectList(value, index))
  }

  def dictionaryAccess(key: Any): Dynamic = {
    new Dynamic(interpreter.selectDictionary(value, key.value))
  }

  def unary_+(): Dynamic = {
    new Dynamic(interpreter.unaryPos(value))
  }

  def unary_-(): Dynamic = {
    new Dynamic(interpreter.unaryNeg(value))
  }

  def +(that: Any): Dynamic = {
    new Dynamic(interpreter.binaryAdd(value, that.value))
  }

  def -(that: Any): Dynamic = {
    new Dynamic(interpreter.binarySub(value, that.value))
  }

  def *(that: Any): Dynamic = {
    new Dynamic(interpreter.binaryMul(value, that.value))
  }

  def /(that: Any): Dynamic = {
    new Dynamic(interpreter.binaryDiv(value, that.value))
  }

  def %(that: Any): Dynamic = {
    new Dynamic(interpreter.binaryMod(value, that.value))
  }
}
