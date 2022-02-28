package dsl

import dsl.AccessModifiers.AccessModifiers

class Field_(val name: String, val value: Value = null, val accessModifier: AccessModifiers = AccessModifiers.PUBLIC) {
  def getValue: Value = value

  def getAccessModifier: AccessModifiers = accessModifier

  def getName: String = name

  def getValueOption: Option[Value] = {
    if (value == null) {
      return None
    }
    Some(value)
  }
}
