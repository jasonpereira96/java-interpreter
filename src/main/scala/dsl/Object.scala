package dsl

import dsl.AccessModifiers.AccessModifiers

import scala.collection.{immutable, mutable}

class Object(val className: String, val fields: dsl.Field_ *) {
//class Object(val className: String, val fields: (String, AccessModifiers, Value)*) {
  private val fieldMap = mutable.Map.empty[String, Field_]
  private val outerObject = mutable.Set.empty[dsl.Object]

  for (field: dsl.Field_ <- fields.toList) {
    val fieldName: String = field.getName
    val accessModifier = field.getAccessModifier
    val fieldValue = field.getValue
    val isFinal = field.isFinal()
    this.fieldMap.addOne(fieldName, new Field_(fieldName, fieldValue, immutable.Map(Constants.ACCESS_MODIFIER -> accessModifier,
      Constants.FINAL -> isFinal)))
  }

  def setField(name: String, value: Expression): Unit = {
    if (hasField(name)) {
      val accessModifier = this.fieldMap(name).getAccessModifier
      if (this.fieldMap(name).isFinal()) {
        throw new Exception(s"Field $name is final. It cannot be changed")
      }
      this.fieldMap(name) = new Field_(name, value, immutable.Map(Constants.ACCESS_MODIFIER -> accessModifier))
    } else {
      throw new Exception(s"object does not have field $name")
    }
  }
  def getClassName: String = this.className

  def hasField(fieldName: String): Boolean = {
    this.fieldMap.contains(fieldName)
  }

  def isFieldFinal(fieldName: String): Boolean = fieldMap(fieldName).isFinal()

  def getField(name: String): Expression = {
    if (hasField(name)) {
      this.fieldMap(name).getValue
    } else {
      throw new Exception(s"object does not have field $name")
    }
  }

  def getAccessModifier(name: String): AccessModifiers = {
    if (hasField(name)) {
      this.fieldMap(name).getAccessModifier
    } else {
      throw new Exception(s"object does not have field $name")
    }
  }

  def getOuterObject(): dsl.Object = {
    this.outerObject.head
  }

  def hasOuterObject(): Boolean = {
    this.outerObject.size == 1
  }

  def setOuterObject(o: dsl.Object): Unit = {
    this.outerObject.addOne(o)
  }

//  def setParentObject(o: dsl.Object) = {
//
//  }
}
