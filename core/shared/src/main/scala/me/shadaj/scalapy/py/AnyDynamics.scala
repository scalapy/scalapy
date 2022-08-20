package me.shadaj.scalapy.py

import scala.language.dynamics

import me.shadaj.scalapy.interpreter.CPythonInterpreter

trait AnyDynamics extends Any with scala.Dynamic {
  def apply(params: Any*): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(
      CPythonInterpreter.call(__scalapy_value, params.map(_.__scalapy_value), Seq())
    )
  }

  def applyDynamic(method: String)(params: Any*): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(
      CPythonInterpreter.call(__scalapy_value, method, params.map(_.__scalapy_value), Seq())
    )
  }

  def applyNamed(params: (String, Any)*): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.call(
      __scalapy_value,
      params.filter(t => t._1.isEmpty && t._2 != null).map(_._2.__scalapy_value),
      params.filter(t => t._1.nonEmpty && t._2 != null).map(t => (t._1, t._2.__scalapy_value))
    ))
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.call(
      __scalapy_value, method,
      params.filter(t => t._1.isEmpty && t._2 != null).map(_._2.__scalapy_value),
      params.filter(t => t._1.nonEmpty && t._2 != null).map(t => (t._1, t._2.__scalapy_value))
    ))
  }

  def selectDynamic(term: String): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(
      CPythonInterpreter.select(__scalapy_value, term)
    )
  }

  def updateDynamic(name: String)(newValue: Any): Unit = {
    CPythonInterpreter.update(__scalapy_value, name, newValue.__scalapy_value)
  }

  def bracketAccess(key: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(
      CPythonInterpreter.selectBracket(__scalapy_value, key.__scalapy_value)
    )
  }

  def bracketUpdate(key: Any, newValue: Any): Unit = {
    CPythonInterpreter.updateBracket(__scalapy_value, key.__scalapy_value, newValue.__scalapy_value)
  }

  def bracketDelete(key: Any): Unit = {
    CPythonInterpreter.deleteBracket(__scalapy_value, key.__scalapy_value)
  }

  def attrDelete(name: String): Unit = {
    CPythonInterpreter.deleteAttribute(__scalapy_value, name)
  }

  def unary_+ : Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.unaryPos(__scalapy_value))
  }

  def unary_- : Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.unaryNeg(__scalapy_value))
  }

  def +(that: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryAdd(__scalapy_value, that.__scalapy_value))
  }

  def -(that: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binarySub(__scalapy_value, that.__scalapy_value))
  }

  def *(that: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryMul(__scalapy_value, that.__scalapy_value))
  }

  def /(that: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryDiv(__scalapy_value, that.__scalapy_value))
  }

  def %(that: Any): Dynamic = {
    implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryMod(__scalapy_value, that.__scalapy_value))
  }
}
