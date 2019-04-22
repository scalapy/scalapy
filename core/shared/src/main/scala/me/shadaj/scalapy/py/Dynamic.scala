package me.shadaj.scalapy.py

import scala.language.dynamics

@native trait Dynamic extends Any with AnyDynamics

trait AnyDynamics extends scala.Any with Any with scala.Dynamic {
  def applyDynamic(method: String)(params: Any*): Dynamic = {
    Any.populateWith(interpreter.call(value, method, params.map(_.value))).as[Dynamic]
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    eval(s"${this.expr}.$method(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})")
  }

  def selectDynamic(term: String): Dynamic = {
    Any.populateWith(interpreter.select(value, term)).as[Dynamic]
  }

  def updateDynamic(name: String)(newValue: Any): Unit = {
    interpreter.update(value, name, newValue.value)
  }

  def arrayAccess(index: Int): Dynamic = {
    Any.populateWith(interpreter.selectList(value, index)).as[Dynamic]
  }

  def dictionaryAccess(key: Any): Dynamic = {
    Any.populateWith(interpreter.selectDictionary(value, key.value)).as[Dynamic]
  }

  def unary_+(): Dynamic = {
    Any.populateWith(interpreter.unaryPos(value)).as[Dynamic]
  }

  def unary_-(): Dynamic = {
    Any.populateWith(interpreter.unaryNeg(value)).as[Dynamic]
  }

  def +(that: Any): Dynamic = {
    Any.populateWith(interpreter.binaryAdd(value, that.value)).as[Dynamic]
  }

  def -(that: Any): Dynamic = {
    Any.populateWith(interpreter.binarySub(value, that.value)).as[Dynamic]
  }

  def *(that: Any): Dynamic = {
    Any.populateWith(interpreter.binaryMul(value, that.value)).as[Dynamic]
  }

  def /(that: Any): Dynamic = {
    Any.populateWith(interpreter.binaryDiv(value, that.value)).as[Dynamic]
  }

  def %(that: Any): Dynamic = {
    Any.populateWith(interpreter.binaryMod(value, that.value)).as[Dynamic]
  }
}
