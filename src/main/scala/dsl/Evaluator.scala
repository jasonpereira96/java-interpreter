package dsl

import scala.collection.{immutable, mutable}
import dsl.Constants._


class Evaluator {
  /**
   * The evaluator maintains a stack of states. Each state is implemented as a Map[String, dsl.Value].
   * A state is a map from variable names to value of that variable.
   * A stack of states is required because each scope has it own state which consists of the local
   * variables of that scope.
   */
  private val SCOPE_NAME = "__SCOPE_NAME__"
  private val stack = mutable.Stack[ScopeRecord]()
  this.stack.push(new ScopeRecord())

  /**
   * @return Returns the state of the current scope of execution
   */
  private def getState() = this.stack.top.getState()

  /**
   * Modifies the topmost state on the stack.
   * @param newState Returns the new state after modification
   */
  private def setState(newState: mutable.Map[String, Value]): Unit = {
    val oldScopeRecord = this.stack.pop()
    this.stack.push(new ScopeRecord(oldScopeRecord.getName(), newState))
  }

  /**
   * Pushes a new Map on the stack. Called when entering a new scope.
   * @oaram name The name of the scope to be created
   */
  private def pushStackFrame(name: String = UNNAMED): Unit = {
    this.stack.push(new ScopeRecord(name))
  }
  /**
   * Pops a Map from the stack. Called when exiting a scope.
   */
  private def popStackFrame(): Unit = {
    this.stack.pop()
  }

  /**
   * Searches down the stack of states for a variable. If the variable is defined in the current scope,
   * will return the value of that variable.
   * If it is not found in the current scope, it will check whether that variable is defined in any of the
   * outer scopes.
   * If it cannot find the variable at all, lookup() will throw an error
   * @param name Name of the variable to lookup.
   * @return The value of that variable
   */
  private def lookup(name: String): Value = {
    for(index <- this.stack.indices by 1) {
      val state = stack(index).getState()
      if (state.contains(name)) {
        return state(name)
      }
    }
    throw new Exception(s"$name is undefined")
  }
  private def lookupScope(scopeName: String): ScopeRecord = {
    for(index <- this.stack.indices by 1) {
      val scopeRecord = stack(index)
      val currentScopeName = scopeRecord.getName()
      if (currentScopeName == scopeName) {
        return stack(index)
      }
    }
    throw new Exception(s"Scope $scopeName is not defined")
  }
  // we can do away with this
  private def lookupWithScopeName(scopeName: String, varName: String): Value = {
    for(index <- this.stack.indices by 1) {
      val state = stack(index).getState()
      val currentScopeName = stack(index).getName()
      if (currentScopeName == scopeName) {
        if (state.contains(varName)) {
          return state(varName)
        }
      }
    }
    throw new Exception(s"$varName is undefined")
  }

  /**
   * Takes a dsl.Program object and executes the commands in it.
   * @param program A list of commands to run
   * @return Returns a Map that represents the final state of the program after all statements have been executed.
   */
  def runProgram(program: Program): immutable.Map[String, Value] = {
    this.stack.clear()
    this.stack.push(new ScopeRecord())
    for (command: Command <- program.commands) {
      execute(command)
    }
    stack.top.getState().toMap
  }

  /**
   * Takes a variable number of commands and executes them.
   * @param commands The commands to run
   * @return Returns a Map that represents the final state of the program after all statements have been executed.
   */
  def run(commands: Command*): immutable.Map[String, Value] = {
    this.runProgram(new Program(commands.toList))
    stack.top.getState().toMap
  }

