<p align="center"><img width="800" src="https://github.com/shadaj/scalapy/raw/master/logo.png"/></p>
<p align="center"><i>Use the world of Python from the comfort of Scala!</i></p>
<p align="center">
  <a href="https://travis-ci.com/shadaj/scalapy">
    <img src="https://travis-ci.com/shadaj/scalapy.svg?branch=master"/>
  </a>
  <img src="https://img.shields.io/maven-central/v/me.shadaj/scalapy_2.12.svg"/>
  <a href="https://gitter.im/shadaj/scalapy?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge">
    <img src="https://badges.gitter.im/shadaj/scalapy.svg"/>
  </a>
</p>

ScalaPy allows you to use Python libraries from your Scala code through interfaces and conversions built on top of [Jep](https://github.com/mrj0/jep).

## Getting Started
ScalaPy is published to Maven Central, so you can add it by including it in your dependencies.

```scala
libraryDependencies += "me.shadaj" %% "scalapy" % "0.2.0"
```

To run ScalaPy apps through SBT, you will need to configure forking as well as Java options to include the Jep native libraries.

To enable forking:
```scala
fork in run := true
```

To add the Jep native libraries (after installing with `pip install jep`):
```scala
javaOptions += "-Djava.library.path=/usr/local/lib/python3.6/site-packages/jep"
```

To get started with working with ScalaPy, check out [the blog post introducing it](http://blog.shadaj.me/2017/01/04/tensorflow-in-scala-with-scalapy.html).

## Building
First, [build Jep](https://github.com/mrj0/jep/wiki/Getting-Started) and place your jep.so and libjep.jnilib files in the `lib/` folder (you will probably have to create this) for SBT to pick them up. If you do not have a libjep.jnilib file, copy the jep.so file and rename it to libjep.jnilib. These files will need to be included in the same manner for building any project that depends on ScalaPy.

When you are running, you may need to provide a path to your libraries when launching SBT by using a command such as `sbt -Djava.library.path=/your/path/to/lib/`.

## Static Facades
+ NumPy: https://github.com/shadaj/scalapy-numpy
+ TensorFlow: https://github.com/shadaj/scalapy-tensorflow

## Credits
Logo based on https://github.com/OlegIlyenko/scala-icon with modifications to use the Python color palette.
