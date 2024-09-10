import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scala.sys.process._

ThisBuild / organization := "dev.scalapy"

lazy val scala212Version = "2.12.20"
lazy val scala213Version = "2.13.14"
lazy val scala3Version = "3.5.0"
lazy val supportedScalaVersions = List(scala212Version, scala213Version, scala3Version)

ThisBuild / scalaVersion := scala213Version

lazy val scalapy = project.in(file(".")).aggregate(
  macrosJVM, macrosNative,
  coreJVM, coreNative,
).settings(
  publish := {},
  publishLocal := {}
)

addCommandAlias(
  "publishSignedAll",
  (scalapy: ProjectDefinition[ProjectReference])
    .aggregate
    .map(p => s"+ ${p.asInstanceOf[LocalProject].project}/publishSigned")
    .mkString(";", ";", "")
)

lazy val macros = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("coreMacros"))
  .settings(
    name := "scalapy-macros",
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq(
          "org.scala-lang" % "scala-reflect" % scalaVersion.value
        )
        case _ => Seq.empty
      }
    },
    Compile / unmanagedSourceDirectories += {
      val sharedSourceDir = (ThisBuild / baseDirectory).value / "coreMacros/src/main"
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => sharedSourceDir / "scala-2"
        case _ => sharedSourceDir / "scala-3"
      }
    },
    crossScalaVersions := supportedScalaVersions
  )

lazy val macrosJVM = macros.jvm
lazy val macrosNative = macros.native

lazy val pythonLdFlags = {
  val withoutEmbed = "python3-config --ldflags".!!
  if (withoutEmbed.contains("-lpython")) {
    withoutEmbed.split(' ').map(_.trim).filter(_.nonEmpty).toSeq
  } else {
    val withEmbed = "python3-config --ldflags --embed".!!
    withEmbed.split(' ').map(_.trim).filter(_.nonEmpty).toSeq
  }
}

lazy val pythonLibsDir = {
  pythonLdFlags.find(_.startsWith("-L")).get.drop("-L".length)
}

