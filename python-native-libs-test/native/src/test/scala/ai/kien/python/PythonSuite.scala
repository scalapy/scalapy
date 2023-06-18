package ai.kien.python

import org.scalatest.{BeforeAndAfterAll, Suite}

trait PythonSuite extends Suite with BeforeAndAfterAll {
  override def beforeAll() = {
    System.setProperty("scalapy.python.programname", Config.pythonExecutable)
  }
}
