package me.shadaj.scalapy.facadegen

import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.SeqConverters
import java.io.File
import java.io.PrintWriter

import scala.collection.mutable

object Main extends App {
  val mypy = py.module("mypy")

  val mypyBuild = py.module("mypy.build")
  val mypyOptions = py.module("mypy.options")
  val mypyModulefinder = py.module("mypy.modulefinder")
  val mypyNodes = py.module("mypy.nodes")

  val modules = Seq("builtins", "typing", "numbers", "enum", "mip", "mip.entities", "mip.constants", "mip.model", "torch", "torch.tensor", "torch._C", "torch.cuda", "torch.storage")

  val temporaryFile = File.createTempFile("mypy-input.py", null)
  val fileWriter = new PrintWriter(temporaryFile)
  modules.foreach { module =>
    fileWriter.println(s"import $module")
    fileWriter.println(s"x_$module: $module = $module")
  }
  fileWriter.close()

  val buildResult = mypyBuild.build(
    Seq(
      mypyModulefinder.BuildSource(
        temporaryFile.getAbsolutePath(),
        py.None
      )
    ).toPythonProxy,
    mypyOptions.Options()
  )

  val TypeInfo = mypyNodes.TypeInfo
  val FuncBase = mypyNodes.FuncBase
  val Var = mypyNodes.Var
  val FuncItem = mypyNodes.FuncItem
  val FuncDef = mypyNodes.FuncDef
  val OverloadedFuncDef = mypyNodes.OverloadedFuncDef
  val Decorator = mypyNodes.Decorator

  val types = py.module("mypy.types")
  val Type = types.Type
  val AnyType = types.AnyType
  val NoneType = types.NoneType
  val Instance = types.Instance
  val TypeVarType = types.TypeVarType
  val CallableType = types.CallableType

  def scalaTypeForPython(pyType: py.Dynamic, recordMethodTypeArgs: Option[mutable.Set[String]] = None): (String, String, Boolean) = {
    if (py.Dynamic.global.isinstance(pyType, TypeInfo).as[Boolean]) {
      val name = pyType.fullname.as[String]
      if (modules.contains(name) || modules.contains(name.split('.').init.mkString("."))) {
        val nameDropBuiltins = name.replace("builtins", "stdlib")
        val safeName = nameDropBuiltins.split('.').map(a => makeNameSafe(a)).mkString(".")
        (s"me.shadaj.scalapy.$safeName", s"me.shadaj.scalapy.$safeName", false)
      } else {
        println(s"missing type: $name")
        (s"/* $pyType */ me.shadaj.scalapy.py.Any", "me.shadaj.scalapy.py.Any", false)
      }
    } else if (py.Dynamic.global.isinstance(pyType, Instance).as[Boolean]) {
      val classTypeScalafied = scalaTypeForPython(pyType.selectDynamic("type"), recordMethodTypeArgs)
      if (classTypeScalafied._1 == classTypeScalafied._2) {
        val addedArgs = py.Dynamic.global.list(pyType.args).as[Seq[py.Dynamic]]
        val addedArgsStrings = addedArgs.map(a => scalaTypeForPython(a, recordMethodTypeArgs))
        val addedArgsCode_1 = if (addedArgsStrings.isEmpty) "" else s"[${addedArgsStrings.map(_._1).mkString(", ")}]"
        val addedArgsCode_2 = if (addedArgsStrings.isEmpty) "" else s"[${addedArgsStrings.map(_._2).mkString(", ")}]"
        (classTypeScalafied._1 + addedArgsCode_1, classTypeScalafied._2 + addedArgsCode_2, classTypeScalafied._3 || addedArgsStrings.exists(_._3))
      } else classTypeScalafied
    } else if (py.Dynamic.global.isinstance(pyType, TypeVarType).as[Boolean]) {
      if (pyType.id.raw_id.as[Int] < 0) {
        recordMethodTypeArgs.foreach(_.add(pyType.name.as[String]))
      }
      (pyType.name.as[String], pyType.name.as[String], true)
    } else if (py.Dynamic.global.isinstance(pyType, CallableType).as[Boolean]) {
      /*val argTypes = pyType.arg_types.as[Seq[py.Dynamic]].map(a => scalaTypeForPython(a, recordMethodTypeArgs))
      val retType = scalaTypeForPython(pyType.ret_type, recordMethodTypeArgs)
      val type_1 = s"(${argTypes.map(_._1).mkString(", ")}) => ${retType._1}"
      val type_2 = s"(${argTypes.map(_._2).mkString(", ")}) => ${retType._2}"
      // TODO: handle arg kinds
      (type_1, type_2, argTypes.exists(_._3) || retType._3)*/
      (s"me.shadaj.scalapy.py.Any", "me.shadaj.scalapy.py.Any", false)
    } else if (py.Dynamic.global.isinstance(pyType, AnyType).as[Boolean]) {
      (s"me.shadaj.scalapy.py.Any", "me.shadaj.scalapy.py.Any", false)
    } else if (py.Dynamic.global.isinstance(pyType, NoneType).as[Boolean]) {
      ("me.shadaj.scalapy.py.None", "me.shadaj.scalapy.py.None", false)
    } else {
      (s"/* $pyType */ me.shadaj.scalapy.py.Any", "me.shadaj.scalapy.py.Any", false)
    }
  }

