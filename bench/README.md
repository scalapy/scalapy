# ScalaPy Benchmarks


## Running benchmarks

From sbt shell
```bash
// run all benchmarks
benchJVM/jmh:run
// run a specific benchmark
benchJVM/jmh:run package me.shadaj.scalapy.py.bench.CreatePythonCopyBenchmark 
```

## cusomize benchmark

pass arguments from sbt shell like `jmh:run -i 3 -wi 3 -f1 -t1 me.shadaj.scalapy.py.bench.CreatePythonCopyBenchmark` 
or edit these parameters of benchmark classes.
```scala
//@Warmup(iterations = 5, time = 100, timeUnit = MILLISECONDS)
//@Measurement(iterations = 100, time = 100, timeUnit = MILLISECONDS)
//@Fork(5)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
```
