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
libraryDependencies += "me.shadaj" %% "scalapy-core" % "0.4.1"

// Scala Native
libraryDependencies += "me.shadaj" %%% "scalapy-core" % "0.4.1"
```

You'll then need to add the Python native libraries to your project and configure SBT to run your code in a separate JVM instance.

```scala
fork := true

import scala.sys.process._
javaOptions += s"-Djava.library.path=${"python3-config --configdir".!!.trim}/lib"
```

If you'd like to use [Scala Native](https://scala-native.readthedocs.io/), follow the instructions there to create a project with Scala Native `0.4.0-M2`. Then, add the following additional configuration to your SBT build to link the Python interpreter.

```scala
import scala.sys.process._
nativeLinkingOptions ++= "python3-config --ldflags".!!.split(' ').map(_.trim).filter(_.nonEmpty).toSeq
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
ScalaPy uses by default python3, python3.7, python3.7m. If you use an other version of python, you should define the variable `SCALAPY_PYTHON_LIBRARY`
```shell
python --version
# Python 3.8.6
export SCALAPY_PYTHON_LIBRARY=python3.8
sbt run
```
