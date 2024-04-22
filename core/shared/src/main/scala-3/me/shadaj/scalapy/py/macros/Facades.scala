package me.shadaj.scalapy.py.macros

import scala.quoted.*

import me.shadaj.scalapy.py.Any
import me.shadaj.scalapy.py.FacadeCreator

object Helper {
  def classDynamicSymbol(using Quotes) =
    quotes.reflect.Symbol.requiredClass("me.shadaj.scalapy.py.Dynamic")
  
  def classReaderSymbol(using Quotes) =
    quotes.reflect.Symbol.requiredClass("me.shadaj.scalapy.readwrite.Reader")

  def classWriterSymbol(using Quotes) =
    quotes.reflect.Symbol.requiredClass("me.shadaj.scalapy.readwrite.Writer")

  def classAnySymbol(using Quotes) =
    quotes.reflect.Symbol.requiredClass("me.shadaj.scalapy.py.Any")
   
  def methodFromSymbol(using Quotes) =
    quotes.reflect.Symbol.requiredMethod("me.shadaj.scalapy.py.Any.from")

  def readerTypeRepr(using Quotes) =
    quotes.reflect.TypeIdent(classReaderSymbol).tpe

  def writerTypeRepr(using Quotes) =
    quotes.reflect.TypeIdent(classWriterSymbol).tpe

  def dynamicTypeRepr(using Quotes) = 
    quotes.reflect.TypeIdent(classDynamicSymbol).tpe
}

object FacadeImpl {
  def methodSymbolParameterRefs(using Quotes)(methodSymbol: quotes.reflect.Symbol): List[List[quotes.reflect.Term]] = {
    import quotes.reflect.*
    
    val termParameterSymbols = methodSymbol.paramSymss.filterNot(_.headOption.exists(_.isType))
    termParameterSymbols.map(_.map(Ref.apply))
  }

  def resolveThis(using Quotes): quotes.reflect.Term = {
    import quotes.reflect.*

    var sym = Symbol.spliceOwner
    while sym != null && !sym.isClassDef do
      sym = sym.owner
    This(sym)
  }

  def searchImplicit(using Quotes)(typeReprParameter: quotes.reflect.TypeRepr): quotes.reflect.Term = {
    import quotes.reflect.*

    Implicits.search(typeReprParameter) match {
      case success: ImplicitSearchSuccess => {
        success.tree
      }
      case _ => {
        report.throwError(s"There is no implicit for ${typeReprParameter.show}")
      }
    }
  }

  def creator[T <: Any](using Type[T], Quotes): Expr[FacadeCreator[T]] =  {
    import quotes.reflect.*
    if (TypeRepr.of[T].typeSymbol.flags.is(Flags.Trait))
      report.error(s"Facade definitions in Scala 3 must be classes, but ${TypeRepr.of[T].show} was defined as a trait")

    // new T{}
    val newExpr: Expr[T] = Apply(Select.unique(New(TypeTree.of[T]), "<init>"), List()).asExprOf[T]

    val facadeCreatorExpr = '{
      new FacadeCreator[T]{
        def create: T = ${newExpr}
      }
    }

