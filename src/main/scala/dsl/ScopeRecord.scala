package dsl

import dsl.Constants._

import scala.collection.mutable.Map

class ScopeRecord(val name: String = UNNAMED, val state: Map[String, Expression] = Map.empty[String, Expression], val thisVal: dsl.Object = null) {

  def getName(): String = this.name

  def getState(): Map[String, Expression] = this.state

  def getThis: dsl.Object = this.thisVal

  override def toString: String = {
    "Scope " + this.name + ": " + this.state.toString()
  }

  def hasThis: Boolean = this.thisVal != null

  def setBinding(key: String, v: Expression): Unit = {
    this.state(key) = v
  }
  def deleteBinding(key: String): Unit = {
    this.state.remove(key)
  }
  def hasBinding(key: String): Boolean = this.state.contains(key)
}
