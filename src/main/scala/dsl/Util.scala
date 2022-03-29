package dsl

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Util {
  def assertp(assertion: Boolean, message: String = ""): Unit = {
    try {
      assert(assertion, message)
    } catch {
      case e: Throwable =>
        println(message)
        throw e
    }
  }

  private def hasCycle(graph: Map[String, ListBuffer[String]]): Boolean = {
    val N = graph.keySet.size
    val prereq = ListBuffer.empty[Array[Int]]
    val idMapping = mutable.Map.empty[String, Integer]
    var  i = 0
    for ((className, list: mutable.ListBuffer[String]) <- graph) {
      idMapping(className) = i
       i = i + 1
    }
    for ((className, list: mutable.ListBuffer[String]) <- graph) {
      for (depName <- graph(className)) {
        prereq.addOne(Array(idMapping(className), idMapping(depName)))
      }
    }
//    println(prereq)
    !canFinish(N, prereq.toArray)
  }

  private def checkForCycles(classTable: mutable.Map[String, ClassDefinition], interfaceTable: mutable.Map[String, InterfaceDefinition]): Unit = {
    val adjacencyList = mutable.Map.empty[String, mutable.ListBuffer[String]]
    for ((className, cd) <- classTable) {
      adjacencyList(className) = mutable.ListBuffer.empty[String]
    }
    for ((className, cd) <- classTable) {
      if (cd.hasParentClass()) {
        adjacencyList(className).addOne(cd.getParentClassName())
      }
    }


    if (hasCycle(adjacencyList.toMap)) {
      assertp(false, "Cyclic inheritance between classes found")
    }

    val adjacencyList2 = mutable.Map.empty[String, mutable.ListBuffer[String]]
    for ((interfaceName, cd) <- interfaceTable) {
      adjacencyList2(interfaceName) = mutable.ListBuffer.empty[String]
    }
    for ((interfaceName, cd) <- interfaceTable) {
      if (cd.hasParentInterface()) {
        adjacencyList2(interfaceName).addOne(cd.getParentInterface())
      }
    }


    if (hasCycle(adjacencyList2.toMap)) {
      assertp(false, "Cyclic inheritance between interfaces found")
    }
  }
  private def canFinish(numCourses: Int, prerequisites: Array[Array[Int]]): Boolean = {
    val graph = mutable.Map.empty[Int, mutable.ListBuffer[Int]]
    val visited = mutable.Map.empty[Int, String]
    val WHITE = "WHITE"
    val BLACK = "BLACK"
    val GREY = "GREY"

    for ( i <- 0 until numCourses) {
      graph(i) = ListBuffer.empty[Int]
      visited(i) = WHITE
    }
    for (prereq <- prerequisites) {
      val from = prereq(0)
      val to = prereq(1)
      graph(from).addOne(to)
    }

    var hasCycle = false


    for (id <- 0 until numCourses) {
      if (visited(id) == WHITE) {
        dfs(id)
      }
    }
    def dfs(nodeId: Int): Unit = {
      if (hasCycle) {
        return
      }
      if (visited(nodeId) == GREY) {
        hasCycle = true
        return
      }
      if (visited(nodeId) == BLACK) {
        return
      }
      visited(nodeId) = GREY
      for (neighbourId <- graph(nodeId)) {
        dfs(neighbourId)
      }
      visited(nodeId) = BLACK
    }
    !hasCycle
  }



  def runChecks(classTable: mutable.Map[String, ClassDefinition], interfaceTable: mutable.Map[String, InterfaceDefinition]): Unit = {
    checkDefs(classTable, interfaceTable)
    checkAbstractClasses(classTable)
    checkForCycles(classTable, interfaceTable)
    checkImplements(classTable, interfaceTable)
  }
  private def checkImplements(classTable: mutable.Map[String, ClassDefinition], interfaceTable: mutable.Map[String, InterfaceDefinition]): Unit = {
    for ((className, cd) <- classTable) {
      val allMethods = mutable.Set.empty[String]
      var currentClassName = className

      // gather all the methods into one set
      while (currentClassName != null) {
        val cd = classTable(currentClassName)
        allMethods.addAll(cd.getMethods().keys)
        currentClassName = if (cd.hasParentClass()) cd.getParentClassName() else null
      }

      // for every implemented interface
      for (iName <- cd.getImplementedInterfaces) {
        // for every method of that interface
        for ((mName, md) <- interfaceTable(iName).getMethods) {
          // check if that method is present somewhere along the inheritance chain
          if (!allMethods.contains(mName)) {
            assertp(false, s"class $className must implement method $mName")
          }
        }
      }
    }
  }
  private def checkAbstractClasses(classTable: mutable.Map[String, ClassDefinition]): Unit = {
    for ((className, cd) <- classTable) {
      val abstractMethodsCount = cd.getMethods().values.count((v: MethodDefinition) => v.isAbstract)
//      println(cd.getName)
//      println(abstractMethodsCount)
      if (cd.isConcrete()) {
         assertp(abstractMethodsCount == 0, s"class $className is not abstract. It cannot have any abstract methods")
      }
      if (cd.isAbstract()) {
        assertp(abstractMethodsCount > 0, s"class $className is abstract. It should have at least one abstract method")
      }
      // if the class is concrete and its immediate parent is abstract, then it needs to implement all the
      // non-implemented abstract members
      if (cd.isConcrete() && cd.hasParentClass() && classTable(cd.getParentClassName()).isAbstract()) {
        doAbstractCheck(classTable, className)
      }
    }
  }

  private def doAbstractCheck(classTable: mutable.Map[String, ClassDefinition], className: String): Unit = {
    val stack = mutable.Stack.empty[String]
    val set = mutable.Set.empty[String]

    // build a stack of classes in extends order because we have to process the classes in reverse order
    var currentClassName = className
    while (currentClassName != null) {
      stack.push(currentClassName)
      if (classTable(currentClassName).hasParentClass()) {
        currentClassName = classTable(currentClassName).getParentClassName()
      } else {
        currentClassName = null
      }
    }
//    println(stack)
    // now check if all the abstract methods are implemented
    while (stack.nonEmpty) {
      val className = stack.pop()
      // add all abstract methods
      set.addAll(classTable(className).getMethods().values.filter(md => md.isAbstract).map(md => md.name))
      // remove all concrete methods
      classTable(className).getMethods().values.filter(md => md.isConcrete()).map(md => md.name).foreach(cn => {
        set.remove(cn)
      })
    }
    assertp(set.isEmpty, "Some abstract method is not implemented")
  }

  private def checkDefs(classTable: mutable.Map[String, ClassDefinition], interfaceTable: mutable.Map[String, InterfaceDefinition]): Unit = {
    for ((className, cd) <- classTable) {
      if (cd.hasParentClass()) {
        if(!classTable.contains(cd.getParentClassName())) {
          val message = s"class ${cd.getParentClassName()} present in the extends clause of $className is not defined"
          println(message)
          assert(false, message)
        }
      }
    }
    for ((interfaceName, id) <- interfaceTable) {
      if (id.hasParentInterface()) {
        if(!interfaceTable.contains(id.getParentInterface())) {
          val message = s"interface ${id.getParentInterface()} present in the extends clause of $interfaceName is not defined"
          println(message)
          assert(false, message)
        }
      }
    }
  }
  
  def isTruthy(value: Value): Boolean = {
    value.value match {
      case v: Boolean => v
      case v: String => !v.isEmpty
      case v: Integer => v != 0
      case _ => true
    }
  }

  @main
  private def testRunChecks(): Unit = {
    val ct = mutable.Map.empty[String, ClassDefinition]
    val it = mutable.Map.empty[String, InterfaceDefinition]

    ct("A") = new ClassDefinition("A", Extends("B"))
    ct("B") = new ClassDefinition("B", Extends("A"))
//    ct("A").setParentClass("B")
    ct("B").setParentClass("A")

    runChecks(ct, it)
  }
}
