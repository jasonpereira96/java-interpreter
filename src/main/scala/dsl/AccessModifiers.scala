package dsl

object AccessModifiers extends Enumeration
{
  type AccessModifiers = Value

  val PUBLIC: dsl.AccessModifiers.Value = Value("PUBLIC")
  val PROTECTED: dsl.AccessModifiers.Value = Value("PROTECTED")
  val PRIVATE: dsl.AccessModifiers.Value = Value("PRIVATE")
}