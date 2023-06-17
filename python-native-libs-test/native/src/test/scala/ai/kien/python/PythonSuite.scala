package ai.kien.python

import org.scalatest.{BeforeAndAfterAllConfigMap, ConfigMap, Suite}

trait PythonSuite extends Suite with BeforeAndAfterAllConfigMap {
  override def beforeAll(configMap: ConfigMap) = {
    System.setProperty("scalapy.python.programname", Config.pythonExecutable)
  }
}
