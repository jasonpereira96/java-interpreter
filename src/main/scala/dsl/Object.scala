package dsl

import scala.collection.mutable

class Object(val className: String, val fields: (String, Any)*) {
  private val fieldMap = mutable.Map.empty[String, Any]

  for (field <- fields.toList) {
    val fieldName = field._1
    val fieldValue = field._2
    this.fieldMap.addOne(fieldName, fieldValue)
  }

  def getClassName: String = this.className
}
