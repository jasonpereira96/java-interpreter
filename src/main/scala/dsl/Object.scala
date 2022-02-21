package dsl

import dsl.AccessModifiers.AccessModifiers

import scala.collection.mutable

class Object(val className: String, val fields: (String, AccessModifiers, Value)*) {
  private val fieldMap = mutable.Map.empty[String, (AccessModifiers, Value)]

  for (field <- fields.toList) {
    val fieldName: String = field._1
    val accessModifier = field._2
    val fieldValue = field._3
    this.fieldMap.addOne(fieldName, (accessModifier , fieldValue))
  }

  def setField(name: String, value: Value) = {
    if (hasField(name)) {
      val accessModifier = this.fieldMap(name)._1
      this.fieldMap(name) = (accessModifier, value)
    } else {
      throw new Exception(s"object does not have field $name")
    }
  }
  def getClassName: String = this.className

  def hasField(fieldName: String): Boolean = {
    this.fieldMap.contains(fieldName)
  }

  def getField(name: String): Value = {
    if (hasField(name)) {
      this.fieldMap(name)._2
    } else {
      throw new Exception(s"object does not have field $name")
    }
  }

  def getAccessModifier(name: String): AccessModifiers = {
    if (hasField(name)) {
      this.fieldMap(name)._1
    } else {
      throw new Exception(s"object does not have field $name")
    }
  }
}
