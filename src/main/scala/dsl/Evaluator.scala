package dsl

import scala.collection.{immutable, mutable}
import dsl.Constants.*
import sun.security.ec.point.ProjectivePoint.Immutable

import scala.annotation.tailrec
import scala.collection.mutable.Map


class Evaluator {
  /**
   * The evaluator maintains a stack of states. Each state is implemented as a Map[String, dsl.Value].
   * A state is a map from variable names to value of that variable.
   * A stack of states is required because each scope has it own state which consists of the local
   * variables of that scope.
   */
  private val SCOPE_NAME = "__SCOPE_NAME__"
  private val stack = mutable.Stack.empty[ScopeRecord]
  private val classTable = mutable.Map.empty[String, ClassDefinition]
  private val interfaceTable = mutable.Map.empty[String, InterfaceDefinition]
  private val exceptionClassTable = mutable.Map.empty[String, String]
  this.stack.push(new ScopeRecord())

  /**
   * @return Returns the state of the current scope of execution
   */
  private def getState() = this.stack.top.getState()

  /**
   * Modifies the topmost state on the stack.
   *
   * @param newState Returns the new state after modification
   */
  private def setState(newState: mutable.Map[String, Expression]): Unit = {
    val oldScopeRecord = this.stack.pop()
    this.stack.push(new ScopeRecord(oldScopeRecord.getName(), newState, thisVal = oldScopeRecord.getThis))
  }

