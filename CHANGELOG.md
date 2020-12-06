# Changelog
## vNEXT
### Bug Fixes :bug:
+ Add `python3.7m` to the default list of Python native libraries to search for, since Conda does not expose `python3.7` (without the `m`) ([PR #119](https://github.com/shadaj/scalapy/pull/119))
+ Add a writer for `Unit` that generates a `None` value, which is useful for callbacks that don't return anything ([PR #120](https://github.com/shadaj/scalapy/pull/120))

## v0.4.0
### Highlights :tada:
+ The JVM interface to Python has been completely rewritten from scratch to share all of its logic with the Scala Native backend by binding directly to CPython with JNA. This means that moving forward, ScalaPy JVM and Native will always have the same features and use near-identical logic for talking to Python libraries
+ Readers and Writers have been simplified to always work in terms of Python interpreter values, simplifying the implementation and reducing intermediate allocations
+ Adds support for sending Scala functions into Python as lambdas, and reading Python lambdas into Scala functions
+ Adds proxy collections, which are Python objects that wrap over Scala collections instead of copying the underlying data
+ Introduces `StaticModule` API for loading modules into statically-typed objects
+ Expands support of `bracketAccess` to any `py.Any` type for the index
+ Adds `bracketUpdate` API to perform the equivalent of a Python `obj[key] = newValue`
+ The version of the Python native library can now be controlled with `SCALAPY_PYTHON_LIBRARY`

### Breaking Changes :warning:
+ Renamed `py.global` to `py.Dynamic.global` to emphasize that global access through it is not type-safe
+ Drops automatic conversion of Scala collections to Python in favor of `toPythonCopy` and `toPythonProxy` methods from the `py.SeqConverters` extension
+ Renames `arrayAccess` to `bracketAccess`
+ The `CPythonInterpreter` object should now be used to access low-level interpreter functions instead of `py.interpreter`

### Bug Fixes :bug:
+ Various reference count synchronization bugs have been fixed to ensure that Python values are not being leaked or used after being freed
+ Fix a segfault in Scala Native when the interpreter is initialized outside of a `py.local { ... }` block
+ Correctly handle reading a list-like object (such as a NumPy array) into a `Seq`

## v0.3.0
### Highlights :tada:
+ ScalaPy now has a website! Check it out at [scalapy.dev](https://scalapy.dev)
+ The `py""` interpolator now makes it possible to interpret bits of Python code with references to Scala values

### Breaking Changes :warning:
+ `py.Any` is now the default type taken in and returned by operations
+ The apply method of `py.Object` to interpret abritrary strings has been replaced by the `eval()` method.
+ `py.DynamicObject` has been renamed to `py.Dynamic` to better match the Scala.js naming scheme
+ Casting to `DynamicObject` with `.asInstanceOf[DynamicObject]` has been replaced by just calling `.as[Dynamic]`
+ Facades are now declared as `@py.native trait MyFacade extends Object { ... }` instead of a class that extends `ObjectFacade` (which has been removed)
