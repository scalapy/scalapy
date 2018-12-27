package me.shadaj.scalapy.py

import jep.Jep

import scala.language.dynamics
import scala.collection.mutable

class Object(val variableId: Int)(implicit jep: Jep) { self =>
  import Object._
  final val expr = s"$objnmPrefix$variableId"
  def value: Any = jep.getValue(expr)

  override def toString: String = {
    jep.getValue(s"str($expr)").asInstanceOf[String]
  }

  override def finalize(): Unit = {
    jep.eval(s"del $expr")
/*
    if (!cleaned) {
      //      println(s"finalized var ${expr}")
      cleaned = true
    }
*/
  }

  def as[T: ObjectReader]: T = implicitly[ObjectReader[T]].read(new ValueAndRequestObject(jep.getValue(expr)) {
    override def getObject: Object = self
  })(jep)
}

object Object {
  val objnmPrefix = "spy_o_"
  private var nextCounter: Int = 0
//  private[py] var allocatedObjects: List[mutable.Queue[Object]] = List.empty
 // memory leak here!
  def empty(implicit jep: Jep): DynamicObject = {
    val variableName = nextCounter
    nextCounter += 1

    new DynamicObject(variableName)
  }

  def apply(stringToEval: String)(implicit jep: Jep): DynamicObject = {
    val variableName = nextCounter
    nextCounter += 1

    //todo fix mem leak break things
    jep.eval(s"$objnmPrefix$variableName = $stringToEval")
//    jep.eval(stringToEval)
    new DynamicObject(variableName)
  }

/*
  /**
   * Constructs a Python value by populating a generated variable, usually via Jep calls.
   * @param populateVariable a function that populates a variable given its name and the Jep instance
   */
  def apply(populateVariable: (String, Jep) => Unit)(implicit jep: Jep): Object = {
    val ret = Object.empty
    populateVariable(ret.expr, jep)
    
    ret
  }
*/

  def populateWith(v: Any)(implicit jep: Jep): DynamicObject = {

    val ret = Object.empty
//    populateVariable(ret.expr, jep)
    jep.set(ret.expr,v)
    ret

/*
    apply { (variable, j) =>
      j.set(variable, v)
    }
*/
  }

  implicit def from[T](v: T)(implicit writer: ObjectWriter[T], jep: Jep): Object = {
    writer.write(v)(jep).left.map(Object.populateWith).merge
  }
}
