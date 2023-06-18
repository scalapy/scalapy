package ai.kien.python

import org.scalatest.{BeforeAndAfterAll, Suite}

trait PythonSuite extends Suite with BeforeAndAfterAll {
  override def beforeAll() = {
    Python(Config.pythonExecutable).scalapyProperties.get.foreach { case (k, v) => System.setProperty(k, v) }
  }
}
