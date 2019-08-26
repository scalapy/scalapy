---
id: getting-started
title: Getting Started with ScalaPy
sidebar_label: Getting Started
---

ScalaPy makes it easy to use Python libraries from Scala code. With a simple API, automatic conversion between Scala and Python types, and optional static typing, ScalaPy scales from hobby projects to production systems.

## Installation
First, add ScalaPy to your SBT build:
```scala
libraryDependencies += "me.shadaj" %% "scalapy-core" % "0.3.0"
```

You'll then need to add the Python native libraries to your project and configure SBT to run your code in a separate JVM instance.

```scala
fork := true

import scala.sys.process._
javaOptions in Test += s"-Djava.library.path=${"python3-config --ldflags".!! + "/lib"}"
```

## Hello World!
Now that ScalaPy is installed, let's start with a simple example. ScalaPy offers a dynamically typed API that's great for making quick Python calls with little ceremony. First, we can use the Python `len` function to calculate the length of a list. Using `py.global`, you can access any members of the global scope.

```scala mdoc
import me.shadaj.scalapy.py

val listLengthPython = py.global.len(List(1, 2, 3))
```

Here, we took a Scala `List`, converted it to a Python list, sent it to the `len` function, and got back a Python number.

To convert Python values back into Scala, we use the `.as` method and pass in the type we want.

```scala mdoc
val listLength = listLengthPython.as[Int]
```
