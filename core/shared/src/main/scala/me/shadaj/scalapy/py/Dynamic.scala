package me.shadaj.scalapy.py

import scala.language.dynamics

import me.shadaj.scalapy.interpreter.CPythonInterpreter

@native class Dynamic extends Any with AnyDynamics

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

trait AnyDynamics extends Any with scala.Dynamic {
  def apply(params: Any*): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(
    //   CPythonInterpreter.call(value, params.map(_.value), Seq())
    // )
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.call(value, params.map(_.value), Seq())
    dynamic
  }

  def applyDynamic(method: String)(params: Any*): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(
    //   CPythonInterpreter.call(value, method, params.map(_.value), Seq())
    // )
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.call(value, method, params.map(_.value), Seq())
    dynamic
    // new FacadeValueProvider(CPythonInterpreter.call(value, method, params.map(_.value), Seq())) with Dynamic
  }

  def applyNamed(params: (String, Any)*): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.call(
    //   value,
    //   params.filter(t => t._1.isEmpty && t._2 != null).map(_._2.value),
    //   params.filter(t => t._1.nonEmpty && t._2 != null).map(t => (t._1, t._2.value))
    // ))
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.call(
      value,
      params.filter(t => t._1.isEmpty && t._2 != null).map(_._2.value),
      params.filter(t => t._1.nonEmpty && t._2 != null).map(t => (t._1, t._2.value))
    )
    dynamic
  }

  def applyDynamicNamed(method: String)(params: (String, Any)*): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.call(
    //   value, method,
    //   params.filter(t => t._1.isEmpty && t._2 != null).map(_._2.value),
    //   params.filter(t => t._1.nonEmpty && t._2 != null).map(t => (t._1, t._2.value))
    // ))
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.call(
      value, method,
      params.filter(t => t._1.isEmpty && t._2 != null).map(_._2.value),
      params.filter(t => t._1.nonEmpty && t._2 != null).map(t => (t._1, t._2.value))
    )
    dynamic
  }

  def selectDynamic(term: String): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(
    //   CPythonInterpreter.select(value, term)
    // )
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.select(value, term)
    dynamic
  }

  def updateDynamic(name: String)(newValue: Any): Unit = {
    CPythonInterpreter.update(value, name, newValue.value)
  }

  def bracketAccess(key: Any): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(
    //   CPythonInterpreter.selectBracket(value, key.value)
    // )
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.selectBracket(value, key.value)
    dynamic
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
    // implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.unaryPos(value))
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.unaryPos(value)
    dynamic
  }

  def unary_-(): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.unaryNeg(value))
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.unaryNeg(value)
    dynamic
  }

  def +(that: Any): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryAdd(value, that.value))
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.binaryAdd(value, that.value)
    dynamic
  }

  def -(that: Any): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binarySub(value, that.value))
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.binarySub(value, that.value)
    dynamic
  }

  def *(that: Any): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryMul(value, that.value))
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.binaryMul(value, that.value)
    dynamic
  }

  def /(that: Any): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryDiv(value, that.value))
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.binaryDiv(value, that.value)
    dynamic
  }

  def %(that: Any): Dynamic = {
    // implicitly[FacadeCreator[Dynamic]].create(CPythonInterpreter.binaryMod(value, that.value))
    val dynamic = implicitly[FacadeCreator[Dynamic]].create
    dynamic.rawValue = CPythonInterpreter.binaryMod(value, that.value)
    dynamic
  }
}
