---
id: static-types
title: Static Types in ScalaPy
sidebar_label: Static Types
---

One of the most important parts of Scala is its strong type system. ScalaPy lets you continue using this even as you use dynamically-typed Python libraries by defining static type definitions.

## Defining Type Facades
Creating type definitions in ScalaPy is very similar to creating them with Scala.js. Type definitions are just annotated traits with methods defining what is available on the underlying Python value.

For example, we could define a static type definition for the built-in string type.

```scala mdoc
import me.shadaj.scalapy.py

@py.native trait PyString extends py.Object {
  def count(subsequence: String): Int = py.native
}
```

Once you have this type facade, it is usable with the `.as` method just like converting to existing Scala types. So, to get a type-safe reference to the Python string we have loaded, we convert it to our facade type.

```scala mdoc
val string = py.module("string").digits.as[PyString]
string.count("123")
```

If we try to call this method with the wrong parameter type, we get the expected error message

```scala mdoc:fail
string.count(123)
```

## Static Module Types
When dealing with modules, ScalaPy offers an additional type `StaticModule` that makes it possible to map a top-level Scala object to a Python module. For example, to create a static facade to the `string` module we saw earlier, we can define a `StaticModule` facade.

```scala mdoc
@py.native object StringsModule extends py.StaticModule("string") {
  def digits: String = py.native
}

StringsModule.digits
```

## Special Types
Due to Python's dynamically typed nature, some APIs can have types that don't easily map to Scala constructs. To help with this, ScalaPy includes some special types to help defining static types for these situations easier.

### `py.|`
ScalaPy includes the union type `py.|` which can represent situations where one of two types is required. For example, the Python `Random` class can be initialized with a seed that is an integer or a string. We could define a type facade as

```scala mdoc
@py.native trait PythonRandomModule extends py.Object {
  def Random(a: py.|[Int, String]): py.Dynamic = py.native
}
```

And use it with either input type
```scala mdoc
val random = py.module("random").as[PythonRandomModule]
random.Random(123)
random.Random("123")
```
