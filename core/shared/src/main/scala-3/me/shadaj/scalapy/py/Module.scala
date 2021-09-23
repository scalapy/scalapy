package me.shadaj.scalapy.py

import scala.language.dynamics

@native class Module extends Dynamic

object Module extends ModuleApply