  def processDefinition(defn: py.Dynamic): String = {
    if (py.Dynamic.global.isinstance(defn, TypeInfo).as[Boolean]) {
      processTypeInfo(defn)
    } else if (py.Dynamic.global.isinstance(defn, FuncDef).as[Boolean]) {
      processFuncDef(defn)
    } else if (py.Dynamic.global.isinstance(defn, Var).as[Boolean]) {
      processVarDef(defn)
    } else {
      try {
        s"/* $defn */"
      } catch {
        case _: Throwable => s"/* cannot compute str */"
      }
    }
  }

  def processTypeInfo(info: py.Dynamic): String = {
    val baseTypes = info.bases.as[Seq[py.Dynamic]]
    val baseScalaTypes = baseTypes.map(t => scalaTypeForPython(t))

    val extendsCode = if (baseScalaTypes.isEmpty) "extends me.shadaj.scalapy.py.Any" else {
      "extends " + baseScalaTypes.distinctBy(_._2).map(_._1).mkString(" with ")
    }

    val members = info.names.as[Map[String, py.Dynamic]]
    val membersCode = members.map(kv => genClassMember(info, kv._1, kv._2.node)).mkString("\n")
    val staticMembersCode = members.map(kv => genClassMember(info, kv._1, kv._2.node, true)).mkString("\n")

    val typeVariables = info.type_vars.as[Seq[String]]
    val typeVariablesCode = if (typeVariables.isEmpty) "" else s"[${typeVariables.map(_ + " <: me.shadaj.scalapy.py.Any").mkString(", ")}]"

    s"""@py.native trait ${makeNameSafe(info.name.as[String])}$typeVariablesCode $extendsCode {
       |$membersCode
       |}
       |
       |object ${makeNameSafe(info.name.as[String])} extends me.shadaj.scalapy.py.StaticValue(as[me.shadaj.scalapy.py.Dynamic].selectDynamic("${info.name.as[String]}")) with py.Object {
       |$staticMembersCode
       |}""".stripMargin
  }

  def genClassMember(classInfo: py.Dynamic, name: String, member: py.Dynamic, static: Boolean = false): String = {
    if (py.Dynamic.global.isinstance(member, FuncBase).as[Boolean]) {
      genClassFunction(classInfo, name, member, static)
    } else if (py.Dynamic.global.isinstance(member, Var).as[Boolean] && !member.is_classvar.as[Boolean]) {
      if (static) {
        s"/* attribute: $name */"
      } else {
        genClassAttribute(classInfo, name, member)
      }
    } else {
      s"/* member: $name */"
    }
  }

  def genClassFunction(classInfo: py.Dynamic, name: String, function: py.Dynamic, static: Boolean): String = {
    val funcStatic = function.is_static.as[Boolean] || name == "__init__"

    if (funcStatic == static) {
      if (!static || name == "__init__") {
        genClassMethod(classInfo, name, function, name == "__init__")
      } else if (py.Dynamic.global.isinstance(function, FuncDef).as[Boolean]) {
        processFuncDef(function)
      } else {
        s"/* function: $name */"
      }
    } else ""
  }

  val keyWords = Set("type", "super", "object", "match", "new", "val", "var", "throw", "final", "implicit", "override", "apply")
  val operatorMapping = Map(
    "__add__" -> "+",
    "__sub__" -> "-",
    "__mul__" -> "*",
    "__truediv__" -> "/",

    "__lt__" -> "<",
    "__le__" -> "<=",
    "__ge__" -> ">=",
    "__gt__" -> ">",

    "__init__" -> "apply"
  )

  def makeNameSafe(arg: String, varMode: Boolean = false): String = {
    if (operatorMapping.contains(arg)) {
      operatorMapping(arg)
    } else if (keyWords.contains(arg) || arg.contains("-")) {
      s"`$arg`"
    } else if (varMode && arg.endsWith("_")) {
      s"`$arg`"
    } else if (arg == "value") {
      s"member_$arg"
    } else arg
  }

  def getMethodArgTypesDedup(argTypes: Seq[py.Dynamic]): Seq[String] = {
    argTypes.tail.map(t => scalaTypeForPython(t)._2)
  }

