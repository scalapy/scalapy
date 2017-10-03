# ScalaPy
*use the world of Python from the comfort of Scala!*

ScalaPy allows you to use Python libraries from your Scala code through interfaces and conversions built on top of [Jep](https://github.com/mrj0/jep).

## Getting Started
Follow the instructions in Building, and then add the dependency on ScalaPy to your own project:

```scala
libraryDependencies += "me.shadaj" %% "scalapy" % "0.1.0-SNAPSHOT"
```

To get started with working with ScalaPy, check out [the blog post introducing it](http://blog.shadaj.me/2017/01/04/tensorflow-in-scala-with-scalapy.html).

## Building
First, [build Jep](https://github.com/mrj0/jep/wiki/Getting-Started) and place your jep.so and libjep.jnilib files in the `lib/` folder (you will probably have to create this) for SBT to pick them up. If you do not have a libjep.jnilib file, copy the jep.so file and rename it to libjep.jnilib. These files will need to be included in the same manner for building any project that depends on ScalaPy.

When you are running, you may need to provide a path to your libraries when launching SBT by using a command such as `sbt -Djava.library.path=/your/path/to/lib/`.

## Static Facades
+ NumPy: https://github.com/shadaj/scalapy-numpy
+ TensorFlow: https://github.com/shadaj/scalapy-tensorflow
