package ai.kien.python

import org.scalatest.{BeforeAndAfterAllConfigMap, ConfigMap, Suite}

trait PythonSuite extends Suite with BeforeAndAfterAllConfigMap {
  override def beforeAll(configMap: ConfigMap) = {
    Python(Config.pythonExecutable).scalapyProperties.get.foreach { case (k, v) => System.setProperty(k, v) }
  }
}
