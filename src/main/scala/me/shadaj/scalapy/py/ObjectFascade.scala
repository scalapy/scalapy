package me.shadaj.scalapy.py

import jep.Jep

import scala.reflect.macros.Context
import scala.language.experimental.macros

class ObjectFascade(originalObject: Object)(implicit jep: Jep) extends Ref(originalObject.expr) {
  protected val dynamic = originalObject.asInstanceOf[DynamicObject]

  override def toString: String = originalObject.toString

  protected def native[T]: T = macro ObjectFascade.native_impl[T]
  protected def nativeNamed[T]: T = macro ObjectFascade.native_named_impl[T]
}

object ObjectFascade {
  def native_impl[T: c.WeakTypeTag](c: Context): c.Expr[T] = {
    import c.universe._

    val method = c.internal.enclosingOwner.asMethod
    val methodName = method.name.toString
    val returnType = method.returnType
    val paramss = method.paramLists

    paramss.headOption match {
      case Some(params) =>
        val paramExprs = params.map { p =>
          q"${p.asTerm}".toString
        }

        c.Expr[T](c.parse(s"""dynamic.applyDynamic("$methodName")(${paramExprs.mkString(",")}).as[${returnType}]"""))
      case scala.None =>
        c.Expr[T](q"""dynamic.selectDynamic($methodName).as[$returnType]""")
    }
  }

  def native_named_impl[T: c.WeakTypeTag](c: Context): c.Expr[T] = {
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

        c.Expr[T](c.parse(s"""dynamic.applyDynamicNamed("$methodName")(${paramExprs.mkString(",")}).as[${returnType}]"""))

      case scala.None =>
        c.Expr[T](q"""dynamic.selectDynamic($methodName).as[$returnType]""")
    }
  }
}
