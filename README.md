# scalapy
*use the world of Python from the comfort of Scala!*

## Building
First, [build Jep](https://github.com/mrj0/jep/wiki/Getting-Started) and place your jep-*.jar, jep.so, and libjep.jni files in the `lib/` folder for SBT to pick them up.
When you are running tests, you may need to provide a path to your libraries when launching SBT by using a command like `sbt -Djava.library.path=/your/path/to/lib/`. 