    facadeCreatorExpr.asExprOf[FacadeCreator[T]]
  }


  def native_impl[T: Type](using Quotes): Expr[T] = {
    import quotes.reflect.*

    if (!Symbol.spliceOwner.owner.owner.hasAnnotation(Symbol.requiredClass("me.shadaj.scalapy.py.native"))) {
      report.throwError("py.native implemented functions can only be declared inside traits annotated as @py.native")
    }

    def constructASTforMethodFrom(arg: quotes.reflect.Term) = {
      val applyArgTypeToWriter = Helper.writerTypeRepr.appliedTo(arg.tpe.widen)
      val tree = Apply(Apply(TypeApply(Ref(Helper.methodFromSymbol),List(Inferred(arg.tpe.widen))),List(arg)),
      List(searchImplicit(applyArgTypeToWriter)))
      tree
    }

    val evidenceForDynamic = searchImplicit(Helper.readerTypeRepr.appliedTo(Helper.dynamicTypeRepr))
    val evidenceForTypeT = searchImplicit(Helper.readerTypeRepr.appliedTo(TypeTree.of[T].tpe))

    val methodSymbol = Symbol.spliceOwner.owner
    val methodName = methodSymbol.name
    val refss = methodSymbolParameterRefs(methodSymbol)

    if (refss.length > 1) {
      report.throwError(s"Method $methodName has curried parameter lists, which are currently not supported in facades")
    }

    val args = refss.headOption.toList.flatten


    if (Symbol.spliceOwner.owner.hasAnnotation(Symbol.requiredClass("me.shadaj.scalapy.py.PyBracketAccess"))) {
      if (args.size == 0) {
        report.throwError("PyBracketAccess functions require at least one parameter")
      } else if (args.size == 1) {
        val bracketAccessAST = Apply(TypeApply(Select.unique(Apply(Select.unique(Apply(TypeApply(
          Select.unique(resolveThis,"as"),List(TypeIdent(Helper.classDynamicSymbol))),List(evidenceForDynamic)),  
          "bracketAccess"),List(constructASTforMethodFrom(args(0)))),"as"), List(TypeTree.of[T])),List(evidenceForTypeT))

        bracketAccessAST.asExprOf[T]
      } else if (args.size == 2) {
        val bracketUpdateAST = Apply(Select.unique(Apply(TypeApply(
          Select.unique(resolveThis,"as"),List(TypeIdent(Helper.classDynamicSymbol))),List(evidenceForDynamic)),  
          "bracketUpdate"),List(constructASTforMethodFrom(args(0)), constructASTforMethodFrom(args(1))))

          bracketUpdateAST.asExprOf[T]
      } else {
        report.throwError("Too many parameters to PyBracketAccess function")
      }
    }
    else {
      if (refss.isEmpty) {
        val selectDynamicAST = Apply(TypeApply(Select.unique(Apply(Select.unique(Apply(TypeApply(
          Select.unique(resolveThis,"as"),List(TypeIdent(Helper.classDynamicSymbol))),List(evidenceForDynamic)),  
          "selectDynamic"),List(Expr(methodName).asTerm)),"as"), List(TypeTree.of[T])),List(evidenceForTypeT))

        selectDynamicAST.asExprOf[T]
      } else {
        val typedVarargs = Typed(Inlined(None, Nil, Repeated(args.map(arg => constructASTforMethodFrom(arg)),
          TypeIdent(Helper.classAnySymbol))),Applied(TypeIdent(defn.RepeatedParamClass),List(TypeIdent(Helper.classAnySymbol))))

        val applyDynamicAST = Apply(TypeApply(Select.unique(Apply(Apply(Select.unique(Apply(TypeApply(
          Select.unique(resolveThis,"as"),List(TypeIdent(Helper.classDynamicSymbol))),List(evidenceForDynamic)),
          "applyDynamic"),List(Expr(methodName).asTerm)),List(typedVarargs)),"as"),List(TypeTree.of[T])),
          List(evidenceForTypeT))

        applyDynamicAST.asExprOf[T]
      }
    }
  }

  def native_named_impl[T: Type](using Quotes): Expr[T] = {
    import quotes.reflect.*

    if (!Symbol.spliceOwner.owner.owner.hasAnnotation(Symbol.requiredClass("me.shadaj.scalapy.py.native"))) {
      report.throwError("py.native implemented functions can only be declared inside traits annotated as @py.native")
    }

    val evidenceForDynamic = searchImplicit(Helper.readerTypeRepr.appliedTo(Helper.dynamicTypeRepr))
    val evidenceForTypeT = searchImplicit(Helper.readerTypeRepr.appliedTo(TypeTree.of[T].tpe))

    val methodSymbol = Symbol.spliceOwner.owner
    val methodName = methodSymbol.name
    val refss = methodSymbolParameterRefs(methodSymbol)

    if (refss.length > 1) {
      report.throwError(s"Method $methodName has curried parameter lists, which are currently not supported in facades")
    }

    val args = refss.headOption.toList.flatten
    
    if (args.isEmpty) {
      val selectDynamicAST = Apply(TypeApply(Select.unique(Apply(Select.unique(Apply(TypeApply(
        Select.unique(resolveThis,"as"),List(TypeIdent(Helper.classDynamicSymbol))),List(evidenceForDynamic)),  
        "selectDynamic"),List(Expr(methodName).asTerm)),"as"),List(TypeTree.of[T])),List(evidenceForTypeT))

      selectDynamicAST.asExprOf[T]
    } else {
      def constructASTforMethodFrom(arg: quotes.reflect.Term) = {
        val applyArgTypeToWriter = Helper.writerTypeRepr.appliedTo(arg.tpe.widen)
        val tree = Apply(Apply(TypeApply(Ref(Helper.methodFromSymbol),List(Inferred(arg.tpe.widen))),List(arg)),
        List(searchImplicit(applyArgTypeToWriter)))
        tree
      }

      def constructASTforTuple(parameterName: quotes.reflect.Term, arg2: quotes.reflect.Term) = {
        val tree = Expr.ofTuple((parameterName.asExprOf[String], arg2.asExpr)).asTerm
        tree
      }
      
      val tupleType = Inferred(TypeTree.of[Tuple2].tpe.appliedTo(List(TypeTree.of[String].tpe,TypeIdent(Helper.classAnySymbol).tpe)))
      val tupleList = args.map(arg => constructASTforTuple(Expr(arg.show).asTerm, constructASTforMethodFrom(arg)))
      val typedVarargs = Typed(Inlined(None,Nil,Repeated(tupleList,tupleType)),
        Applied(TypeIdent(defn.RepeatedParamClass),List(tupleType)))

      val applyDynamicNamedAST = Apply(TypeApply(Select.unique(Apply(Apply(Select.unique(Apply(TypeApply(
        Select.unique(resolveThis,"as"),List(TypeIdent(Helper.classDynamicSymbol))),List(evidenceForDynamic)),
        "applyDynamicNamed"),List(Expr(methodName).asTerm)),List(typedVarargs)),"as"),List(TypeTree.of[T])),
        List(evidenceForTypeT))

      applyDynamicNamedAST.asExprOf[T]
    }
  }
}
