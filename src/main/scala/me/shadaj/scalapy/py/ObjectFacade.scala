package me.shadaj.scalapy.py

import jep.Jep

import scala.reflect.macros.whitebox
import scala.language.experimental.macros

class ObjectFacade(originalObject: Object)(implicit jep: Jep) extends Object(originalObject.varId) {
  final val toDynamic = originalObject.asInstanceOf[DynamicObject]

  override def toString: String = originalObject.toString

  protected def native[T]: T = macro ObjectFacadeImpl.native_impl[T]
  protected def nativeNamed[T]: T = macro ObjectFacadeImpl.native_named_impl[T]

  override def finalize(): Unit = {} // let the originalObject handle this
}

object ObjectFacadeImpl {
  def native_impl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[T] = {
    import c.universe._

    val method = c.internal.enclosingOwner.asMethod
    val methodName = method.name.toString
    val returnType = method.returnType
    val paramss = method.paramLists

    paramss.headOption match {
      case Some(params) =>
        val paramExprs = params.map(_.name)
        c.Expr[T](q"toDynamic.applyDynamic($methodName)(..$paramExprs).as[$returnType]")
      case scala.None =>
        c.Expr[T](q"toDynamic.selectDynamic($methodName).as[$returnType]")
    }
  }

  def native_named_impl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[T] = {
    import c.universe._

    val method = c.internal.enclosingOwner.asMethod
    val methodName = method.name.toString
    val returnType = method.returnType
    val paramss = method.paramLists

    paramss.headOption match {
      case Some(params) =>
        val paramExprs = params.map { p =>
          val paramName = q"${p.asTerm}".toString
          s"""("${p.name}", $paramName)"""
        }

        c.Expr[T](c.parse(s"""toDynamic.applyDynamicNamed("$methodName")(${paramExprs.mkString(",")}).as[$returnType]"""))

      case scala.None =>
        c.Expr[T](q"toDynamic.selectDynamic($methodName).as[$returnType]")
    }
  }
}
