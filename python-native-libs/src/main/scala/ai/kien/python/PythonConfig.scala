package ai.kien.python

import scala.util.Try

private[python] class PythonConfig(
    pythonConfig: String,
    callProcess: Seq[String] => Try[String] = Defaults.callProcess
) {
  def callPythonConfig(cmd: String*): Try[String] = callProcess(pythonConfig +: cmd)

  lazy val ldflags: Try[String] =
    callPythonConfig("--ldflags", "--embed")
      .recoverWith { case _ => callPythonConfig("--ldflags") }
}
