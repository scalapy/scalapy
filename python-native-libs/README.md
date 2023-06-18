# python-native-libs

Helpers for setting up an embedded Python interpreter

![Build Status](https://github.com/kiendang/python-native-libs/actions/workflows/ci.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/ai.kien/python-native-libs_2.13.svg)](https://maven-badges.herokuapp.com/maven-central/ai.kien/python-native-libs_2.13)

## Overview

The canonical use case is to help set up [`ScalaPy`](https://scalapy.dev/) to point to a specific Python installation by attempting to infer the correct configuration properties used by `ScalaPy` during the initialization of the embedded Python interpreter. This could potentially see usage outside of `ScalaPy` too since these properties are relevant to embedded Python in general.

## Usage

By default `Python` checks for the `python3` executable (or `python` if `python3` is not found) on `PATH`

```scala
import ai.kien.python.Python

val python = Python()
// python: Python = ai.kien.python.Python@5eb35f5d

python.nativeLibrary
// res0: util.Try[String] = Success(value = "python3.9")

python.nativeLibraryPaths
// res1: util.Try[Seq[String]] = Success(
//   value = ArraySeq(
//     "/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib/python3.9/config-3.9-darwin",
//     "/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib"
//   )
// )

python.scalapyProperties
// res2: util.Try[Map[String, String]] = Success(
//   value = Map(
//     "jna.library.path" -> "/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib/python3.9/config-3.9-darwin:/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib",
//     "scalapy.python.library" -> "python3.9",
//     "scalapy.python.programname" -> "/usr/local/opt/python@3.9/bin/python3.9"
//   )
// )

python.ldflags
// res3: util.Try[Seq[String]] = Success(
//   value = ArraySeq(
//     "-L/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib/python3.9/config-3.9-darwin",
//     "-L/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib",
//     "-lpython3.9",
//     "-ldl",
//     "-framework",
//     "CoreFoundation"
//   )
// )
```

You can point it towards a specific Python installation by passing the path to the interpreter executable to `Python`

```scala
val python = Python("/usr/bin/python3")
// python: Python = ai.kien.python.Python@eb0b5d0

python.nativeLibrary
// res4: util.Try[String] = Success(value = "python3.9")

python.nativeLibraryPaths
// res5: util.Try[Seq[String]] = Success(
//   value = ArraySeq(
//     "/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib/python3.9/config-3.9-darwin",
//     "/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib"
//   )
// )

python.scalapyProperties
// res6: util.Try[Map[String, String]] = Success(
//   value = Map(
//     "jna.library.path" -> "/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib/python3.9/config-3.9-darwin:/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib",
//     "scalapy.python.library" -> "python3.9",
//     "scalapy.python.programname" -> "/usr/local/opt/python@3.9/bin/python3.9"
//   )
// )

python.ldflags
// res7: util.Try[Seq[String]] = Success(
//   value = ArraySeq(
//     "-L/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib/python3.9/config-3.9-darwin",
//     "-L/usr/local/opt/python@3.9/Frameworks/Python.framework/Versions/3.9/lib",
//     "-lpython3.9",
//     "-ldl",
//     "-framework",
//     "CoreFoundation"
//   )
// )
```

See `docs/details.md` to see the full list of these properties and what they mean.

`scalapyProperties` contains the system properties used by `ScalaPy`. For example, to set up `ScalaPy` to use the Python located at `/usr/bin/python3` in [`Ammonite`](https://ammonite.io/) or [`Almond`](https://almond.sh/) run

```scala
import $ivy.`ai.kien::python-native-libs:<version>`
import $ivy.`me.shadaj::scalapy-core:<scalapy_version>`

import ai.kien.python.Python

Python("/usr/bin/python3").scalapyProperties.fold(
  ex => println(s"Error while getting ScalaPy properties: $ex"),
  props => props.foreach { case(k, v) => System.setProperty(k, v) }
)

import me.shadaj.scalapy.py

println(py.module("sys").version)
```
