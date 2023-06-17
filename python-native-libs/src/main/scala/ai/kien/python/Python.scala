package ai.kien.python

import java.io.{File, FileNotFoundException}
import java.nio.file.{FileSystem, FileSystems, Files}
import scala.util.{Properties, Success, Try}

/** A class for extracting the necessary configuration properties for embedding a specific Python
  * interpreter into an appication
  */
class Python private[python] (
    interpreter: Option[String] = None,
    callProcess: Seq[String] => Try[String] = Defaults.callProcess,
    getEnv: String => Option[String] = Defaults.getEnv,
    fs: FileSystem = FileSystems.getDefault,
    isWindows: Option[Boolean] = None
) {

  /** Provides a list of possible locations for the `libpython` corresponding to this Python
    * interpreter
    */
  lazy val nativeLibraryPaths: Try[Seq[String]] =
    callPython(if (isWin) Python.libPathCmdWin else Python.libPathCmd)
      .map(_.split(";"))
      .map(_.map(_.trim).distinct.filter(_.nonEmpty).toSeq)

  /** Name of the `libpython` corresponding to this Python interpreter, ''e.g.'' `python3.8`,
    * `python3.7m`
    */
  lazy val nativeLibrary: Try[String] = ldversion.map("python" + _)

  /** Absolute path to the Python interpreter executable
    */
  lazy val executable: Try[String] = callPython(Python.executableCmd)

  /** Provides the system properties necessary for setting up [[https://scalapy.dev/ ScalaPy]] with
    * this Python interpreter
    *
    * @example
    *
    * {{{
    * import me.shadaj.scalapy.py
    *
    * Python("/usr/local/bin/python3").scalapyProperties.get.foreach {
    *   case (k, v) => System.setProperty(k, v)
    * }
    * println(py.eval("'Hello from Python!'"))
    * }}}
    */
  def scalapyProperties: Try[Map[String, String]] = for {
    nativeLibPaths <- nativeLibraryPaths
    library        <- nativeLibrary
    executable     <- executable
  } yield {
    val currentPathsStr = Properties.propOrEmpty("jna.library.path")
    val currentPaths    = currentPathsStr.split(pathSeparator)

    val pathsToAdd =
      if (currentPaths.containsSlice(nativeLibPaths)) Nil else nativeLibPaths
    val pathsToAddStr = pathsToAdd.mkString(pathSeparator)

    val newPaths = (currentPathsStr, pathsToAddStr) match {
      case (c, p) if c.isEmpty => p
      case (c, p) if p.isEmpty => c
      case (c, p)              => s"$p$pathSeparator$c"
    }

    Map(
      "jna.library.path"           -> newPaths,
      "scalapy.python.library"     -> library,
      "scalapy.python.programname" -> executable
    )
  }

  /** Provides the recommended linker options for embedding this Python interpreter into another
    * application, mostly extracted from the outputs of
    *
    * `pythonX.Y-config --ldflags` for `python` 3.7 and
    *
    * `pythonX.Y-config --ldflags --embed` for `python` 3.8+
    */
  lazy val ldflags: Try[Seq[String]] = if (isWin) ldflagsWin else ldflagsNix

  lazy val ldflagsWin: Try[Seq[String]] =
    (for {
      nativeLibraryPaths <- nativeLibraryPaths
      nativeLibrary      <- nativeLibrary
    } yield (nativeLibrary, nativeLibraryPaths)).map {
      case (nativeLibrary, nativeLibraryPaths) =>
        "-fuse-ld=lld" +: nativeLibraryPaths
          .headOption
          .map(fs.getPath(_).resolve(nativeLibrary + ".dll"))
          .filter(Files.exists(_))
          .map(_.toString)
          .toSeq
    }

  lazy val ldflagsNix: Try[Seq[String]] = for {
    rawLdflags         <- rawLdflags
    nativeLibraryPaths <- nativeLibraryPaths
    libPathFlags = nativeLibraryPaths.map("-L" + _)
    flags = rawLdflags
      .split("\\s+(?=-)")
      .filter(f => f.nonEmpty && !libPathFlags.contains(f))
      .flatMap(f => if (f.startsWith("-L")) Array(f) else f.split("\\s+"))
      .toSeq
  } yield libPathFlags ++ flags

  private val path: String = getEnv("PATH").getOrElse("")

  private lazy val isWin = isWindows.getOrElse(Properties.isWin)

  private val pathSeparator =
    isWindows.map(if (_) ";" else ":").getOrElse(File.pathSeparator)

  private def existsInPath(exec: String): Boolean = {
    val pathExts = getEnv("PATHEXT").getOrElse("").split(pathSeparator)
    val l = for {
      elem <- path.split(pathSeparator).iterator
      elemPath = fs.getPath(elem)
      ext <- pathExts.iterator
    } yield Files.exists(elemPath.resolve(exec + ext))

    l.contains(true)
  }

  private lazy val python: Try[String] = Try(
    if (existsInPath("python3"))
      "python3"
    else if (existsInPath("python"))
      "python"
    else
      throw new FileNotFoundException(
        "Neither python3 nor python was found in $PATH."
      )
  )

  private lazy val interp: Try[String] =
    interpreter.map(Success(_)).getOrElse(python)

  private def callPython(cmd: String): Try[String] =
    interp.flatMap(python => callProcess(Seq(python, "-c", cmd)))

  private def ldversion: Try[String] = callPython(
    if (isWin) Python.ldversionCmdWin else Python.ldversionCmd
  )

  private lazy val binDir =
    callPython("import sys;print(sys.base_prefix)")
      .map(base => s"${base}${fs.getSeparator}bin")

  private lazy val pythonConfigExecutable = for {
    binDir    <- binDir
    ldversion <- ldversion
    pythonConfigExecutable = s"${binDir}${fs.getSeparator}python${ldversion}-config"
    _ <- Try {
      if (!Files.exists(fs.getPath(pythonConfigExecutable)))
        throw new FileNotFoundException(s"$pythonConfigExecutable does not exist")
      else ()
    }
  } yield pythonConfigExecutable

  private lazy val pythonConfig = pythonConfigExecutable.map(new PythonConfig(_, callProcess))

  private lazy val rawLdflags = pythonConfig.flatMap(_.ldflags)
}

