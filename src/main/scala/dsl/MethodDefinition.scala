package dsl

class MethodDefinition (val name: String, val commands: List[Command], val isAbstract: Boolean = false) {
  def isConcrete(): Boolean = {
    !this.isAbstract
  }
}