  def genClassMethod(classInfo: py.Dynamic, name: String, method: py.Dynamic, static: Boolean = false, existing: mutable.Set[String] = mutable.Set.empty, setter: Boolean = false): String = {
    if (py.Dynamic.global.isinstance(method, FuncItem).as[Boolean]) {
      val argNames = method.arg_names.as[Seq[String]].tail
      val signature = method.selectDynamic("type")
      val argTypesUntailed = if (signature == py.None) {
        method.arg_names.as[Seq[py.Any]].map(_ => AnyType)
      } else signature.arg_types.as[Seq[py.Dynamic]]
      val argTypes = argTypesUntailed.tail
      val argKinds = (if (signature == py.None) {
        method.arg_names.as[Seq[py.Any]].map(_ => 0)
      } else signature.arg_kinds.as[Seq[Int]]).tail
      val returnType = if (signature == py.None) AnyType else if (name == "__init__") classInfo else signature.ret_type

      val selfArgsTypesDedup = getMethodArgTypesDedup(argTypesUntailed)

      val isOverride = classInfo.mro.as[Seq[py.Dynamic]].tail.exists { superClass =>
        val superNames = superClass.names.as[Map[String, py.Dynamic]]
        val superScalaType = scalaTypeForPython(superClass)
        if (superScalaType._1 == superScalaType._2) {
          superNames.get(name).nonEmpty
        } else {
          false
        }
      }

      if (isOverride && !static) s"/* skip override: $name */"
      else if (name == "clone") s"/* skip illegal name: $name */" else {
        val typeVariables = mutable.Set[String]()

        val argsCode = argNames.zip(argTypes).zip(argKinds).map { case ((name, tpe), kind) =>
          if (kind == 1 || kind == 5) {
            val (fullType, shortType, _) = scalaTypeForPython(tpe, Some(typeVariables))
            s"${makeNameSafe(name)} : ${fullType} = null.asInstanceOf[$shortType]"
          } else {
            s"${makeNameSafe(name)} : ${scalaTypeForPython(tpe, Some(typeVariables))._1}"
          }
        }.mkString(", ")

        val keywordArgs = argNames.map(a => s""""$a" -> ${makeNameSafe(a)}""").mkString(", ")

        val (retTypeStringPreArgs, cleanRetType, needsImplicitReader) = scalaTypeForPython(returnType, Some(typeVariables))

        val retTypeString = if (name == "__init__") {
          val classTypeVariables = classInfo.type_vars.as[Seq[String]]
          typeVariables.addAll(classTypeVariables)

          retTypeStringPreArgs + (if (classTypeVariables.isEmpty) "" else s"[${classTypeVariables.mkString(", ")}]")
        } else retTypeStringPreArgs

        val typeVariablesCode = if (typeVariables.isEmpty) "" else s"[${typeVariables.map(_ + " <: me.shadaj.scalapy.py.Any").mkString(", ")}]"

        val maybeImplicitReader = if (needsImplicitReader) {
          s"(implicit retReader: me.shadaj.scalapy.readwrite.Reader[$retTypeString])"
        } else ""

        val out = if (setter) {
          assert(argNames.size == 1)
          s"""def ${makeNameSafe(name)}_=$typeVariablesCode($argsCode)$maybeImplicitReader: Unit = as[me.shadaj.scalapy.py.Dynamic].updateDynamic("$name")(${makeNameSafe(argNames.head)})"""
        } else if (name == "__init__") {
          s"""def ${makeNameSafe(name)}$typeVariablesCode($argsCode)$maybeImplicitReader: $retTypeString = as[me.shadaj.scalapy.py.Dynamic].applyNamed($keywordArgs).as[$retTypeString]"""
        } else {
          s"""def ${makeNameSafe(name)}$typeVariablesCode($argsCode)$maybeImplicitReader: $retTypeString = as[me.shadaj.scalapy.py.Dynamic].applyDynamicNamed("$name")($keywordArgs).as[$retTypeString]"""
        }

        val cleanOut = if (setter) {
          s"""def ${makeNameSafe(name)}_=(${argNames.size}): Unit"""
        } else {
          s"""def ${makeNameSafe(name)}(${argNames.size}): $cleanRetType"""
        }

        if (existing.contains(cleanOut)) {
          s"/* skip conflict: $name */"
        } else {
          existing.add(cleanOut)
          out
        }
      }
    } else if (py.Dynamic.global.isinstance(method, Decorator).as[Boolean]) {
      val decorators = method.decorators.as[Seq[py.Dynamic]]
      genClassMethod(classInfo, name, method.func, static, existing, setter = decorators.exists(_.name.as[String] == "setter"))
    } else if (py.Dynamic.global.isinstance(method, OverloadedFuncDef).as[Boolean]) {
      val parts = method.items.as[Seq[py.Dynamic]]
      val dupCatcher = mutable.Set.empty[String]
      parts.map(p => genClassMethod(classInfo, name, p, static, dupCatcher, setter)).mkString("\n")
    } else {
      s"/* method: $name */"
    }
  }

