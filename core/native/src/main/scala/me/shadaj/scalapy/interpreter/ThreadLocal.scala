package me.shadaj.scalapy.interpreter

private[scalapy] class SingleThreadLocal[T](value: T) {
  def get(): T = value
}

object SingleThreadLocal {
  def withInitial[T](initial: () => T) = new SingleThreadLocal[T](initial())
}
