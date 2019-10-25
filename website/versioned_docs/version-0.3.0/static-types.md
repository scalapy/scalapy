---
id: version-0.3.0-static-types
title: Static Types in ScalaPy
sidebar_label: Static Types
original_id: static-types
---

One of the most important parts of Scala is its strong type system. ScalaPy lets you continue using this even as you use dynamically-typed Python libraries by defining static type definitions.

## Defining Type Facades

Creating type definitions in ScalaPy is very similar to creating them with Scala.js. Type definitions are just annotated traits with methods defining what is available on the underlying Python value.

For example, we could define a static type definition for the built-in `string` module.

```scala
import me.shadaj.scalapy.py

@py.native trait StringModuleFacade extends py.Object {
  def digits: String = py.native
}
```

Once you have this type facade, it is usable with the `.as` method just like converting to existing Scala types. So, to get a type-safe reference to the `string` module, we import it and convert it to our facade type.

```scala
val string = py.module("string").as[StringModuleFacade]
// string: StringModuleFacade = <module 'string' from '/usr/lib/python3.7/string.py'>
string.digits
// res0: String = "0123456789"
```

If we try to access something that doesn't exist in our type, we get the expected error message

```scala
string.ddigits
// error: value ddigits is not a member of repl.Session.App.StringModuleFacade
// string.ddigits
// ^^^^^^^^^^^^^^
```

## Special Types

Due to Python's dynamically typed nature, some APIs can have types that don't easily map to Scala constructs. To help with this, ScalaPy includes some special types to help defining static types for these situations easier.

### `py.|`

ScalaPy includes the union type `py.|` which can represent situations where one of two types is required. For example, the Python `Random` class can be initialized with a seed that is an integer or a string. We could define a type facade as

```scala
@py.native trait PythonRandomModule extends py.Object {
  def Random(a: py.|[Int, String]): py.Dynamic = py.native
}
```

And use it with either input type

```scala
val random = py.module("random").as[PythonRandomModule]
// random: PythonRandomModule = <module 'random' from '/usr/lib/python3.7/random.py'>
random.Random(123)
// res2: py.Dynamic = <random.Random object at 0x7f7fdb6e28d8>
random.Random("123")
// res3: py.Dynamic = <random.Random object at 0x7f7fdb6e0bf8>
```

