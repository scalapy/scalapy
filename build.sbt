import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

organization in ThisBuild := "me.shadaj"

scalaVersion in ThisBuild := "2.12.7"

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
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  ).nativeSettings(
    scalaVersion := "2.11.12"
  )

lazy val macrosJVM = macros.jvm
lazy val macrosNative = macros.native

lazy val core = crossProject(JVMPlatform, NativePlatform)
  .in(file("core"))
  .dependsOn(macros)
  .settings(
    name := "scalapy-core",
    sourceGenerators in Compile += Def.task {
      val fileToWrite = (sourceManaged in Compile).value / "TupleReaders.scala"
      val methods = (2 to 22).map { n =>
        val tupleElements = (1 to n).map(t =>
          s"""r$t.read(new ValueAndRequestRef(orArr(${t - 1})) {
             |  def getRef = or.requestRef.as[Dynamic].arrayAccess(${t - 1})
             |})""".stripMargin).mkString(", ")
        s"""implicit def tuple${n}Reader[${(1 to n).map(t => s"T$t").mkString(", ")}](implicit ${(1 to n).map(t => s"r$t: Reader[T$t]").mkString(", ")}): Reader[(${(1 to n).map(t => s"T$t").mkString(", ")})] = {
           |  new Reader[(${(1 to n).map(t => s"T$t").mkString(", ")})] {
           |    override def read(or: ValueAndRequestRef) = {
           |      val orArr = or.value.getTuple
           |      ($tupleElements)
           |    }
           |  }
           |}"""
      }
    
      val toWrite =
        s"""package me.shadaj.scalapy.py
           |trait TupleReaders {
           |${methods.mkString("\n")}
           |}""".stripMargin
    
      IO.write(fileToWrite, toWrite)
      Seq(fileToWrite)
    },
    sourceGenerators in Compile += Def.task  {
      val fileToWrite = (sourceManaged in Compile).value / "TupleWriters.scala"
      val methods = (2 to 22).map { n =>
        val seqArgs = (1 to n).map(t => s"r$t.write(v._" + t + ").right.map(_.value).merge").mkString(", ")
        s"""implicit def tuple${n}Writer[${(1 to n).map(t => s"T$t").mkString(", ")}](implicit ${(1 to n).map(t => s"r$t: Writer[T$t]").mkString(", ")}): Writer[(${(1 to n).map(t => s"T$t").mkString(", ")})] = {
           |  new Writer[(${(1 to n).map(t => s"T$t").mkString(", ")})] {
           |    override def write(v: (${(1 to n).map(t => s"T$t").mkString(", ")})): Either[PyValue, Any] = {
           |      Left(interpreter.createTuple(Seq(${seqArgs})))
           |    }
           |  }
           |}"""
      }
    
      val toWrite =
        s"""package me.shadaj.scalapy.py
           |trait TupleWriters {
           |${methods.mkString("\n")}
           |}""".stripMargin
    
      IO.write(fileToWrite, toWrite)
      Seq(fileToWrite)
    }
  ).settings(
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.0-SNAP8" % Test,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  ).jvmSettings(
    libraryDependencies += "black.ninia" % "jep" % "3.8.2",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
    fork in Test := true,
    javaOptions in Test += s"-Djava.library.path=${sys.env.getOrElse("JEP_PATH", "/usr/local/lib/python3.7/site-packages/jep")}"
  ).nativeSettings(
    scalaVersion := "2.11.12",
    libraryDependencies += "com.github.lolgab" %%% "scalacheck" % "1.14.1" % Test,
    nativeLinkStubs := true,
    nativeLinkingOptions ++= {
      import scala.sys.process._
      "python3-config --ldflags".!!.split(' ').map(_.trim).filter(_.nonEmpty).toSeq
    }
  )

lazy val coreJVM = core.jvm
lazy val coreNative = core.native

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
    javaOptions += s"-Djava.library.path=${sys.env.getOrElse("JEP_PATH", "/usr/local/lib/python3.7/site-packages/jep")}"
  )