object Python {

  /** @param interpreter
    *   optional path to a Python interpreter executable, which defaults to `Some("python3")` if not
    *   provided
    *
    * @example
    *
    * {{{
    * val python = Python()
    * python.scalapyProperties.get.foreach {
    *   case (k, v) => System.setProperty(k, v)
    * }
    *
    * import me.shadaj.scalapy.py
    * println(py.eval("'Hello from Python!'"))
    * }}}
    *
    * @return
    *   an instance of [[ai.kien.python.Python]] which provides the necessary configuration
    *   properties for embedding a specific Python interpreter
    */
  def apply(interpreter: Option[String] = None): Python = new Python(interpreter)

  /** @param interpreter
    *   path to a Python interpreter executable
    *
    * @example
    *
    * {{{
    * val python = Python("/usr/local/bin/python3")
    * python.scalapyProperties.get.foreach {
    *   case (k, v) => System.setProperty(k, v)
    * }
    *
    * import me.shadaj.scalapy.py
    * println(py.eval("'Hello from Python!'"))
    * }}}
    *
    * @return
    *   an instance of [[ai.kien.python.Python]] which provides the necessary configuration
    *   properties for embedding a specific Python interpreter
    */
  def apply(interpreter: String): Python = apply(Some(interpreter))

  private def executableCmd = "import sys;print(sys.executable)"

  private def ldversionCmd =
    """import sys
      |import sysconfig
      |try:
      |    abiflags = sys.abiflags
      |except AttributeError:
      |    abiflags = sysconfig.get_config_var('abiflags') or ''
      |print(sysconfig.get_python_version() + abiflags)
    """.stripMargin

  private def ldversionCmdWin =
    """import sys
      |import sysconfig
      |try:
      |    abiflags = sys.abiflags
      |except AttributeError:
      |    abiflags = sysconfig.get_config_var('abiflags') or ''
      |print(''.join(map(str, sys.version_info[:2])) + abiflags)
    """.stripMargin

  private def libPathCmd =
    """import sys
      |import os.path
      |from sysconfig import get_config_var
      |libpl = get_config_var('LIBPL')
      |libpl = libpl + ';' if libpl is not None else ''
      |print(libpl + os.path.join(sys.base_prefix, 'lib'))
    """.stripMargin

  private def libPathCmdWin =
    """import sys
      |print(sys.base_prefix)
    """.stripMargin
}
