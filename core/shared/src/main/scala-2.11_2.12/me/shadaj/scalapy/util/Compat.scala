package me.shadaj.scalapy.util

object Compat {
  /**
   * This trait extends the Scala 2.12 mutable.Map to implement the Scala 2.13 mutable.Map API.
   * This bridges the following source incompatibility:
   *
   * - In Scala 2.11 and 2.12, `+=` and `-=` are abstract (must be implemented).
   * - In Scala 2.13, `+=` and `-=` are final (cannot be overridden). They are aliases for the
   *   abstract `addOne` and `subtractOne`.
   */
  trait MutableMap[K, V] extends scala.collection.mutable.Map[K, V] {
    def addOne(kv: (K, V)): this.type
    def subtractOne(k: K): this.type

    @inline final def +=(kv: (K, V)): this.type = addOne(kv)
    @inline final def -=(k: K): this.type = subtractOne(k)
  }
}
