package me.shadaj.scalapy.py

import scala.language.implicitConversions

sealed class |[A, B](val value: scala.Any, val isLeft: Boolean) {
  def map[U](leftMap: A => U, rightMap: B => U): U = if (isLeft) leftMap(value.asInstanceOf[A]) else rightMap(value.asInstanceOf[B])
}

object | {
  implicit def fromLeft[A, B](v: A): A | B = new |(v, true)
  implicit def fromRight[A, B](v: B): A | B = new |(v, false)
}
