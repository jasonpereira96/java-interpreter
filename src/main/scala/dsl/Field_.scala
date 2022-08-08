package dsl

import dsl.AccessModifiers.AccessModifiers

import scala.collection.mutable
import scala.collection.immutable

class Field_(val name: String, val value: Expression = null, val options: immutable.Map[String, Any] = immutable.Map.empty[String, Any]) {

  private val metadata = mutable.Map.empty[String, Any]

  metadata("fieldType") = null
  metadata(Constants.FINAL) = false

  for ((k, v) <- options) {
    k match {
      case Constants.ACCESS_MODIFIER =>
        metadata(Constants.ACCESS_MODIFIER) = options(Constants.ACCESS_MODIFIER)
      case Constants.FINAL =>
        metadata(Constants.FINAL) = options(Constants.FINAL)
    }
  }

  def getValue: Expression = value

  def getAccessModifier: AccessModifiers = metadata(Constants.ACCESS_MODIFIER).asInstanceOf[AccessModifiers]

  def getName: String = name

  def getValueOption: Option[Expression] = {
    if (value == null) {
      return None
    }
    Some(value)
  }

  def setType(fieldType: String): Unit = {
    metadata("fieldType") = fieldType
  }

  def hasType(): Boolean = {
    metadata("fieldType") != null
  }

  def getType(): Option[String] = {
    if (hasType()) {
      return Some(metadata("fieldType").asInstanceOf[String])
    }
    None
  }

  def isFinal(): Boolean = metadata(Constants.FINAL).asInstanceOf[Boolean]
}
