organization := "me.shadaj"

name := "scalapy"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.1"

sourceGenerators in Compile <+= baseDirectory map { dir =>
  val fileToWrite = dir / "src" / "gen" / "scala" / "me/shadaj/scalapy/py" / "ObjectTupleReaders.scala"
  val methods = (2 to 22).map { n =>
    s"""implicit def tuple${n}Reader[${(1 to n).map(t => s"T$t").mkString(", ")}](implicit ${(1 to n).map(t => s"r$t: ObjectReader[T$t]").mkString(", ")}): ObjectReader[(${(1 to n).map(t => s"T$t").mkString(", ")})] = {
       |  new ObjectReader[(${(1 to n).map(t => s"T$t").mkString(", ")})] {
       |    override def read(or: Ref)(implicit jep: Jep) = {
       |      val r = Ref(or.expr).asInstanceOf[DynamicRef]
       |      (${(1 to n).map(t => s"r.arrayAccess(${t - 1}).as[T$t]").mkString(", ")})
       |    }
       |  }
       |}"""
  }

  val toWrite =
    s"""package me.shadaj.scalapy.py
       |import jep.Jep
       |trait ObjectTupleReaders {
       |${methods.mkString("\n")}
       |}""".stripMargin

  IO.write(fileToWrite, toWrite)
  Seq(fileToWrite)
}

sourceGenerators in Compile <+= baseDirectory map { dir =>
  val fileToWrite = dir / "src" / "gen" / "scala" / "me/shadaj/scalapy/py" / "ObjectTupleWriters.scala"
  val methods = (2 to 22).map { n =>
    s"""implicit def tuple${n}Writer[${(1 to n).map(t => s"T$t").mkString(", ")}](implicit ${(1 to n).map(t => s"r$t: ObjectWriter[T$t]").mkString(", ")}): ObjectWriter[(${(1 to n).map(t => s"T$t").mkString(", ")})] = {
       |  new ObjectWriter[(${(1 to n).map(t => s"T$t").mkString(", ")})] {
       |    override def write(v: (${(1 to n).map(t => s"T$t").mkString(", ")}))(implicit jep: Jep): Ref = {
       |      val array = Object("[]")
       |      ${(1 to n).map(t => "jep.eval(s\"${array.expr}.append(${Ref.from(v._" + t + ").expr})\")").mkString(";")}
       |      array.asRef
       |    }
       |  }
       |}"""
  }

  val toWrite =
    s"""package me.shadaj.scalapy.py
       |import jep.Jep
       |trait ObjectTupleWriters {
       |${methods.mkString("\n")}
       |}""".stripMargin

  IO.write(fileToWrite, toWrite)
  Seq(fileToWrite)
}

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

fork in Test := true

unmanagedBase := file("/usr/local/lib/python3.6/site-packages/jep")
javaOptions in Test += "-Djava.library.path=/usr/local/lib/python3.6/site-packages/jep"
