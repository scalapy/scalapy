package me.shadaj.scalapy.py

import scala.language.dynamics

class DynamicObject(value: PyValue) extends Object(value) with scala.Dynamic {
  def applyDynamic(method: String)(params: Object*): DynamicObject = {
    new DynamicObject(interpreter.call(value, method, params.map(_.value)))
  }

  def applyDynamicNamed(method: String)(params: (String, Object)*): DynamicObject = {
    Object(s"$expr.$method(${params.map(t => s"${t._1} = ${t._2.expr}").mkString(",")})").asInstanceOf[DynamicObject]
  }

  def selectDynamic(term: String): DynamicObject = {
    new DynamicObject(interpreter.select(value, term))
  }

  def arrayAccess(index: Int): DynamicObject = {
    new DynamicObject(interpreter.selectList(value, index))
  }

  def dictionaryAccess(key: Object): DynamicObject = {
    new DynamicObject(interpreter.selectDictionary(value, key.value))
  }

  def unary_+(): DynamicObject = {
    Object(s"+$expr").asInstanceOf[DynamicObject]
  }

  def unary_-(): DynamicObject = {
    Object(s"-$expr").asInstanceOf[DynamicObject]
  }

  def +(that: Object): DynamicObject = {
    new DynamicObject(interpreter.binaryAdd(value, that.value))
  }

  def -(that: Object): DynamicObject = {
    new DynamicObject(interpreter.binarySub(value, that.value))
  }

  def *(that: Object): DynamicObject = {
    new DynamicObject(interpreter.binaryMul(value, that.value))
  }

  def /(that: Object): DynamicObject = {
    new DynamicObject(interpreter.binaryDiv(value, that.value))
  }

  def %(that: Object): DynamicObject = {
    new DynamicObject(interpreter.binaryMod(value, that.value))
  }
}
