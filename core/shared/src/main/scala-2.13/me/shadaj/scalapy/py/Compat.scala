package me.shadaj.scalapy.py

object Compat {
  type MutableMap[K, V] = scala.collection.mutable.Map[K, V]
}
