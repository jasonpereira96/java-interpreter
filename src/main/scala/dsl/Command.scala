package dsl

import scala.util.control.Exception.Finally


case class Parameter(parameterName: String, value: Expression)
/**
 * A command represents an operation that you can perform in my language.
 */
abstract class Command
case class Assign(variable: Any, exp: Expression) extends Command
case class TypedAssign(className: String, variable: Any, exp: Expression) extends Command
case class Insert(ident: String, expressions: Expression*) extends Command
case class Delete(ident: String, expressions: Expression*) extends Command
case class CreateNewSet(name: String) extends Command
case class Scope(commands: Command*) extends Command
case class NamedScope(name: String, commands: Command*) extends Command
case class DefineMacro(name: String, expression: Expression) extends Command
case class If(expression: Expression, commands: Command*) extends Command
case class IfElse(expression: Expression, ifStatements: List[Command], elseStatements: List[Command]) extends Command
case class ExceptionClassDef(className: String) extends Command
case class Try(commands: List[Command], catchBlocks: List[CatchBlock], finallyBlock: FinallyBlock = Constants.DEFAULT_FINALLY_BLOCK) extends Command
case class CatchBlock(className: String, commands: Command*) extends Command
case class FinallyBlock(commands: Command*) extends Command
case class Throw(className: String, reason: String = "") extends Command
//case class Catch(className: String) extends Command
case class Display(message: String, identifier: String) extends Command
//case class NewObject(referenceName: String, className: String, arguments: (String, Any)*) extends Command
case class DefineClass(className: String, options: ClassDefinitionOption *) extends Command
case class DefineInterface(interfaceName: String, options: InterfaceDefinitionOption *) extends Command
case class InvokeMethod(returnee: Variable, objectName: String, methodName: String, params: Parameter*) extends Command
case class Return(exp: Expression) extends Command
case class Print(message: Any) extends Command
case class PrintStack() extends Command
case class Assert(value: Boolean) extends Command

// References: https://docs.python.org/3/library/ast.html#ast.If