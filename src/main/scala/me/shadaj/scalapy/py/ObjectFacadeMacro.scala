package me.shadaj.scalapy.py

import scala.reflect.macros.whitebox

object ObjectFacadeMacro {
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
