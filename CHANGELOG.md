# Changelog
## vNEXT
### Breaking Changes :warning:
+ `py.Any` is now the default type taken in and returned by operations
+ The apply method of `py.Object` to interpret abritrary strings has been replaced by the `py""` string interpolator
+ `py.DynamicObject` has been renamed to `py.Dynamic` to better match the Scala.js naming scheme
+ Casting to `DynamicObject` with `.asInstanceOf[DynamicObject]` has been replaced by just calling `.asDynamic`
+ Facades are now declared as `@py.native trait MyFacade extends Object { ... }` instead of a class that extends `ObjectFacade` (which has been removed)
