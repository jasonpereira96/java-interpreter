package dsl

import dsl.AccessModifiers.{AccessModifiers, PUBLIC}

import scala.collection.mutable


abstract class InterfaceDefinitionOption
case class InterfaceMethod(name: String) extends InterfaceDefinitionOption
case class ExtendsInterface(name: String) extends InterfaceDefinitionOption

class InterfaceDefinition(val name: String, val options: InterfaceDefinitionOption*) {
  private val methods = mutable.Map.empty[String, MethodDefinition]
  private val parentInterface = mutable.Set.empty[String]

  for (option <- options) {
    option match {
      case InterfaceMethod(name: String) => {
        this.methods.addOne(name, new MethodDefinition(name, List()))
      }
      case ExtendsInterface(name_) => {
        // just here to placate a match error since we're handling extends outside
        println(s"$name extends interface $name_")
      }
    }
  }
  
  def hasParentInterface(): Boolean = {
     !this.parentInterface.isEmpty
  }
  def setParentInterface(name: String): Unit = {
    this.parentInterface.addOne(name)
  }
  def getParentInterface(): String = {
    this.parentInterface.head
  }
  def getMethods: mutable.Map[String, MethodDefinition] = this.methods
}
