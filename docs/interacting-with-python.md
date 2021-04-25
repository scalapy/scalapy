---
id: interacting-with-python
title: Interacting with Python
sidebar_label: Interacting with Python
---

ScalaPy offers a variety of ways to interact with the Python interpreter, enabling you to calculate any Python expression from Scala code.

## Global Scope
The primary entrypoint into the Python interpreter from ScalaPy is `py.global`, which acts similarly to Scala.js's `js.Dynamic.global` to provide a dynamically-typed interface for the interpreter's global scope. With `py.global`, you can call any global method and access any global value.

For example, we can create a Python range with the `range()` method, and calculate the sum of its elements with `sum()`.

```scala mdoc
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.SeqConverters

// Python ranges are exclusive
val list = py.Dynamic.global.range(1, 3 + 1)

// 1 + 2 + 3 == 6
val listSum = py.Dynamic.global.sum(list)
```

## Importing Modules
If you're working with a Python library, you'll likely need to import some modules. You can do this in ScalaPy with the `py.module` method. This method returns an object representing the imported module, which can be used just like `py.global` but with the contents referencing the module instead of the global scope.

For example we can import NumPy, a popular package for scientific computing with Python.

```scala mdoc
val np = py.module("numpy")

val a = np.array(Seq(
  Seq(1, 0).toPythonProxy,
  Seq(0, 12).toPythonProxy
).toPythonProxy)

val aSquared = np.matmul(a, a)
```

In this example, you'll notice that we passed in a Scala `Seq` into `np.array`, which usually takes a Python list. When using Python APIs, ScalaPy will automatically convert Scala values into their Python equivalents, in this case converting the sequences into Python lists.


## Custom Python Snippets
Sometimes, you might run into a situation where you need to express a Python construct that can't be done through an existing ScalaPy API. For this situation and to make converting Python code easier, ScalaPy provides an escape hatch via the `py""` string interpolator. This lets you run arbitrary strings as Python code with the additional power of being able to interpolate in Scala values.

For example, we might want to use Python `map` which takes a `lambda`. Instead, we can use the `py""` interpolator to write the expression as a piece of Python code.

```scala mdoc
import py.PyQuote

val mappedList = py.Dynamic.global.list(
  py"map(lambda elem: elem + 1, ${Seq(1, 2, 3).toPythonProxy})"
)
```

If you need to run arbitrary strings of Python that are dynamically generated, you can use `py.eval`:
```scala mdoc
val myPythonList = py.eval("1 + 2")
```

## Special Python Syntax
ScalaPy includes APIs to make it possible to use Python features that require special syntax from Scala.

### `py.with`
Python includes a "try-with-resources" feature in the form of the `with` keyword. In ScalaPy, you can use this feature by calling `py.with` with the value you want to open and a curried function using that value. For example, we can open a file with the following code:

```scala mdoc
val myFile = py.Dynamic.global.open("../README.md")
py.`with`(myFile) { file =>
  println(file.encoding.as[String])
}
```

### `bracketAccess` and `bracketUpdate`
To index into a sequence-like Python value, `py.Dynamic` offers the `bracketAccess` and `bracketUpdate` APIs to load a value through an indexing operation and set a value through one. For example, we could update values of a Python list:

```scala mdoc
val pythonList = py.Dynamic.global.list(Seq(1, 2, 3).toPythonProxy)
pythonList.bracketAccess(0)
pythonList.bracketUpdate(1, 100)
pythonList
```
