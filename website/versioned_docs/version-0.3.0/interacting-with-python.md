---
id: version-0.3.0-interacting-with-python
title: Interacting with Python
sidebar_label: Interacting with Python
original_id: interacting-with-python
---

ScalaPy offers a variety of ways to interact with the Python interpreter, enabling you to calculate any Python expression from Scala code.

## Global Scope

The primary entrypoint into the Python interpreter from ScalaPy is `py.global`, which acts similarly to Scala.js's `js.Dynamic.global` to provide a dynamically-typed interface for the interpreter's global scope. With `py.global`, you can call any global method and access any global value.

For example, we can create a Python range with the `range()` method, and calculate the sum of its elements with `sum()`.

```scala
import me.shadaj.scalapy.py

// Python ranges are exclusive
val list = py.global.range(1, 3 + 1)
// list: py.Any = range(1, 4)

// 1 + 2 + 3 == 6
val listSum = py.global.sum(list)
// listSum: py.Any = 6
```

## Importing Modules

If you're working with a Python library, you'll likely need to import some modules. You can do this in ScalaPy with the `py.module` method. This method returns an object representing the imported module, which can be used just like `py.global` but with the contents referencing the module instead of the global scope.

For example we can import NumPy, a popular package for scientific computing with Python.

```scala
val np = py.module("numpy")
// np: py.Module = <module 'numpy' from '/usr/lib/python3/dist-packages/numpy/__init__.py'>

val a = np.array(Seq(
  Seq(1, 0),
  Seq(0, 1)
))
// a: py.Dynamic = [[1 0]
//  [0 1]]

val b = np.array(Seq(
  Seq(4, 1),
  Seq(2, 2)
))
// b: py.Dynamic = [[4 1]
//  [2 2]]

val aTimesB = np.matmul(a, b)
// aTimesB: py.Dynamic = [[4 1]
//  [2 2]]
```

In this example, you'll notice that we passed in a Scala `Seq` into `np.array`, which usually takes a Python list. When using Python APIs, ScalaPy will automatically convert Scala values into their Python equivalents, in this case converting the sequences into Python lists.


## Custom Python Snippets

Sometimes, you might run into a situation where you need to express a Python construct that can't be done through an existing ScalaPy API. For this situation and to make converting Python code easier, ScalaPy provides an escape hatch via the `py""` string interpolator. This lets you run arbitrary strings as Python code with the additional power of being able to interpolate in Scala values.

For example, we might want to use Python `map` which takes a `lambda`, something that can't be directly expressed in Scala code yet. Instead, we can use the `py""` interpolator to write the expression as a piece of Python code.

```scala
import py.PyQuote

val mappedList = py.global.list(
  py"map(lambda elem: elem + 1, ${Seq(1, 2, 3)})"
)
// mappedList: py.Any = [2, 3, 4]
```

If you need to run arbitrary strings of Python that are dynamically generated, you can use `py.eval`:

```scala
val myPythonList = py.eval("1 + 2")
// myPythonList: py.Dynamic = 3
```

