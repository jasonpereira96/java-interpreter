package dsl

/**
 * An dsl.Expression represents a computation that eventually evaluates to a value.
 * Expressions can be nested.
 */
abstract class Expression
case class Union(exp1: Expression, exp2: Expression) extends Expression
case class Difference(exp1: Expression, exp2: Expression) extends Expression
case class Intersection(exp1: Expression, exp2: Expression) extends Expression
case class SymmetricDifference(exp1: Expression, exp2: Expression) extends Expression
case class CartesianProduct(exp1: Expression, exp2: Expression) extends Expression
case class CheckIfContains(exp1: Expression, exp2: Expression) extends Expression
case class Value(value: Any) extends Expression
case class Variable(name: String) extends Expression // used for macro expansion as well
case class ScopeResolvedVariable(scopeName: String, name: String) extends Expression
case class NewObject(className: String, outerClassObject: String = "") extends Expression
case class EqualTo(exp1: Expression, exp2: Expression) extends Expression
case class IfElseExpression(condition: Expression, exprIfTrue : Expression, exprIfFalse : Expression) extends Expression
//case class NewObject(className: String, outerClassObject: String = null, args: (String, Any)*) extends Expression
case class This(fieldName: String, outerClassName: String = "") extends Expression