  /**
   * Evaluates a list of expressions to their corresponding values
   * @param expressions A list of expressions to evaluate
   * @return A list of values which are the results of the evaluated expressions
   */
  private def evaluateExpressions (expressions: List[Expression]): List[Value] = expressions.map[Value](exp => evaluate(exp))
  /**
   * Evaluates an expression to a value
   * @param exp The expression to evaluate
   * @return The value that the expression is evaluated to
   */
  private def evaluate(exp: Expression): Value = {
    exp match {
      case Value(v) => Value(v)
      case Variable(name) =>
        try {
          val v = lookup(name).value

          v match {
            case exp: Expression =>
              evaluate(exp)
            case _ =>
              Value(v)
          }
        } catch {
          case _ :Throwable => throw new Exception(s"dsl.Variable $name not found")
        }
      case ScopeResolvedVariable(scopeName: String, varName) =>
        try {

          if (this.stack.filter(_.getName() == scopeName).length == 0) {
            throw new Exception(s"dsl.Scope name $scopeName not found")
          }
          val v = lookupWithScopeName(scopeName, varName).value

          v match {
            case exp: Expression =>
              evaluate(exp)
            case _ =>
              Value(v)
          }
        } catch {
          case _ :Throwable => throw new Exception(s"dsl.Variable $varName not found")
        }
      case Union(exp1, exp2) =>
        val v1 = evaluate(exp1)
        val v2 = evaluate(exp2)
        assert(v1.value.isInstanceOf[mutable.Set[Value]])
        assert(v2.value.isInstanceOf[mutable.Set[Value]])

        val set1: mutable.Set[Value] = v1.value.asInstanceOf[mutable.Set[Value]]
        val set2: mutable.Set[Value] = v2.value.asInstanceOf[mutable.Set[Value]]

        Value(set1.union(set2))
      case Difference(exp1, exp2) =>
        val expressions: List[Value] = evaluateExpressions(List(exp1, exp2))
        val set1: mutable.Set[Value] = expressions.head.value.asInstanceOf[mutable.Set[Value]]
        val set2: mutable.Set[Value] = expressions(1).value.asInstanceOf[mutable.Set[Value]]
        Value(set1.diff(set2))
      case Intersection(exp1, exp2) =>
        val expressions: List[Value] = evaluateExpressions(List(exp1, exp2))
        val set1: mutable.Set[Value] = expressions.head.value.asInstanceOf[mutable.Set[Value]]
        val set2: mutable.Set[Value] = expressions(1).value.asInstanceOf[mutable.Set[Value]]
        Value(set1.intersect(set2))
      case SymmetricDifference(exp1, exp2) =>
        val expressions: List[Value] = evaluateExpressions(List(exp1, exp2))
        val set1: mutable.Set[Value] = expressions.head.value.asInstanceOf[mutable.Set[Value]]
        val set2: mutable.Set[Value] = expressions(1).value.asInstanceOf[mutable.Set[Value]]
        val union = set1.union(set2)
        val intersection = set1.intersect(set2)
        Value(union.diff(intersection))
      case CartesianProduct(exp1, exp2) =>
        val expressions: List[Value] = evaluateExpressions(List(exp1, exp2))
        val set1: mutable.Set[Value] = expressions.head.value.asInstanceOf[mutable.Set[Value]]
        val set2: mutable.Set[Value] = expressions(1).value.asInstanceOf[mutable.Set[Value]]
        val result = mutable.Set.empty[Value]

        // computing the Cartesian Product of 2 sets
        set1.foreach(v1 => {
          set2.foreach(v2 => {
            val tuple_ = (v1.value, v2.value)
            result.add(Value(tuple_))
          })
        })
        Value(result)
      case CheckIfContains(exp1, exp2) =>
        val expressions: List[Value] = evaluateExpressions(List(exp1, exp2))
        val set1: mutable.Set[Value] = expressions.head.value.asInstanceOf[mutable.Set[Value]]
        val v: Value = expressions(1)
        Value(set1.contains(v))
      case NewObject(className, args @_*) => {
        try {
          val classDefValue = lookup(className)
          assert(classDefValue.value.isInstanceOf[ClassDefinition])
          val classDef = classDefValue.value.asInstanceOf[ClassDefinition]

          pushStackFrame(s"Constructor call of ${classDef.name}")
          // this is dangerous and may have deadly side effects :'(
          for (c <- classDef.getConstructor) {
            execute(c)
          }
          popStackFrame()

        } catch {
          case _ :Throwable => throw new Exception(s"class $className is not defined in this scope")
        }
        val o = new dsl.Object(className)
        Value(o)
      }

      case _ =>
        Value(99)
    }
  }

