package me.shadaj.scalapy.py

import scala.language.dynamics

import me.shadaj.scalapy.interpreter.CPythonInterpreter

trait AnyDynamics extends Any with scala.Dynamic {
  def apply(params: Any*): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(
      CPythonInterpreter.call(value, params.map(_.value), Seq())
    )
  }

  def applyDynamic(method: String)(params: Any*): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(
      CPythonInterpreter.call(value, method, params.map(_.value), Seq())
    )
  }

  def applyNamed(params: (String, Any)*): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.call(
      value,
      params.filter(t => t._1.isEmpty && t._2 != null).map(_._2.value),
      params.filter(t => t._1.nonEmpty && t._2 != null).map(t => (t._1, t._2.value))
    ))
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.call(
      value, method,
      params.filter(t => t._1.isEmpty && t._2 != null).map(_._2.value),
      params.filter(t => t._1.nonEmpty && t._2 != null).map(t => (t._1, t._2.value))
    ))
  }

  def selectDynamic(term: String): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(
      CPythonInterpreter.select(value, term)
    )
  }

  def updateDynamic(name: String)(newValue: Any): Unit = {
    CPythonInterpreter.update(value, name, newValue.value)
  }

  def bracketAccess(key: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(
      CPythonInterpreter.selectBracket(value, key.value)
    )
  }

  def bracketUpdate(key: Any, newValue: Any): Unit = {
    CPythonInterpreter.updateBracket(value, key.value, newValue.value)
  }

  def bracketDelete(key: Any): Unit = {
    CPythonInterpreter.deleteBracket(value, key.value)
  }

  def attrDelete(name: String): Unit = {
    CPythonInterpreter.deleteAttribute(value, name)
  }

  def unary_+(): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.unaryPos(value))
  }

  def unary_-(): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.unaryNeg(value))
  }

  def +(that: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryAdd(value, that.value))
  }

  def -(that: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binarySub(value, that.value))
  }

  def *(that: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryMul(value, that.value))
  }

  def /(that: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryDiv(value, that.value))
  }

  def %(that: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryMod(value, that.value))
  }
}
