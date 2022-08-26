package ai.kien.python

import scala.sys.process.Process
import scala.util.Try

private[python] object Defaults {
  def callProcess(cmd: Seq[String]) = Try(Process(cmd).!!.trim)

  def getEnv(k: String) = Option(System.getenv(k))
}
