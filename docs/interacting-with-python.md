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
  Seq(1, 0),
  Seq(0, 12)
).toPythonProxy)

val aSquared = np.matmul(a, a)
```

## Scala-Python Conversions
In the previous example, you'll notice that we passed in a Scala `Seq[Seq[Int]]` into `np.array`, which usually takes a Python list. When using Python APIs, ScalaPy will automatically convert scalar Scala values into their Python equivalents (through the `Writer` type). This handles the integer values, but not sequences, which have multiple options for conversion in ScalaPy.

If you'd like to create a copy of the sequence, which can be accessed by Python code with high performance but miss any mutations you later make in Scala code, you can use `toPythonCopy`:

```scala mdoc
val mySeqToCopy = Seq(Seq(1, 2), Seq(3))
mySeqToCopy.toPythonCopy
```

If you'd like to create a proxy of the sequence instead, which uses less memory and can observe mutations but comes with a larger overhead for repeated access from Python, you can use `toPythonCopy`:

```scala mdoc
val mySeqToProxy = Array(1, 2, 3)
val myProxy = mySeqToProxy.toPythonProxy
println(py.Dynamic.global.list(myProxy))
mySeqToProxy(2) = 100
println(py.Dynamic.global.list(myProxy))
```

To convert Python values back into their Scala equivalents, ScalaPy comes with the `.as` API to automatically perform conversions for supported types (those that have a `Reader` implementation). Unlike writing, where there were multiple options for converting sequence types, there is a single `.as[]` API for converting back. If you load a collection into an immutable Scala sequence type, it will be loaded as a copy. If you load it as a `mutable.Seq`, however, it will be loaded as a proxy and can observe underlying changes

```scala mdoc
import scala.collection.mutable
val myPythonList = py.Dynamic.global.list(py.Dynamic.global.range(10))
val copyLoad = myPythonList.as[Vector[Int]]
val proxyLoad = myPythonList.as[mutable.Seq[Int]]

println(copyLoad)
println(proxyLoad)

myPythonList.bracketUpdate(0, 100)

println(copyLoad)
println(proxyLoad)

proxyLoad(0) = 200

println(myPythonList)
```

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
py.eval("1 + 2")
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

### `bracketAccess`, `bracketUpdate`, and `bracketDelete`
To index into a sequence-like Python value, `py.Dynamic` offers the `bracketAccess`, `bracketUpdate`, and `bracketDelete` APIs to load, set, or delete a value through an indexing operation. For example, we could update values of a Python list:

```scala mdoc
val pythonList = py.Dynamic.global.list(Seq(1, 2, 3).toPythonProxy)
println(pythonList)
pythonList.bracketAccess(0)
pythonList.bracketUpdate(1, 100)
println(pythonList)
```

We can also delete elements of a Python dictionary:
```scala mdoc
val myDict = py.Dynamic.global.dict()
myDict.bracketUpdate("hello", "world")
println(myDict)
myDict.bracketDelete("hello")
println(myDict)
```

### `attrDelete`
On supported objects, you can also delete an attribute with the `attrDelete` APIs:
```scala mdoc
import me.shadaj.scalapy.interpreter.CPythonInterpreter
CPythonInterpreter.execManyLines(
  """class MyClass:
    |  myAttribute = 0""".stripMargin
)
py.Dynamic.global.MyClass.attrDelete("myAttribute")
```

### `.del()`
Some Python APIs require you to explicitly delete a reference to a value with the `del` keyword. In ScalaPy, you can perform the equivalent operation by calling `del` on a Python value.

```scala mdoc:silent
val myValue = py.Dynamic.global.MyClass()
myValue.del()
```
```scala mdoc:crash
println(myValue)
```

There are two key points to note when using this API. First, although the Python value is still available in Scala, any attempts to access it will result in an exception since the value has been released. Second, if there are multiple references to a single Python value from your Scala code, `del` will only delete a single reference and the underlying value will not be freed since other Scala code still holds a reference to it.