  /**
   * Pushes a new Map on the stack. Called when entering a new scope.
   *
   * @param name The name of the scope to be created
   */
  private def pushStackFrame(name: String = UNNAMED, state: mutable.Map[String, Expression] = mutable.Map.empty[String, Expression], thisVal: dsl.Object = null): Unit = {
    if (thisVal == null) {
      this.stack.push(new ScopeRecord(name, state))
    } else {
      this.stack.push(new ScopeRecord(name, state, thisVal = thisVal))
    }
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
   *
   * @param name Name of the variable to lookup.
   * @return The value of that variable
   */
  private def lookup(name: String): Expression = {
    for (index <- this.stack.indices by 1) {
      val state = stack(index).getState()
      if (state.contains(name)) {
        return state(name)
      }
    }
    throw new Exception(s"$name is undefined")
  }

  private def lookupSafe(name: String): Expression = {
    for (index <- this.stack.indices by 1) {
      val state = stack(index).getState()
      if (state.contains(name)) {
        return state(name)
      }
    }
    null
  }

  private def lookupScope(scopeName: String): ScopeRecord = {
    for (index <- this.stack.indices by 1) {
      val scopeRecord = stack(index)
      val currentScopeName = scopeRecord.getName()
      if (currentScopeName == scopeName) {
        return stack(index)
      }
    }
    throw new Exception(s"Scope $scopeName is not defined")
  }

  // we can do away with this
  private def lookupWithScopeName(scopeName: String, varName: String): Expression = {
    for (index <- this.stack.indices by 1) {
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
   *
   * @param program A list of commands to run
   * @return Returns a Map that represents the final state of the program after all statements have been executed.
   */
  def runProgram(program: Program): immutable.Map[String, Expression] = {
    this.stack.clear()
    this.stack.push(new ScopeRecord())
    for (command: Command <- program.commands) {
      execute(command)
      Util.runChecks(this.classTable, this.interfaceTable)
    }

    stack.top.getState().toMap
  }

  /**
   * Takes a variable number of commands and executes them.
   *
   * @param commands The commands to run
   * @return Returns a Map that represents the final state of the program after all statements have been executed.
   */
  def run(commands: Command*): immutable.Map[String, Expression] = {
    this.runProgram(new Program(commands.toList))
  }

  /**
   * Evaluates a list of expressions to their corresponding values
   *
   * @param expressions A list of expressions to evaluate
   * @return A list of values which are the results of the evaluated expressions
   */
  private def evaluateExpressions(expressions: List[Expression]): List[Expression] = expressions.map[Expression](exp => evaluate(exp))

  /**
   * Evaluates an expression to a value
   *
   * @param exp The expression to evaluate
   * @return The value that the expression is evaluated to
   */
  private def evaluate(exp: Expression): Expression = {
    exp match {
      case Value(v) => Value(v)
      case This(fieldName, outerClassName) =>
        val sr = this.stack.head
        assert(sr.hasThis)
        val currentObject = sr.getThis
        val className = currentObject.getClassName

        if (outerClassName != "") {
          val outerObject = this.getOuterObject(currentObject, outerClassName)

          outerObject match {
            case Some(o) =>
              return o.getField(fieldName)
            case None =>
              // failed to find outer class object
              throw new Exception(s"Failed to find outer class object for outer class $outerClassName")
          }
        }

        if (isFieldAccessible(currentObject, fieldName)) {
          sr.getThis.getField(fieldName)
        } else {
          throw new Exception(s"Field $fieldName is not accessible")
        }
      case Variable(name) =>
        try {
          val v = lookup(name)

          v match {
            case s: Value =>
              s
            case exp: Expression =>
              evaluate(exp)
          }
        } catch {
          case _: Throwable => {
            Variable(name)
          }
        }
      case ScopeResolvedVariable(scopeName: String, varName) =>
        try {

          if (!this.stack.exists(_.getName() == scopeName)) {
            throw new Exception(s"dsl.Scope name $scopeName not found")
          }
          val v = lookupWithScopeName(scopeName, varName)

          v match {
            case s: Value =>
              s
            case exp: Expression =>
              evaluate(exp)

          }
        } catch {
          case _: Throwable => throw new Exception(s"dsl.Variable $varName not defined")
        }
      case Union(exp1, exp2) =>
        val e1 = evaluate(exp1)
        val e2 = evaluate(exp2)

        if (e1.isInstanceOf[Value] && e2.isInstanceOf[Value]) {
          val s1 = e1.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]]
          val s2 = e2.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]]
          Value(s1.union(s2))
        } else {
          optimize(Union(e1, e2))
        }
      case Difference(exp1, exp2) =>
        val expressions: List[Expression] = evaluateExpressions(List(exp1, exp2))
        val e1: Expression = expressions.head
        val e2: Expression = expressions(1)

        if (e1.isInstanceOf[Value] && e2.isInstanceOf[Value]) {
          val s1 = e1.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]]
          val s2 = e2.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]]
          Value(s1.diff(s2))
        } else {
          optimize(Difference(e1, e2))
        }
      case Intersection(exp1, exp2) =>
        val expressions: List[Expression] = evaluateExpressions(List(exp1, exp2))
        val e1: Expression = expressions.head
        val e2: Expression = expressions(1)

        if (e1.isInstanceOf[Value] && e2.isInstanceOf[Value]) {
          val s1 = e1.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]]
          val s2 = e2.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]]
          Value(s1.intersect(s2))
        } else {
          optimize(Intersection(e1, e2))
        }
      case SymmetricDifference(exp1, exp2) =>
        val expressions: List[Expression] = evaluateExpressions(List(exp1, exp2))
        val e1: Expression = expressions.head
        val e2: Expression = expressions(1)

        if (e1.isInstanceOf[Value] && e2.isInstanceOf[Value]) {
          val s1 = e1.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]]
          val s2 = e2.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]]
          val union = s1.union(s2)
          val intersection = s1.intersect(s2)
          Value(union.diff(intersection))
        } else {
          SymmetricDifference(exp1, exp2)
        }
      case CartesianProduct(exp1, exp2) =>
        val expressions: List[Expression] = evaluateExpressions(List(exp1, exp2))
        val e1: Expression = expressions.head
        val e2: Expression = expressions(1)

        if (e1.isInstanceOf[Value] && e2.isInstanceOf[Value]) {
          val s1 = e1.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]]
          val s2 = e2.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]]
          val result = mutable.Set.empty[Value]
          s1.foreach(v1 => {
            s2.foreach(v2 => {
              val tuple_ = (v1.value, v2.value)
              result.add(Value(tuple_))
            })
          })
          Value(result)
        } else {
          CartesianProduct(exp1, exp2)
        }
      case CheckIfContains(exp1, exp2) =>
        return Value(false)
      //        val expressions: List[Value] = evaluateExpressions(List(exp1, exp2))
      //        val set1: mutable.Set[Value] = expressions.head.value.asInstanceOf[mutable.Set[Value]]
      //        val v: Value = expressions(1)
      //        Value(set1.contains(v))

      case NewObject(className, outerClassObjectName: String) =>
        val classDef = this.getClassDef(className)
        // at this we know that the class exists because getClassDef handles error checking

        val o = createObject(className, outerClassObjectName)
        Value(o)
      case EqualTo(exp1: Expression, exp2: Expression) =>
        val e1 = evaluate(exp1)
        val e2 = evaluate(exp2)
        (e1, e2) match {
          case (v1: Value, v2: Value) => Value(v1.value == v2.value)
          case _ => EqualTo(e1, e2)
        }

      case Add(exp1: Expression, exp2: Expression) =>
        val e1 = evaluate(exp1)
        val e2 = evaluate(exp2)
        (e1, e2) match {
          case (v1: Value, v2: Value) => Util.addValues(v1, v2)
          case _ => optimize(Add(e1, e2))
        }

      case IfElseExpression(exp, exprIfTrue, exprIfFalse) =>
        evaluate(exp) match {
          case v: Value => {
            if (Util.isTruthy(v)) {
              evaluate(exprIfTrue)
            } else {
              evaluate(exprIfFalse)
            }
          }
          case e: Expression => {
            IfElseExpression(evaluate(exp), evaluate(exprIfTrue), evaluate(exprIfFalse))
          }
        }
      case dsl.Map(expression: Expression, anonymousFunction: AnonymousFunction) =>
        val s1 = evaluate(expression)