  /**
   * Executes a specified command.
   * @param command The command to execute
   */
  private def execute(command: Command): Unit = {
    command match {

      case CreateNewSet(name) =>
        val state = this.getState()
        val newState = state + (name -> Value(mutable.Set.empty[Value]))
        this.setState(newState)
      case Assign(variable: Any, exp) =>
        variable match {
          case Variable(name) => {
            val newState = this.getState() + (name -> evaluate(exp))
            this.setState(newState)
          }
          case ScopeResolvedVariable(scopeName: String, varName: String) => {
            val sr = lookupScope(scopeName)
            val state = sr.getState()
            state(varName) = evaluate(exp)
          }
          // TODO
          // case This => {}
          case _ => {
            throw new Error("Assign() parameter type invalid")
          }
        }
      case Insert(ident, expressionsSeq @ _*) =>
        val name = ident
        val expressions = expressionsSeq.toList
        val set: mutable.Set[Value] = this.lookup(name).value.asInstanceOf[mutable.Set[Value]]
        for (exp: Expression <- expressions) {
          set.add(evaluate(exp))
        }
        val newState = this.getState() + (name -> Value(set))
        this.setState(newState)
      case Delete(ident, expressionsSeq @ _*) =>
        val name = ident
        val expressions = expressionsSeq.toList
        val set: mutable.Set[Value] = this.lookup(name).value.asInstanceOf[mutable.Set[Value]]
        for (exp: Expression <- expressions) {
          set.remove(evaluate(exp))
        }
        val newState = this.getState() + (name -> Value(set))
        this.setState(newState)
      case DefineMacro(name, expression) =>
        val newState = this.getState() + (name -> Value(expression))
        this.setState(newState)
      case Scope(name, commands@_*) =>
        val commandsList = commands.toList
        this.pushStackFrame(UNNAMED) // push a stack from onto the stack since we're entering a new scope
        for (command: Command <- commandsList) {
          execute(command)
        }
        this.popStackFrame() // pop the stack frame from the stack

      case NamedScope(name, commands@_*) =>
        val commandsList = commands.toList
        this.pushStackFrame(name) // push a stack from onto the stack since we're entering a new scope
        for (command: Command <- commandsList) {
          execute(command)
        }
        this.popStackFrame() // pop the stack frame from the stack
      case Display(message, identifier) =>
        println(message)
        val variable = lookup(identifier)

        variable.value match {
          case set: mutable.HashSet[Value] =>
            println("{" + set.map[Any](v => v.value).mkString(",") + "}")

          case x =>
            println(x)
        }
      case DefineClass(name, options @ _*) => {
        // check for multiple inheritance
        if (options.filter(option => option.isInstanceOf[Extends]).length > 1) {
          throw new Exception(s"Two or more extends clauses found. Multiple inheritance is not allowed.")
        }

        // need to check for class already defined
        // TODO

        val classDefinition = new ClassDefinition(name, options: _*)

        // check for single inheritance
        for (o: ClassDefinitionOption <- options) {
          o match {
            case Extends(parentClassName) => {
              try {
                val classDefinitionValue = lookup(parentClassName)
                assert(classDefinitionValue.value.isInstanceOf[ClassDefinition])
                // set the name of the parent class given in the extends clause
                classDefinition.setParentClass(classDefinitionValue.value.asInstanceOf[ClassDefinition].getName)
              } catch {
                case _ :Throwable => throw new
                    Exception(s"Parent class of the extends clause $parentClassName not defined")
              }
            }
            case _ => {}
          }
        }
        val newState = this.getState() + (name -> Value(classDefinition))
        this.setState(newState)
      }
      case PrintStack() => {
        for (sr <- this.stack) {
          println(sr)
        }
      }
    }
  }

  /**
   * Checks if a particular value is present in a given set.
   * @param setName The set name to check
   * @param value The value to check
   * @return A boolean that represents if a particular value is present in a given set.
   */
  def Check(setName: String, value: Value): Boolean = {
    // for now, let's assume that Check is used only after the whole program has run
    assert(this.stack.size == 1)
    val state = getState()
    if (!state.contains(setName)) {
      throw new Exception(s"$setName is not defined")
    }
    // check if the thing is a set in the first place
    val A_value = state(setName)
    val set: mutable.Set[Value] = A_value.value.asInstanceOf[mutable.Set[Value]]
    set.contains(value)
  }
  def CheckVariable(name: String, value: Value): Boolean = {
    assert(this.stack.size == 1)
    val currentValue = lookup(name)
    currentValue == value
  }
}
