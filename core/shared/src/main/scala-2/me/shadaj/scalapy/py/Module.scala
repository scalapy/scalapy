package me.shadaj.scalapy.py

import scala.language.dynamics

@native trait Module extends Dynamic

object Module extends ModuleApply
