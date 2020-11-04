package me.shadaj.scalapy.util

object Compat {
  type MutableMap[K, V] = scala.collection.mutable.Map[K, V]
}
