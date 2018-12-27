package me.shadaj.scalapy.py

abstract class ValueAndRequestObject(getValue: => Any) {
  final def value: Any = getValue

  protected def getObject: Object

//  private var objectCache: Object = null
  final def requestObject: Object = {
//    if (objectCache == null) objectCache = getObject
//    objectCache
    getObject
  }
}
