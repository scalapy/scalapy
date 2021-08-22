# Changelog
## vNEXT
### Highlights :tada:
+ Add support for bracket syntax to facades. The annotation `@PyBracketAccess` can be used on methods to mark them as representing bracket access on an object. The target method must have one (to read the value) or two parameters (to update the value) ([PR #194](https://github.com/shadaj/scalapy/pull/194)).
+ The version of the Python native library can now be controlled with the `scalapy.python.library` system property ([PR #198](https://github.com/shadaj/scalapy/pull/198))
+ Enable running `Py_SetProgramName` with user provided input prior to `Py_Initialize` to set the correct paths to Python run-time libraries. Input to `Py_SetProgramName`, the Python interpreter executable, can be controlled with either the `scalapy.python.programname` system property or the `SCALAPY_PYTHON_PROGRAMNAME` environment variable ([PR #200](https://github.com/shadaj/scalapy/pull/200))

### Bug Fixes :bug:
+ Raise a `NameError` exception when attempting to call a function or access a variable that does not exist in the global namespace ([PR #207](https://github.com/shadaj/scalapy/pull/207))

## v0.5.0
### Highlights :tada:
+ Significantly optimize transfers from Scala to Python, which are now up to 5x faster on the JVM and 4x faster on Scala Native ([PR #179](https://github.com/shadaj/scalapy/pull/179))
+ Optimize transfers from Python to Scala, which are now up to 4x faster on the JVM and 3x faster on Scala Native ([PR #183](https://github.com/shadaj/scalapy/pull/183))
+ Introduce a pre-alpha type facade generator that generates Scala types through mypy ([PR #110](https://github.com/shadaj/scalapy/pull/110))
  + This is pre-alpha in the sense that types generated for any module, including Python builtins, are likely to require manual patching to compile
  + **However**, with a bit of patching, the types for many libraries are good enough to use directly without having to fall back to dynamic interfaces
  + The current type generator has been merged as a very experimental feature just so that others in the community can try it out. It will be rewritten in the near future with a cleaner architecture that will enable generation of more complex types correctly.
+ Python values can now be loaded into any immutable Scala collection type as a copy, not just `Seq` ([PR #179](https://github.com/shadaj/scalapy/pull/179))
+ Allow converting nested sequences to Python using a single call to `toPythonCopy` or `toPythonProxy` ([PR #178](https://github.com/shadaj/scalapy/pull/178))
+ Add API equivalents for the Python `del` keyword (`del foo.bar`, `del foo["key"]`, and `del foo`) ([PR #175](https://github.com/shadaj/scalapy/pull/175), [PR #177](https://github.com/shadaj/scalapy/pull/177))
+ Support referential equality of Python values with a corresponding hash-code implementation ([PR #110](https://github.com/shadaj/scalapy/pull/110))

### Breaking Changes :warning:
+ Reading a Python collection as an immutable sequence will now load a copy. To load a proxy that can observe changes, load sequences with `.as[mutable.Seq[...]]` ([PR #179](https://github.com/shadaj/scalapy/pull/179))
+ Calling the `apply` method on a `py.Dynamic` value will now directly call the original value as-is instead of the `apply` method of the value in Python ([PR #177](https://github.com/shadaj/scalapy/pull/177))
  + To call the original value with keyword arguments, you can use the new `applyNamed` API, passing in tuples of keyword arguments and values
  + To call the original `apply` method in Python, use `applyDynamic` explicitly (`myValue.applyDynamic("apply")(arg1, arg2, ...)`)

## v0.4.2
### Highlights :tada:
+ Upgrade Scala Native to 0.4.0, which brings support for Scala 2.12 and 2.13 ([PR #147](https://github.com/shadaj/scalapy/pull/147))
+ Add support for Python 3.8 and 3.9 ([PR #139](https://github.com/shadaj/scalapy/pull/139))

### Breaking Changes :warning:
+ Support for Scala 2.11 has been dropped in order to focus efforts on 2.12/2.13 ([PR #147](https://github.com/shadaj/scalapy/pull/147))

## v0.4.1
### Bug Fixes :bug:
+ Fix Python library loading on the JVM to correctly fall back to other library names ([PR #132](https://github.com/shadaj/scalapy/pull/132))
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
