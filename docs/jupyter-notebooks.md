---
id: jupyter-notebooks
title: Jupyter Notebooks with Almond
sidebar_label: Jupyter Notebooks
---

Just like Python, ScalaPy can be used inside Jupyter notebooks to build experiments in an interactive environment. With [Almond](https://almond.sh/), a Scala kernel implementation, using ScalaPy only requires a little configuration to be used!

If you want to get set up in a hosted environment, take a look at the [example notebook](https://colab.research.google.com/gist/shadaj/29d77180aeefc41a749273026f7d1fd9/scala-cnn-training-on-gpus-with-tensorflow.ipynb) on Google Colab. To set up a local instance, read on!

## Configuring Python Support in Jupyter
First, we must modify the default Scala kernel definition to load Python native libraries.

In your `jupyter/kernels/scala/kernel.json` file, replace the contents with
```json
{
  "language" : "scala",
  "display_name" : "Scala",
  "argv" : [
    "bash",
    "-c",
    "env LD_PRELOAD=/usr/lib/x86_64-linux-gnu/libpython3.6m.so:$LD_PRELOAD java -jar /usr/local/share/jupyter/kernels/scala/launcher.jar --connection-file {connection_file}"
  ]
}"
```

Make sure to replace `/usr/lib/x86_64-linux-gnu/libpython3.6m.so` with the path to your Python native library. `python3-config --prefix` will give you the folder containing your installation's native libraries.

## Loading ScalaPy in a notebook
ScalaPy on the JVM contains with all the logic to load native libraries built-in, so you can import ScalaPy and start using it as usual!

```scala
import $ivy.`me.shadaj::scalapy:0.3.0`

import me.shadaj.scalapy.py
```