  def genClassAttribute(classInfo: py.Dynamic, name: String, attribute: py.Dynamic): String = {
    val returnType = attribute.selectDynamic("type")

    val isOverride = classInfo.mro.as[Seq[py.Dynamic]].tail.exists { superClass =>
      val superNames = superClass.names.as[Map[String, py.Dynamic]]
      val superScalaType = scalaTypeForPython(superClass)
      if (superScalaType._1 == superScalaType._2) {
        superNames.get(name).nonEmpty
      } else {
        false
      }
    }

    if (isOverride) s"/* skip override: $name */" else {
      val (retTypeString, _, needsImplicitReader) = scalaTypeForPython(returnType)

      val maybeImplicitReader = if (needsImplicitReader) {
        s"(implicit retReader: me.shadaj.scalapy.readwrite.Reader[$retTypeString])"
      } else ""

      s"""def ${makeNameSafe(name, true)}$maybeImplicitReader: $retTypeString = as[me.shadaj.scalapy.py.Dynamic].selectDynamic("$name").as[$retTypeString]""".stripMargin
    }
  }

  def processFuncDef(method: py.Dynamic): String = {
    val argNames = method.arg_names.as[Seq[String]]
    val signature = method.selectDynamic("type")
    val argTypes = if (signature == py.None) {
      argNames.map(_ => AnyType)
    } else signature.arg_types.as[Seq[py.Dynamic]]
    val returnType = if (signature == py.None) AnyType else signature.ret_type

    val typeVariables = mutable.Set[String]()

    val argsCode = argNames.zip(argTypes).map { case (name, tpe) =>
      s"${makeNameSafe(name)} : ${scalaTypeForPython(tpe, Some(typeVariables))._1}"
    }.mkString(", ")

    val justArgNames = argNames.map(a => makeNameSafe(a)).mkString(", ")

    val (retTypeString, _, needsImplicitReader) = scalaTypeForPython(returnType, Some(typeVariables))

    val typeVariablesCode = if (typeVariables.isEmpty) "" else s"[${typeVariables.map(_ + " <: me.shadaj.scalapy.py.Any").mkString(", ")}]"

    val maybeImplicitReader = if (needsImplicitReader) {
      s"(implicit retReader: me.shadaj.scalapy.readwrite.Reader[$retTypeString])"
    } else ""

    val methodName = method.name.as[String]

    s"""def ${makeNameSafe(methodName)}$typeVariablesCode($argsCode)$maybeImplicitReader: $retTypeString = as[me.shadaj.scalapy.py.Dynamic].applyDynamic("$methodName")($justArgNames).as[$retTypeString]""".stripMargin
  }

  def processVarDef(attribute: py.Dynamic): String = {
    val returnType = attribute.selectDynamic("type")

    val (retTypeString, _, needsImplicitReader) = scalaTypeForPython(returnType)

    val maybeImplicitReader = if (needsImplicitReader) {
      s"(implicit retReader: me.shadaj.scalapy.readwrite.Reader[$retTypeString])"
    } else ""

    val name = attribute.name.as[String]

    s"""def ${makeNameSafe(name, true)}$maybeImplicitReader: $retTypeString = as[me.shadaj.scalapy.py.Dynamic].selectDynamic("$name").as[$retTypeString]""".stripMargin
  }

  modules.foreach { m =>
    val localDefinitions = buildResult.manager.modules.bracketAccess(m).names.as[Map[String, py.Dynamic]].filter { case (_, value) =>
      value.node.fullname == py.None || value.node.fullname.as[String].split('.').init.mkString(".") == m
    }

    val out = new File(s"$m.scala")
    val outWriter = new PrintWriter(out)
    val dotSplit = m.split('.')
    outWriter.println(if (dotSplit.size == 1) s"package me.shadaj.scalapy" else s"package me.shadaj.scalapy.${dotSplit.init.mkString(".")}")
    outWriter.println("import me.shadaj.scalapy.py")
    outWriter.println(s"package object ${if (m == "builtins") "stdlib" else dotSplit.last} extends me.shadaj.scalapy.py.StaticModule(${"\"" + m + "\""}) {")
    outWriter.println(localDefinitions.values.map(v => processDefinition(v.node)).mkString("\n"))
    outWriter.println("}")
    outWriter.close()
  }
}
