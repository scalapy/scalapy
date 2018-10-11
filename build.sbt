organization := "me.shadaj"

name := "scalapy"

scalaVersion := "2.12.7"

sourceGenerators in Compile += Def.task {
  val fileToWrite = (sourceManaged in Compile).value / "ObjectTupleReaders.scala"
  val methods = (2 to 22).map { n =>
    val tupleElements = (1 to n).map(t =>
      s"""r$t.read(new ValueAndRequestObject(orArr(${t - 1})) {
         |  def getObject = or.requestObject.asInstanceOf[DynamicObject].arrayAccess(${t - 1})
         |})""".stripMargin).mkString(", ")
    s"""implicit def tuple${n}Reader[${(1 to n).map(t => s"T$t").mkString(", ")}](implicit ${(1 to n).map(t => s"r$t: ObjectReader[T$t]").mkString(", ")}): ObjectReader[(${(1 to n).map(t => s"T$t").mkString(", ")})] = {
       |  new ObjectReader[(${(1 to n).map(t => s"T$t").mkString(", ")})] {
       |    override def read(or: ValueAndRequestObject)(implicit jep: Jep) = {
       |      val orArr = or.value.asInstanceOf[java.util.List[Any]].toArray
       |      ($tupleElements)
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

sourceGenerators in Compile += Def.task  {
  val fileToWrite = (sourceManaged in Compile).value / "ObjectTupleWriters.scala"
  val methods = (2 to 22).map { n =>
    s"""implicit def tuple${n}Writer[${(1 to n).map(t => s"T$t").mkString(", ")}](implicit ${(1 to n).map(t => s"r$t: ObjectWriter[T$t]").mkString(", ")}): ObjectWriter[(${(1 to n).map(t => s"T$t").mkString(", ")})] = {
       |  new ObjectWriter[(${(1 to n).map(t => s"T$t").mkString(", ")})] {
       |    override def write(v: (${(1 to n).map(t => s"T$t").mkString(", ")}))(implicit jep: Jep): Either[Any, Object] = {
       |      Right(Object("(" + ${(1 to n).map(t => s"r$t.write(v._" + t + ").left.map(Object.populateWith).merge.expr").mkString("+ \",\" +")} + ")"))
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
libraryDependencies += "black.ninia" % "jep" % "3.8.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % Test

fork in Test := true
javaOptions in Test += s"-Djava.library.path=${sys.env.getOrElse("JEP_PATH", "/usr/local/lib/python3.7/site-packages/jep")}"
