package dsl

import dsl.AccessModifiers.AccessModifiers

import scala.collection.mutable

class Field_(val name: String, val value: Value = null, val accessModifier: AccessModifiers = AccessModifiers.PUBLIC) {

  private val metadata = mutable.Map.empty[String, Any]

  metadata("fieldType") = null

  def getValue: Value = value

  def getAccessModifier: AccessModifiers = accessModifier

  def getName: String = name

  def getValueOption: Option[Value] = {
    if (value == null) {
      return None
    }
    Some(value)
  }

  def setType(fieldType: String) = {
    metadata("fieldType") = fieldType
  }

  def hasType(): Boolean = {
    metadata("fieldType") != null
  }

  def getType(): Option[String] = {
    if (hasType()) {
      return Some(metadata("fieldType").asInstanceOf[String])
    }
    return None
  }
}