lazy val core = crossProject(JVMPlatform, NativePlatform)
  .in(file("core"))
  .dependsOn(macros)
  .settings(
    name := "scalapy-core",
    Compile / sourceGenerators += Def.task {
      val fileToWrite = (Compile / sourceManaged).value / "TupleReaders.scala"
      val methods = (2 to 22).map { n =>
        val tupleElements = (1 to n).map(t => s"r$t.read(orArr(${t - 1}))")
          .mkString(", ")
        s"""implicit def tuple${n}Reader[${(1 to n).map(t => s"T$t").mkString(", ")}](implicit ${(1 to n).map(t => s"r$t: Reader[T$t]").mkString(", ")}): Reader[(${(1 to n).map(t => s"T$t").mkString(", ")})] = {
           |  new Reader[(${(1 to n).map(t => s"T$t").mkString(", ")})] {
           |    override def read(or: PyValue) = {
           |      val orArr = or.getTuple
           |      ($tupleElements)
           |    }
           |  }
           |}"""
      }

      val toWrite =
        s"""package me.shadaj.scalapy.readwrite
           |import me.shadaj.scalapy.interpreter.PyValue
           |trait TupleReaders {
           |${methods.mkString("\n")}
           |}""".stripMargin

      IO.write(fileToWrite, toWrite)
      Seq(fileToWrite)
    },
    Compile / sourceGenerators += Def.task  {
      val fileToWrite = (Compile / sourceManaged).value / "TupleWriters.scala"
      val methods = (2 to 22).map { n =>
        val seqArgs = (1 to n).map(t => s"r$t.write(v._" + t + ")").mkString(", ")
        s"""implicit def tuple${n}Writer[${(1 to n).map(t => s"T$t").mkString(", ")}](implicit ${(1 to n).map(t => s"r$t: Writer[T$t]").mkString(", ")}): Writer[(${(1 to n).map(t => s"T$t").mkString(", ")})] = {
           |  new Writer[(${(1 to n).map(t => s"T$t").mkString(", ")})] {
           |    override def write(v: (${(1 to n).map(t => s"T$t").mkString(", ")})): PyValue = {
           |      CPythonInterpreter.createTuple(Seq(${seqArgs}))
           |    }
           |  }
           |}"""
      }

      val toWrite =
        s"""package me.shadaj.scalapy.readwrite
           |import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue}
           |trait TupleWriters {
           |${methods.mkString("\n")}
           |}""".stripMargin

      IO.write(fileToWrite, toWrite)
      Seq(fileToWrite)
    },
    Compile / sourceGenerators += Def.task {
      val fileToWrite = (Compile / sourceManaged).value / "FunctionReaders.scala"
      val methods = (0 to 22).map { n =>
        val functionArgs = (1 to n).map(t => s"w$t.write(i$t)")
          .mkString(", ")
        s"""implicit def function${n}Reader[${((1 to n).map(t => s"T$t") :+ "O").mkString(", ")}](implicit ${((1 to n).map(t => s"w$t: Writer[T$t]") :+ "oReader: Reader[O]").mkString(", ")}): Reader[(${(1 to n).map(t => s"T$t").mkString(", ")}) => O] = {
           |  new Reader[(${(1 to n).map(t => s"T$t").mkString(", ")}) => O] {
           |    override def read(orig: PyValue) = {
           |      (${(1 to n).map(t => s"i$t: T$t").mkString(", ")}) => {
           |        oReader.read(CPythonInterpreter.call(orig, Seq($functionArgs), Seq()))
           |      }
           |    }
           |  }
           |}"""
      }

      val toWrite =
        s"""package me.shadaj.scalapy.readwrite
           |import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue}
           |trait FunctionReaders {
           |${methods.mkString("\n")}
           |}""".stripMargin

      IO.write(fileToWrite, toWrite)
      Seq(fileToWrite)
    },
    Compile / sourceGenerators += Def.task  {
      val fileToWrite = (Compile / sourceManaged).value / "FunctionWriters.scala"
      val methods = (0 to 22).map { n =>
        val seqArgs = (1 to n).map(t => s"r$t.read(args(${t - 1}))").mkString(", ")
        s"""implicit def function${n}Writer[${((1 to n).map(t => s"T$t") :+ "O").mkString(", ")}](implicit ${((1 to n).map(t => s"r$t: Reader[T$t]") :+ "oWriter: Writer[O]").mkString(", ")}): Writer[(${(1 to n).map(t => s"T$t").mkString(", ")}) => O] = {
           |  new Writer[(${(1 to n).map(t => s"T$t").mkString(", ")}) => O] {
           |    override def write(v: (${(1 to n).map(t => s"T$t").mkString(", ")}) => O): PyValue = {
           |      CPythonInterpreter.createLambda(args => oWriter.write(v($seqArgs)))
           |    }
           |  }
           |}"""
      }

      val toWrite =
        s"""package me.shadaj.scalapy.readwrite
           |import me.shadaj.scalapy.interpreter.{CPythonInterpreter, PyValue}
           |trait FunctionWriters {
           |${methods.mkString("\n")}
           |}""".stripMargin

      IO.write(fileToWrite, toWrite)
      Seq(fileToWrite)
    },
    libraryDependencies += "org.scala-lang.modules" %%% "scala-collection-compat" % "2.12.0",
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq(
          "org.scala-lang" % "scala-reflect" % scalaVersion.value
        )
        case _ => Seq.empty
      }
    },
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.19" % Test,
    Compile / unmanagedSourceDirectories += {
      val sharedSourceDir = (ThisBuild / baseDirectory).value / "core/shared/src/main"
      if (scalaVersion.value.startsWith("2.13.") || scalaVersion.value.startsWith("3")) sharedSourceDir / "scala-2.13"
      else sharedSourceDir / "scala-2.11_2.12"
    },
    Compile / unmanagedSourceDirectories += {
      val sharedSourceDir = (ThisBuild / baseDirectory).value / "core/shared/src/main"
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => sharedSourceDir / "scala-2"
        case _ => sharedSourceDir / "scala-3"
      }
    },
    crossScalaVersions := supportedScalaVersions
  ).jvmSettings(
    libraryDependencies += "net.java.dev.jna" % "jna" % "5.11.0",
    Test / fork := true,
    Test / javaOptions += s"-Djna.library.path=$pythonLibsDir",
    unmanagedSources / excludeFilter := HiddenFileFilter || "*Native*"
  ).nativeSettings(
    nativeConfig ~= {
      _.withLinkingOptions(pythonLdFlags)
        .withLinkStubs(true)
    }
  )

lazy val coreJVM = core.jvm
lazy val coreNative = core.native

lazy val facadeGen = project.in(file("facadeGen"))
  .dependsOn(coreJVM)
  .settings(
    run / fork := true,
    run / javaOptions += s"-Djna.library.path=${"python3-config --prefix".!!.trim}/lib"
  )

lazy val docs = project
  .in(file("built-docs"))
  .settings(
    moduleName := "built-docs",
  )
  .enablePlugins(MdocPlugin, DocusaurusPlugin)
  .dependsOn(coreJVM)
  .settings(
    fork := true,
    connectInput := true,
    javaOptions += s"-Djna.library.path=$pythonLibsDir",
    docusaurusCreateSite := {
      (Compile / mdoc).toTask(" ").value
      Process(List("yarn", "install"), cwd = DocusaurusPlugin.website.value).!
      Process(List("yarn", "run", "build"), cwd = DocusaurusPlugin.website.value).!
      val out = DocusaurusPlugin.website.value / "build"
      out
    }
  )

lazy val bench = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("bench"))
  .settings(
    name := "scalapy-bench",
    version := "0.1.0-SNAPSHOT",
    crossScalaVersions := supportedScalaVersions
  ).jvmSettings(
    javaOptions += s"-Djna.library.path=$pythonLibsDir"
  ).nativeSettings(
    nativeConfig ~= {
      _.withLinkingOptions(pythonLdFlags)
        .withMode(scala.scalanative.build.Mode.releaseFast)
    }
  ).dependsOn(core)

lazy val benchJVM = bench.jvm
lazy val benchNative = bench.native
