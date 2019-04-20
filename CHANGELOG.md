# Changelog
## vNEXT
### Breaking Changes :warning:
+ `py.DynamicObject` has been renamed to `py.Dynamic` to better match the Scala.js naming scheme
+ Casting to `DynamicObject` with `.asInstanceOf[DynamicObject]` has been replaced by just calling `.asDynamic`
+ Facades are now declared as `@py.native trait MyFacade extends Object { ... }` instead of a class that extends `ObjectFacade` (which has been removed)
