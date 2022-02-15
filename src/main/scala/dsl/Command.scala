package dsl

/**
 * A command represents an operating that you can perform in my language.
 */
abstract class Command
case class Assign(ident: String, exp: Expression) extends Command
case class Insert(ident: String, expressions: Expression*) extends Command
case class Delete(ident: String, expressions: Expression*) extends Command
case class CreateNewSet(name: String) extends Command
case class Scope(name: String, commands: Command*) extends Command
case class DefineMacro(name: String, expression: Expression) extends Command
case class Display(message: String, identifier: String) extends Command
case class NewObject(referenceName: String, className: String, arguments: (String, Any)*) extends Command