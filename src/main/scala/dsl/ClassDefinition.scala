package dsl

import scala.collection.mutable

abstract class ClassDefinitionOption
case class Constructor(commands: Command*) extends ClassDefinitionOption
case class Field(fieldName: String) extends ClassDefinitionOption
case class Method(name: String, commands: Command*) extends ClassDefinitionOption

class ClassDefinition(val name: String, val options: ClassDefinitionOption*) {
  private val fields = mutable.Map.empty[String, Any]
  private val methods = mutable.Map.empty[String, List[Command]]
  private val constructor = mutable.ListBuffer.empty[Command]

  for (option <- options) {
    option match {
      case Constructor(commands @ _*) => {
        this.constructor.addAll(commands.toList)
      }
      case Method(name: String, commands @ _*) => {
        this.methods.addOne(name, commands.toList)
      }
      case Field(name: String) => {
        this.fields.addOne(name, null)
      }
    }
  }

}
