# ScalaPy
*use the world of Python from the comfort of Scala!*

ScalaPy allows you to use Python libraries from your Scala code through interfaces and conversions built on top of [Jep](https://github.com/mrj0/jep).

## Getting Started
Follow the instructions in Building, and then add the dependency on ScalaPy:

```scala
libraryDependencies += "me.shadaj" %% "scalapy" % "0.1.0-SNAPSHOT"
```

To get started with working with ScalaPy, check out [the blog post introducing it](http://blog.shadaj.me/2017/01/02/tensorflow-in-scala-with-scalapy.html).

## Building
First, [build Jep](https://github.com/mrj0/jep/wiki/Getting-Started) and place your jep-*.jar, jep.so, and libjep.jni files in the `lib/` folder for SBT to pick them up.
When you are running tests, you may need to provide a path to your libraries when launching SBT by using a command like `sbt -Djava.library.path=/your/path/to/lib/`. 

## Static Facades
+ NumPy: https://github.com/shadaj/scalapy-tensorflow
+ TensorFlow: https://github.com/shadaj/scalapy-tensorflow
