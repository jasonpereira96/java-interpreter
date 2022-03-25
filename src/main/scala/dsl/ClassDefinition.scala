package dsl

import dsl.AccessModifiers.{AccessModifiers, PUBLIC}

import scala.collection.{immutable, mutable}
import scala.collection.mutable.ListBuffer

abstract class ClassDefinitionOption
case class Constructor(commands: Command*) extends ClassDefinitionOption
//case class Field(fieldName: String) extends ClassDefinitionOption
case class Field(fieldName: String, accessModifier: AccessModifiers = PUBLIC) extends ClassDefinitionOption
case class TypedField(typeName: String, fieldName: String, accessModifier: AccessModifiers = PUBLIC) extends ClassDefinitionOption
case class Method(name: String, commands: Command*) extends ClassDefinitionOption
case class AbstractMethod(name: String) extends ClassDefinitionOption
case class Extends(name: String) extends ClassDefinitionOption
case class Implements(interfaceName: String) extends ClassDefinitionOption
case class isAbstract(isAbstract: Boolean) extends ClassDefinitionOption
case class NestedClass(className: String, options: ClassDefinitionOption *) extends ClassDefinitionOption

class ClassDefinition(val name: String, val options: ClassDefinitionOption*) {
  private val fields = mutable.Map.empty[String, Field_] // why is this a map? maybe we can store the type of field
  private val methods = mutable.Map.empty[String, MethodDefinition]
  private val constructor = mutable.ListBuffer.empty[Command]
  private val parentClass = mutable.Set.empty[String]
  private val outerClass = mutable.Set.empty[String]
  private val metadata = mutable.Map.empty[String, Any]
  private val implementedInterfaces = mutable.Set.empty[String]

  metadata.addOne(Constants.isAbstract, false)

  for (option <- options) {
    option match {
      case Constructor(commands @ _*) => {
        this.constructor.addAll(commands.toList)
      }
      case Method(name: String, commands @ _*) => {
        this.methods.addOne(name, new MethodDefinition(name, commands.toList))
      }
      case Field(name: String, accessModifier: AccessModifiers) => {
        this.fields.addOne(name, new Field_(name, null, immutable.Map(Constants.ACCESS_MODIFIER -> accessModifier)))
      }
      case TypedField(typeName: String, name: String, accessModifier: AccessModifiers) => {
        val f = new Field_(name, null, immutable.Map(Constants.ACCESS_MODIFIER -> accessModifier))
        this.fields.addOne(name, f)
        f.setType(typeName)
      }

      case isAbstract(isAbstract) => {
        metadata(Constants.isAbstract) = isAbstract
      }

      // handling this outside for now
      // case Extends(name: String) => {
        //this.parentClass.addOne(name)
      //}
      case Extends(name) => {
        // just here to placate a match error since we're handling extends outside
      }
      case Implements(name) => {
        this.implementedInterfaces.addOne(name)
      }
      case NestedClass(name, options @ _*) => {
        // just here to placate a match error since we're handling nested class outside
      }
      case AbstractMethod(name) => {
        this.methods.addOne(name, new MethodDefinition(name, List.empty[Command],true))
      }
    }
  }

  def getName: String = this.name
  def getConstructor: immutable.List[Command] = this.constructor.toList

  def setParentClass(parentClassName: String): Unit = {
    this.parentClass.addOne(parentClassName)
  }
  def hasParentClass(): Boolean = {
    this.parentClass.size == 1
  }
  def hasMethod(methodName: String): Boolean = this.methods.contains(methodName)

  def hasField(fieldName: String): Boolean = this.fields.contains(fieldName)

  def getMethodDefinition(methodName: String): MethodDefinition = {
    if (hasMethod(methodName)) {
      return this.methods(methodName)
    }
    throw new Exception(s"Method $methodName not found in class $name")
  }

  def getParentClassName(): String = {
    if (this.parentClass.isEmpty) {
      return null
    }
    return this.parentClass.toList.head
  }

  def getFieldAccessModifier(fieldName: String): AccessModifiers = {
    if (this.fields.contains(fieldName)) { // checking only on this class, not parent classes
      return this.fields(fieldName).getAccessModifier
    } else {
      throw new Exception()
    }
  }

  def getFieldInfo(): List[Field_] = {
    return this.fields.toList.map[Field_](x => x._2)
  }

  def setOuterClass(outerClassName: String): Unit = {
    this.outerClass.addOne(outerClassName)
  }
  def hasOuterClass(): Boolean = {
    this.outerClass.size == 1
  }

  def getOuterClassName(): String = {
    if (this.outerClass.isEmpty) {
      return null
    }
    return this.outerClass.toList.head
  }

  def isAbstract(): Boolean = {
    assert(this.metadata(Constants.isAbstract).isInstanceOf[Boolean])
    this.metadata(Constants.isAbstract).asInstanceOf[Boolean]
  }
  def isConcrete(): Boolean = !this.isAbstract()

  def getMethods(): mutable.Map[String, MethodDefinition] =  this.methods

  def implementsInterface(name: String) = this.implementedInterfaces.contains(name)

  def getImplementedInterfaces: mutable.Set[String] = this.implementedInterfaces
}
