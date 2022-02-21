package dsl

object AccessModifiers extends Enumeration
{
  type AccessModifiers = Value

  val PUBLIC = Value("PUBLIC")
  val PROTECTED = Value("PROTECTED")
  val PRIVATE = Value("PRIVATE")
}