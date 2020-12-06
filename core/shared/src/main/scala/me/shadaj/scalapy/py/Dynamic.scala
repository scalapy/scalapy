package me.shadaj.scalapy.py

import scala.language.dynamics

import me.shadaj.scalapy.interpreter.CPythonInterpreter

@native trait Dynamic extends Any with AnyDynamics

object Dynamic {
  object global extends scala.Dynamic {
    def applyDynamic(method: String)(params: Any*): Dynamic = {
      Any.populateWith(CPythonInterpreter.callGlobal(method, params.map(_.value), Seq())).as[Dynamic]
    }

    def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
      Any.populateWith(CPythonInterpreter.callGlobal(
        method,
        params.filter(_._1.isEmpty).map(_._2.value),
        params.filter(_._1.nonEmpty).map(t => (t._1, t._2.value))
      )).as[Dynamic]
    }

    def selectDynamic(value: String): Dynamic = {
      Any.populateWith(CPythonInterpreter.selectGlobal(value)).as[Dynamic]
    }
  }
}

trait AnyDynamics extends scala.Any with Any with scala.Dynamic {
  def applyDynamic(method: String)(params: Any*): Dynamic = {
    Any.populateWith(CPythonInterpreter.call(value, method, params.map(_.value), Seq())).as[Dynamic]
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    Any.populateWith(CPythonInterpreter.call(
      value, method,
      params.filter(_._1.isEmpty).map(_._2.value),
      params.filter(_._1.nonEmpty).map(t => (t._1, t._2.value))
    )).as[Dynamic]
  }

  def selectDynamic(term: String): Dynamic = {
    Any.populateWith(CPythonInterpreter.select(value, term)).as[Dynamic]
  }

  def updateDynamic(name: String)(newValue: Any): Unit = {
    CPythonInterpreter.update(value, name, newValue.value)
  }

  def bracketAccess(key: Any): Dynamic = {
    Any.populateWith(CPythonInterpreter.selectBracket(value, key.value)).as[Dynamic]
  }

  def bracketUpdate(key: Any, newValue: Any): Unit = {
    CPythonInterpreter.updateBracket(value, key.value, newValue.value)
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
