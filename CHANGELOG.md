# Changelog
## vNEXT
### Bug Fixes :bug:
+ Fix a segfault in Scala Native when the interpreter is initialized outside of a `py.local { ... }` block
+ Correctly handle reading a list-like object (such as a NumPy array) into a `Seq` in Scala Native

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
