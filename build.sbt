organization := "me.shadaj"

name := "scalapy"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.1"

sourceGenerators in Compile <+= baseDirectory map { dir =>
  val fileToWrite = dir / "src" / "gen" / "scala" / "me/shadaj/scalapy/py" / "ObjectTupleReaders.scala"
  val methods = (2 to 22).map { n =>
    s"""implicit def tuple${n}Reader[${(1 to n).map(t => s"T$t").mkString(", ")}](implicit ${(1 to n).map(t => s"r$t: ObjectReader[T$t]").mkString(", ")}): ObjectReader[(${(1 to n).map(t => s"T$t").mkString(", ")})] = {
       |  new ObjectReader[(${(1 to n).map(t => s"T$t").mkString(", ")})] {
       |    override def read(r: Ref)(implicit jep: Jep) = {
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

fork in Test := true
javaOptions in Test += "-Djava.library.path=./lib/"
