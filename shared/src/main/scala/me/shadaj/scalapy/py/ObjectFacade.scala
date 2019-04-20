package me.shadaj.scalapy.py

import scala.reflect.macros.whitebox
import scala.language.experimental.macros

trait ObjectFacade extends Object {
  private[py] var value: PyValue = null

  protected def native[T]: T = macro ObjectFacadeImpl.native_impl[T]
  protected def nativeNamed[T]: T = macro ObjectFacadeImpl.native_named_impl[T]

  override def finalize(): Unit = {} // let the originalObject handle this
}

object ObjectFacade {
  implicit def getCreator[F <: ObjectFacade]: FacadeCreator[F] = macro ObjectFacadeImpl.creator[F]
}

abstract class FacadeCreator[F <: ObjectFacade] {
  def create: F
}

object ObjectFacadeImpl {
  def creator[T <: ObjectFacade](c: whitebox.Context)(implicit tag: c.WeakTypeTag[T]): c.Expr[FacadeCreator[T]] = {
    import c.universe._
    
    c.Expr[FacadeCreator[T]](q"""new _root_.me.shadaj.scalapy.py.FacadeCreator[${tag.tpe}] {
      def create = new ${tag.tpe} {}
    }""")
  }

  def native_impl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[T] = {
    import c.universe._

    val method = c.internal.enclosingOwner.asMethod
    val methodName = method.name.toString
    val returnType = method.returnType
    val paramss = method.paramLists

    paramss.headOption match {
      case Some(params) =>
        val paramExprs = params.map(_.name)
        c.Expr[T](q"asDynamic.applyDynamic($methodName)(..$paramExprs).as[$returnType]")
      case scala.None =>
        c.Expr[T](q"asDynamic.selectDynamic($methodName).as[$returnType]")
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

        c.Expr[T](c.parse(s"""asDynamic.applyDynamicNamed("$methodName")(${paramExprs.mkString(",")}).as[$returnType]"""))

      case scala.None =>
        c.Expr[T](q"asDynamic.selectDynamic($methodName).as[$returnType]")
    }
  }
}
