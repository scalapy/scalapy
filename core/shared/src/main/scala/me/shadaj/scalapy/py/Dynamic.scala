package me.shadaj.scalapy.py

import scala.language.dynamics

@native trait Dynamic extends Any with AnyDynamics

trait AnyDynamics extends scala.Any with Any with scala.Dynamic {
  def applyDynamic(method: String)(params: Any*): Dynamic = {
    Any.populateWith(CPythonInterpreter.call(value, method, params.map(_.value))).as[Dynamic]
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    eval(s"${this.expr}.$method(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})")
  }

  def selectDynamic(term: String): Dynamic = {
    Any.populateWith(CPythonInterpreter.select(value, term)).as[Dynamic]
  }

  def updateDynamic(name: String)(newValue: Any): Unit = {
    CPythonInterpreter.update(value, name, newValue.value)
  }

  def arrayAccess(index: Int): Dynamic = {
    Any.populateWith(CPythonInterpreter.selectList(value, index)).as[Dynamic]
  }

  def dictionaryAccess(key: Any): Dynamic = {
    Any.populateWith(CPythonInterpreter.selectDictionary(value, key.value)).as[Dynamic]
  }

  def unary_+(): Dynamic = {
    Any.populateWith(CPythonInterpreter.unaryPos(value)).as[Dynamic]
  }

  def unary_-(): Dynamic = {
    Any.populateWith(CPythonInterpreter.unaryNeg(value)).as[Dynamic]
  }

  def +(that: Any): Dynamic = {
    Any.populateWith(CPythonInterpreter.binaryAdd(value, that.value)).as[Dynamic]
  }

  def -(that: Any): Dynamic = {
    Any.populateWith(CPythonInterpreter.binarySub(value, that.value)).as[Dynamic]
  }

  def *(that: Any): Dynamic = {
    Any.populateWith(CPythonInterpreter.binaryMul(value, that.value)).as[Dynamic]
  }

  def /(that: Any): Dynamic = {
    Any.populateWith(CPythonInterpreter.binaryDiv(value, that.value)).as[Dynamic]
  }

  def %(that: Any): Dynamic = {
    Any.populateWith(CPythonInterpreter.binaryMod(value, that.value)).as[Dynamic]
  }
}
