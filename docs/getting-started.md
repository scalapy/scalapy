---
slug: /
id: getting-started
title: Getting Started with ScalaPy
sidebar_label: Getting Started
---

ScalaPy makes it easy to use Python libraries from Scala code. With a simple API, automatic conversion between Scala and Python types, and optional static typing, ScalaPy scales from hobby projects to production systems.

## Installation
First, add ScalaPy to your SBT build:
```scala
// JVM
libraryDependencies += "dev.scalapy" %% "scalapy-core" % "0.5.3"

// Scala Native
libraryDependencies += "dev.scalapy" %%% "scalapy-core" % "0.5.3"
```

You'll then need to add the Python native libraries to your project and configure SBT to run your code in a separate JVM instance, either manually,

```scala
fork := true

import scala.sys.process._
lazy val pythonLdFlags = {
  val withoutEmbed = "python3-config --ldflags".!!
  if (withoutEmbed.contains("-lpython")) {
    withoutEmbed.split(' ').map(_.trim).filter(_.nonEmpty).toSeq
  } else {
    val withEmbed = "python3-config --ldflags --embed".!!
    withEmbed.split(' ').map(_.trim).filter(_.nonEmpty).toSeq
  }
}

lazy val pythonLibsDir = {
  pythonLdFlags.find(_.startsWith("-L")).get.drop("-L".length)
}

javaOptions += s"-Djna.library.path=$pythonLibsDir"
```

or using the [`python-native-libs`](https://github.com/kiendang/python-native-libs) helper library,

First, add `python-native-libs` to `project/plugins.sbt`

```scala
libraryDependencies += "ai.kien" %% "python-native-libs" % "0.2.2"
```

Then, in `build.sbt`,

```scala
fork := true

import ai.kien.python.Python

lazy val python = Python("<optional-path-to-a-python-interpreter-executable>")

lazy val javaOpts = python.scalapyProperties.get.map {
  case (k, v) => s"""-D$k=$v"""
}.toSeq

javaOptions ++= javaOpts
```

If you'd like to use [Scala Native](https://scala-native.readthedocs.io/), follow the instructions there to create a project with Scala Native `0.4.0-M2`. Then, add the following additional configuration to your SBT build to link the Python interpreter.

Manually,

```scala
lazy val pythonLdFlags = ... // same as above

nativeConfig ~= {
  _.withLinkingOptions(pythonLdFlags)
}
```

Using `python-native-libs`,

```scala
import ai.kien.python.Python

lazy val python = Python("<optional-path-to-a-python-interpreter-executable>")

lazy val pythonLdFlags = python.ldflags.get

nativeConfig ~= {
  _.withLinkingOptions(pythonLdFlags)
}
```

## Hello World!
Now that ScalaPy is installed, let's start with a simple example. ScalaPy offers a dynamically typed API that's great for making quick Python calls with little ceremony. First, we can use the Python `len` function to calculate the length of a list. Using `py.Dynamic.global`, you can access any members of the global scope.

```scala mdoc
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.SeqConverters

val listLengthPython = py.Dynamic.global.len(List(1, 2, 3).toPythonProxy)
```

Here, we took a Scala `List`, converted it to a Python list, sent it to the `len` function, and got back a Python number.

To convert Python values back into Scala, we use the `.as` method and pass in the type we want.

```scala mdoc
val listLength = listLengthPython.as[Int]
```
## Execution
ScalaPy officially supports Python 3.{7, 8, 9, 10, 11, 12}. If you want to use another version of Python, you should either define the environment variable `SCALAPY_PYTHON_LIBRARY`

```shell
python --version
# Python 3.8.6
export SCALAPY_PYTHON_LIBRARY=python3.8
sbt run
```

or set the system property `scalapy.python.library`

```shell
sbt -Dscalapy.python.library=python3.8 run
```

The environment variable takes precedence over the system property.

## Virtualenv

To use ScalaPy with a Python installation inside a virtualenv, set the path to the Python interpreter executable using either the `scalapy.python.programname` system property

```shell
sbt -Dscalapy.python.programname=/Users/example/example-env/bin/python run
```

or the `SCALAPY_PYTHON_PROGRAMNAME` environment variable

```shell
export SCALAPY_PYTHON_PROGRAMNAME=/Users/example/example-env/bin/python
sbt run
```

The environment variable takes precedence over the system property.

This variable is used as the input to the Python/C API function `Py_SetProgramName`. `Py_SetProgramName` is run prior to `Py_Initialize` to set the correct paths to Python run-time libraries (`prefix`, `exec_prefix`, ...).
