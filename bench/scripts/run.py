#!/usr/bin/env python
import sys
import os
import errno
import subprocess as subp
import shutil as sh

import pathlib
scripts_dir = pathlib.Path(__file__).parent.absolute()

def mkdir(path):
    try:
        os.makedirs(path)
    except OSError as exc: # Python >2.5
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else:
            raise

def slurp(path):
    with open(path) as f:
        return f.read().strip()

def where(cmd):
    if os.path.isfile(cmd):
        return cmd
    else:
        paths = os.environ['PATH'].split(os.pathsep)
        for p in paths:
            f = os.path.join(p, cmd)
            if os.path.isfile(f):
                return f
        else:
            return None

def run(cmd):
    print(">>> " + str(cmd))
    return subp.check_output(cmd)

def compile(bench, compilecmd):
    cmd = [sbt, '-J-Xmx6G', 'benchJVM/clean', 'benchNative/clean']
    cmd.append('set benchJVM/Compile/mainClass := Some("{}")'.format(bench))
    cmd.append('set benchNative/Compile/mainClass := Some("{}")'.format(bench))
    cmd.append(compilecmd)
    return run(cmd)

sbt = where('sbt')

sizes = [2 ** x for x in range(1, 15)]

benchmarks = [
    ('CreatePythonCopyBenchmark', sizes, None),
    ('CreatePythonProxyBenchmark', sizes, None),
    ('SumPythonCopyBenchmark', sizes, None),
    ('SumPythonProxyBenchmark', sizes, None),
    ('SumScalaBenchmark', sizes, None),
    ('TensorFlowAppScalaPyBenchmark', [0], 50),
    ('TensorFlowAppPythonBenchmark', [0], 50),
]

configurations = [
    'jvm',
    'scala-native',
]

runs = 20
batches = 4000
batch_size = 1

def setup_and_compile(conf, bench):
    compilecmd = slurp(os.path.join(scripts_dir, "..", 'confs', conf, 'compile'))
    compile(bench, compilecmd)

if __name__ == "__main__":
    for conf in configurations:
        if conf == "jvm":
            setup_and_compile(conf, "")
        
        for (bench, sizes, custom_batches) in benchmarks:
            print('--- conf: {}, bench: {}'.format(conf, bench))

            if not conf == "jvm":
                setup_and_compile(conf, bench)
            
            for size_input in sizes:
                print('--- conf: {}, bench: {}, size'.format(conf, bench, size_input))
                runcmd = slurp(os.path.join(scripts_dir, "..", 'confs', conf, 'run')) \
                    .replace('$BENCH', bench) \
                    .replace('$HOME', os.environ['HOME']) \
                    .replace("$PYTHON_LIB", os.environ['PYTHON_LIB']).split(' ')

                resultsdir = os.path.join(scripts_dir, "..", 'results', conf, bench + "-" + str(size_input))
                mkdir(resultsdir)

                for n in range(runs):
                    print('--- run {}/{}'.format(n, runs))

                    cmd = []
                    cmd.extend(runcmd)
                    cmd.extend([str(custom_batches or batches), str(batch_size), "", "", str(size_input)])
                    out = run(cmd).decode()
                    with open(os.path.join(resultsdir, str(n)), 'w+') as resultfile:
                        resultfile.write(out)