//        assert(s1.isInstanceOf[Set])
        //val newState = state + (name -> Value(mutable.Set.empty[Value]))

        val s2 = s1.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]]

        val resultSet = mutable.Set.empty[Expression]
        for (v: Value <- s2) {
          val currentThisVal = this.stack.top.getThis
          val newState = this.getState() + (Constants.ELEMENT -> v)
          pushStackFrame("MAP", newState, currentThisVal)
          for (command: Command <- anonymousFunction.commands) {
            execute(command)
          }
          assert(this.stack(1).hasBinding(RETURN))
          val retVal = this.stack(1).state(RETURN)
          this.stack(1).state.remove(RETURN)
          popStackFrame()
          resultSet.add(retVal)
        }
        Value(resultSet)

      case _ =>
        Value(99)
    }
  }

  /**
   * Executes a specified command.
   *
   * @param command The command to execute
   */
  private def execute(command: Command): Unit = {
    command match {
      case Print(message) =>
        println(message)
      case CreateNewSet(name) =>
        val state = this.getState()
        val newState = state + (name -> Value(mutable.Set.empty[Value]))
        this.setState(newState)
      case Assign(variable: Any, exp) =>
        variable match {
          case Variable(name) =>
            val newState = this.getState() + (name -> evaluate(exp))
            this.setState(newState)
          case ScopeResolvedVariable(scopeName: String, varName: String) =>
            val sr = lookupScope(scopeName)
            val state = sr.getState()
            state(varName) = evaluate(exp)
          case This(fieldName: String, outerClassName) =>
            val sr = this.stack.head
            val currentObject = sr.thisVal

            if (outerClassName != "") {

              val outerObject = this.getOuterObject(currentObject, outerClassName)

              outerObject match {
                case Some(o) =>
                  o.setField(fieldName, evaluate(exp))
                  return
                case None =>
                  // failed to find outer class object
                  throw new Exception(s"Failed to find outer class object for outer class $outerClassName")
              }
            }

            if (isFieldAccessible(currentObject, fieldName)) {
              currentObject.setField(fieldName, evaluate(exp))
            } else {
              throw new Exception(s"Invalid permissions for field $fieldName")
            }
          case _ =>
            throw new Error("Assign() parameter type invalid")
        }
      case Insert(ident, expressionsSeq@_*) =>
        val name = ident
        val expressions = expressionsSeq.toList
        val lookedUpItem = this.lookup(name)
        lookedUpItem match {
          case x: Value => {
            val set: mutable.Set[Expression] = x.value.asInstanceOf[mutable.Set[Expression]]
            for (exp: Expression <- expressions) {
              set.add(evaluate(exp))
            }
            val newState = this.getState() + (name -> Value(set))
            this.setState(newState)
          }
          case ex: Expression => {
            println("xxxxxx")
          }
        }
      case Delete(ident, expressionsSeq@_*) =>
        val name = ident
        val expressions = expressionsSeq.toList
        val lookedUpItem = this.lookup(name)
        lookedUpItem match {
          case x: Value => {
            val set: mutable.Set[Expression] = x.value.asInstanceOf[mutable.Set[Expression]]
            for (exp: Expression <- expressions) {
              set.remove(evaluate(exp))
            }
            val newState = this.getState() + (name -> Value(set))
            this.setState(newState)
          }
          case ex: Expression => {
            println("xxxxxx")
          }
        }
      case DefineMacro(name, expression) =>
        val newState = this.getState() + (name -> expression)
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

        variable match {
          case v: dsl.Value =>
            println(v.value)
          case ex: Expression =>
            print(ex)
//          case set: mutable.HashSet[Value] =>
//            println("{" + set.map[Any](v => v.value).mkString(",") + "}")
          case x =>
            println("not matched")
            println(x)
        }
      case DefineClass(name, options@_*) =>
        this.processClassDef(command.asInstanceOf[DefineClass])
      case InvokeMethod(returnee: Variable, objectName: String, methodName: String, params@_*) =>
        val objectValue = lookup(objectName)
        objectValue match {
          case ob: Value =>
            assert(ob.value.isInstanceOf[dsl.Object])
            val object_ = ob.value.asInstanceOf[dsl.Object]

            val className = object_.getClassName
            val mdo = lookupMethod(className, methodName)

            if (mdo.isDefined) {
              val md = mdo.get
              val map = mutable.Map.empty[String, Expression]

              for (param <- params) {
                map.addOne((param.parameterName, evaluate(param.value))) // weird
              }

              this.pushStackFrame(name = s"method call of $methodName", map, thisVal = object_)
              // TODO if a method call has args, we need to shove them into the stack frame before
              // running the method!
              for (c <- md.commands) {
                this.execute(c)
              }
              this.popStackFrame()

              // handling the return value if any
              val sr: ScopeRecord = this.stack.head
              if (sr.hasBinding(RETURN)) {
                val retValue: Expression = sr.getState()(RETURN)
                this.stack.head.deleteBinding(RETURN)

                // _ indicates that we don't care about the return value
                if (returnee.name != "_") {
                  execute(Assign(returnee, retValue))
                }
              }
            } else {
              throw new Exception(s"Method $methodName not found on object of class $className")
            }
          case _ =>
            throw new Exception("cannot resolve the value to an object to invoke method")
        }
      case Return(exp: Expression) =>
        this.stack(1).setBinding(Constants.RETURN, evaluate(exp))
      case PrintStack() =>
        for (sr <- this.stack) {
          println(sr)
        }
      case DefineInterface(interfaceName, options@_*) =>
        this.processInterfaceDef(command.asInstanceOf[DefineInterface])
      //        Util.runChecks(this.classTable, this.interfaceTable)

      case If(expression: Expression, commands@_*) =>
        val evaluatedExp = evaluate(expression)
        evaluatedExp match {
          case v: Value =>
            if (Util.isTruthy(v)) {
              runCommands(commands.toList)
            }
          case _ =>
            throw new Exception("Cannot evaluate if statement because the conditional expression cannot to reduced to a value")
        }
      case IfElse(expression: Expression, ifStatements: List[Command], elseStatements: List[Command]) =>
        val evaluatedExp = evaluate(expression)
        evaluatedExp match {
          case v: Value =>
            if (Util.isTruthy(v)) {
              runCommands (ifStatements)
            } else {
              runCommands (elseStatements)
            }
          case _ =>
            throw new Exception("Cannot evaluate if statement because the conditional expression cannot to reduced to a value")
        }
      case ExceptionClassDef(className: String) =>
        this.exceptionClassTable(className) = className

      case Try(commands: List[Command], catchBlocks: List[CatchBlock], finallyBlock) => {
        try {
          runCommands(commands)
        } catch {
          case e: InternalException => {
            if (!runCatchBlocks(e, catchBlocks)) { // no matching catch block is found
              this.execute(Throw(e.className, e.reason)) // re-throw the exception
            }

          }
        } finally {
          // always execute the finally block
          this.execute(finallyBlock)
        }
      }

      case Throw(className: String, reason) =>
        if (this.exceptionClassTable.contains(className)) {
          throw InternalException(className, reason = reason)
        } else {
          Util.assertp(false, s"Exception class $className is not defined")
        }

      case CatchBlock(className: String, commands@_*) =>
        this.runCommands(commands.toList)

      case FinallyBlock(commands@_*) =>
        this.runCommands(commands.toList)
      case Assert(v: Boolean) =>
        assert(v)
    }
  }

  private def runCatchBlocks(e: InternalException, catchBlocks: List[CatchBlock]): Boolean = {
    // loop through the catch blocks and look for a matching catch block
    // if one is found, then execute it and return
    for (catchBlock: CatchBlock <- catchBlocks) {
      if (matchesCatchBlock(catchBlock, e.className)) {
        runCommands(catchBlock.commands.toList)
        return true
      }
    }
    false
  }

  private def runCommands(commands: List[Command]) = {
    for (c: Command <- commands) {
      this.execute(c)
    }
  }

  private def matchesCatchBlock(cb: CatchBlock, className: String): Boolean = (cb.className == ANY) || (cb.className == className)

  @tailrec
  private def lookupMethod(className: String, methodName: String): Option[MethodDefinition] = {
    val cd = getClassDef(className)

    if (cd.hasMethod(methodName)) {
      Some(cd.getMethodDefinition(methodName)) // if the method is found on the current class
    } else if (cd.hasParentClass()) {
      lookupMethod(cd.getParentClassName(), methodName) // check the parent classes
    } else {
      None
    }
  }

  /**
   * Checks if a particular value is present in a given set.
   *
   * @param setName The set name to check
   * @param value   The value to check
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
    A_value match {
      case v: Value  =>
        val set: mutable.Set[Value] = v.value.asInstanceOf[mutable.Set[Value]]
        set.contains(value)
      case _ =>
        throw new Exception("cannot check contains because the expression could not be reduced to a set")
    }
  }

  def CheckVariable(name: String, value: Value): Boolean = {
    assert(this.stack.size == 1)
    val currentValue = lookup(name)
    currentValue == value
  }

//  private def _getClassDef(className: String): ClassDefinition = {
//    val classDefValue = lookup(className)
//    assert(classDefValue.value.isInstanceOf[ClassDefinition])
//    val classDef = classDefValue.value.asInstanceOf[ClassDefinition]
//    classDef
//  }

  private def getClassDef(className: String): ClassDefinition = {
    Util.assertp(this.classTable.contains(className), s"class $className is not defined")
    this.classTable(className)
  }

  private def getClassDefOption(className: String): Option[ClassDefinition] = {
    if (this.classTable.contains(className)) {
      return Some(classTable(className))
    }
    None
  }

  private def getInterfaceFields(className: String): List[Field_] = {
    val cd = this.classTable(className)
    val result = mutable.ListBuffer.empty[dsl.Field_]
    for (iName <- cd.getImplementedInterfaces) {
      var ci = iName
      while (ci != null) {
        val interfaceDef = interfaceTable(ci)
        for ((fieldName, fieldValue) <- interfaceDef.getFields) {
          val fi = new Field_(fieldName, fieldValue, immutable.Map(Constants.ACCESS_MODIFIER -> AccessModifiers.PUBLIC, Constants.FINAL -> true))
          result.addOne(fi)
        }
        ci = if (interfaceDef.hasParentInterface()) interfaceDef.getParentInterface() else null
      }
    }
    result.toList
  }

  private def createObject(className: String, outerClassObjectName: String): dsl.Object = {
    val classDef = getClassDef(className)
    val parentClassName = if (classDef.hasParentClass()) classDef.getParentClassName() else null

    if (classDef.isAbstract()) {
      throw new Exception(s"class $className is abstract; cannot be instantiated")
    }

    if (parentClassName != null) {
      val parentClassDef = getClassDef(parentClassName)
      val o = new dsl.Object(className,
        getInterfaceFields(className) ++ classDef.getFieldInfo() ++ parentClassDef.getFieldInfo(): _*)
      // invoking all the constructors to initialize the object
      invokeAllConstructors(o, className)
      setOuterObject(o, outerClassObjectName)
      o
    } else {
      val o = new dsl.Object(className, getInterfaceFields(className) ++ classDef.getFieldInfo(): _*)
      setOuterObject(o, outerClassObjectName)
      invokeAllConstructors(o, className)
      o
    }
  }

  private def setOuterObject(o: dsl.Object, outerClassObjectName: String): Unit = {
    if (outerClassObjectName != "") {
      val x = getState()(outerClassObjectName)
      x match {
        case c: Value =>
          o.setOuterObject (c.value.asInstanceOf[dsl.Object] )
        case _ =>
          println("Failed")
      }
    }
  }

  private def getClassNameOfField(fieldName: String, className: String): Option[String] = {
    var cdo: Option[ClassDefinition] = getClassDefOption(className)
    while (true) { // look up the inheritance chain searching for the field
      cdo match {
        case Some(cd) =>
          if (cd.hasField(fieldName)) {
            return Some(cd.getName)
          }
          cdo = getClassDefOption(cd.getParentClassName())
        case None => // reached the end of the inheritance chain
          return None
      }
    }
    None
  }

  private def isFieldAccessible(currentObject: dsl.Object, fieldName: String): Boolean = {
    val cd = getClassDef(currentObject.getClassName)
    val objectClassName = cd.getName

    if (hasField(cd, fieldName)) {
      if (currentObject.hasField(fieldName) && currentObject.isFieldFinal(fieldName)) {
        return true
      }
      val classNameOption = getClassNameOfField(fieldName, currentObject.getClassName)
      classNameOption match {
        case Some(fieldClassName) =>
          val fieldClassDef = getClassDef(fieldClassName)
          if (fieldClassDef.getFieldAccessModifier(fieldName) == AccessModifiers.PUBLIC) {
            return true
          }
          if (fieldClassName == objectClassName) { // its on the class itself so good to go
            return true
          } else {
            return fieldClassDef.getFieldAccessModifier(fieldName) == AccessModifiers.PROTECTED
          }
        case None =>
          throw new Exception("field is not present or inherited on this class")
      }
    }
    throw new Exception(s"Field $fieldName not present on class ${cd.getName}")
  }

  //  private def isParentField(cd: ClassDefinition, fieldName: String): Boolean = {
  //    if (cd.hasField(fieldName)) {
  //      return false
  //    }
  //  }

  @tailrec
  private def hasField(cd: ClassDefinition, fieldName: String): Boolean = {
    if (cd.hasParentClass()) {
      cd.hasField(fieldName) || hasField(getClassDef(cd.getParentClassName()), fieldName)
    } else {
      hasInterfaceField(cd, fieldName) || cd.hasField(fieldName)
    }
  }

  private def hasInterfaceField(cd: ClassDefinition, fieldName: String): Boolean = {
    for (iName <- cd.getImplementedInterfaces) {
      var curInterfaceName = iName
      while (curInterfaceName != null) {
        if (interfaceTable(curInterfaceName).getFields.contains(fieldName)) {
          return true
        }
        curInterfaceName = if (interfaceTable(curInterfaceName).hasParentInterface()) interfaceTable(curInterfaceName).getParentInterface() else null
      }
    }
    false
  }

  private def hasParentClass(className: String): Boolean = {
    assert(classTable.contains(className))
    val classDef = getClassDef(className)
    classDef.hasParentClass()
  }

  private def invokeAllConstructors(o: dsl.Object, className: String): Unit = {
    val classDef = getClassDef(className)
    if (classDef.hasParentClass()) {
      invokeAllConstructors(o, classDef.getParentClassName())
    }
    invokeConstructor(o, className)
  }

  private def invokeConstructor(o: dsl.Object, className: String): Unit = {
    val classDef = getClassDef(className)
    pushStackFrame(s"Constructor call of ${classDef.getName}", thisVal = o)
    //    println(s"Constructor call of $className")
    // this is dangerous and may have deadly side effects :'(
    for (c <- classDef.getConstructor) {
      execute(c)
    }
    popStackFrame()
  }

  private def processClassDef(dc: dsl.DefineClass, outerClassName: String = null): Unit = {
    val options = dc.options
    val name = dc.className

    // check for multiple inheritance
    if (options.count(option => option.isInstanceOf[Extends]) > 1) {
      throw new Exception(s"Two or more extends clauses found. Multiple inheritance is not allowed.")
    }

    // need to check for class already defined
    if (classTable.contains(name)) {
      throw new Exception(s"class $name is already defined")
    }

    val classDefinition = new ClassDefinition(name, options: _*)

    if (outerClassName != null) {
      classDefinition.setOuterClass(outerClassName)
    }

    this.classTable(name) = classDefinition // adding here is a little dangerous

    // check for single inheritance
    for (o: ClassDefinitionOption <- options) {
      o match {
        case Extends(parentClassName) =>
          // check whether the parent class actually exists
          if (!this.classTable.contains(parentClassName)) {
            //            throw new Exception(s"Parent class of the extends clause $parentClassName not defined")
          }
          // set the name of the parent class given in the extends clause
          classDefinition.setParentClass(parentClassName)
        case NestedClass(nestedClassName, nestedClassOptions@_*) =>
          processClassDef(dsl.DefineClass(nestedClassName, nestedClassOptions *), outerClassName = name)
        case _ =>
      }
    }
    //        val newState = this.getState() + (name -> Value(classDefinition))
    //        this.setState(newState)
  }

  private def processInterfaceDef(di: dsl.DefineInterface): Unit = {
    val options = di.options
    val name = di.interfaceName

    // check for multiple inheritance
    if (options.count(option => option.isInstanceOf[ExtendsInterface]) > 1) {
      throw new Exception(s"Two or more extends clauses found. Multiple inheritance is not allowed.")
    }

    // need to check for class already defined
    if (this.interfaceTable.contains(name)) {
      throw new Exception(s"interface $name is already defined")
    }

    val interfaceDefinition = new InterfaceDefinition(name, options: _*)

    //    if (outerClassName != null) {
    //      classDefinition.setOuterClass(outerClassName)
    //    }

    this.interfaceTable(name) = interfaceDefinition // adding here is a little dangerous

    // check for single inheritance
    for (o: InterfaceDefinitionOption <- options) {
      o match {
        case ExtendsInterface(parentInterfaceName) =>
          // check whether the parent interface actually exists
          if (!this.interfaceTable.contains(parentInterfaceName)) {
            //            throw new Exception(s"Parent interface of the extends clause $parentInterfaceName not defined")
          }
          // set the name of the parent interface given in the extends clause
          interfaceDefinition.setParentInterface(parentInterfaceName)

        case _ =>
      }
    }
  }


  private def getOuterObject(currentObject: dsl.Object, outerClassName: String): Option[dsl.Object] = {
    val outerObject = getOuterObject_(currentObject, outerClassName)
    if (outerObject == null) {
      return None
    }
    Some(outerObject)
  }

  @tailrec
  private def getOuterObject_(currentObject: dsl.Object, outerClassName: String): dsl.Object = {
    if (currentObject.getClassName == outerClassName) {
      return currentObject
    }
    if (!currentObject.hasOuterObject()) {
      return null
    }
    getOuterObject_(currentObject.getOuterObject(), outerClassName)
  }

  private def optimize(expression: Expression): Expression = {
    expression match {
      case Add(e1, e2) =>
        e1 match {
          case value: Value if value.value == 0 =>
            e2
          case _ => e2 match {
            case value: Value if value.value == 0 =>
              e1
            case _ =>
              Add(e1, e2)
          }
        }
      case Intersection(e1, e2) =>
        val b1 = e1.isInstanceOf[Value]
        val b2 = b1 && e1.asInstanceOf[Value].value.isInstanceOf[mutable.Set[Value]]
        val b3 = b2 && e1.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]].isEmpty
        if (b1 && b2 && b3) {
          return Value(mutable.Set.empty[Expression])
        }
        val c1 = e2.isInstanceOf[Value]
        val c2 = c1 && e2.asInstanceOf[Value].value.isInstanceOf[mutable.Set[Value]]
        val c3 = c2 && e2.asInstanceOf[Value].value.asInstanceOf[mutable.Set[Value]].isEmpty
        if (c1 && c2 && c3) {
          return Value(mutable.Set.empty[Expression])
        }
        if (e1.isInstanceOf[Variable] && e2.isInstanceOf[Variable]) {
          val name1 = e1.asInstanceOf[Variable].name
          val name2 = e1.asInstanceOf[Variable].name
          if (name1 == name2) {
            return dsl.Variable(name1)
          }
        }
        Intersection(e1, e2)
      case Union(e1, e2) =>
        if (e1.isInstanceOf[Variable] && e2.isInstanceOf[Variable]) {
          val name1 = e1.asInstanceOf[Variable].name
          val name2 = e1.asInstanceOf[Variable].name
          if (name1 == name2) {
            return dsl.Variable(name1)
          }
        }
        Union(e1, e2)
      case Difference(e1, e2) =>
        (e1, e2) match {
          case (v: Variable, e2: Value) =>
            e2.value match {
              case s: mutable.Set[Expression] => if (s.isEmpty) e1  else Difference(e1, e2)
              case _ => Difference(e1, e2)
            }
          case _ => Difference(e1, e2)
        }
      case _ => expression
    }
  }
}


final case class InternalException(className: String, reason: String = "") extends Exception