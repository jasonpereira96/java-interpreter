package dsl

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Util {
  def assertp(assertion: Boolean, message: String = "") = {
    try {
      assert(assertion, message)
    } catch {
      case e: Throwable => {
        println(message)
        throw e
      }
    }
  }

  private def hasCycle(graph: Map[String, ListBuffer[String]]): Boolean = {
    val N = graph.keySet.size
    var prereq = ListBuffer.empty[Array[Int]]
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
    return !canFinish(N, prereq.toArray)
  }

  private def checkForCycles(classTable: mutable.Map[String, ClassDefinition], interfaceTable: mutable.Map[String, InterfaceDefinition]) = {
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
      print("Found a cycle!!!!!!!!!!!")
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
      print("Found a cycle!!!!!!!!!!!")
    }
  }
  private def canFinish(numCourses: Int, prerequisites: Array[Array[Int]]): Boolean = {
    val graph = mutable.Map.empty[Int, mutable.ListBuffer[Int]]
    val visited = mutable.Map.empty[Int, String]
    val WHITE = "WHITE"
    val BLACK = "BLACK"
    val GREY = "GREY"

    for ( i <- 0 to numCourses -1) {
      graph(i) = ListBuffer.empty[Int]
      visited(i) = WHITE
    }
    for (prereq <- prerequisites) {
      val from = prereq(0)
      val to = prereq(1)
      graph(from).addOne(to)
    }

    var hasCycle = false


    for (id <- 0 to numCourses - 1) {
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
    return !hasCycle
  }



  def runChecks(classTable: mutable.Map[String, ClassDefinition], interfaceTable: mutable.Map[String, InterfaceDefinition]) = {
    checkDefs(classTable, interfaceTable)
    checkAbstractClasses(classTable)
    checkImplements(classTable, interfaceTable)
    checkForCycles(classTable, interfaceTable)
  }
  private def checkImplements(classTable: mutable.Map[String, ClassDefinition], interfaceTable: mutable.Map[String, InterfaceDefinition]) = {
    for ((className, cd) <- classTable) {
      val allMethods = mutable.Set.empty[String]
      var currentClassName = className

      // gather all the methods into one set
      while (currentClassName != null) {
        val cd = classTable(className)
        allMethods.addAll(cd.getMethods().keys)
        currentClassName = if (cd.hasParentClass()) cd.getParentClassName() else null
      }

      // for every implemented interface
      for (iName <- cd.getImplementedInterfaces) {
        // for every method of that interface
        for ((mName, md) <- interfaceTable(iName).getMethods) {
          // check if that method is present somewhere along the inheritance chain
          if (!allMethods.contains(mName)) {
            assertp(false, s"class ${className} must implement method ${mName}")
          }
        }
      }
    }
  }
  private def checkAbstractClasses(classTable: mutable.Map[String, ClassDefinition]) = {
    for ((className, cd) <- classTable) {
      val abstractMethodsCount = cd.getMethods().values.filter((v: MethodDefinition) => v.isAbstract).size
//      println(cd.getName)
//      println(abstractMethodsCount)
      if (cd.isConcrete()) {
         assertp(abstractMethodsCount == 0, s"class $className is not abstract. It cannot have any abstract methods")
      }
      // if the class is concrete and its immediate parent is abstract, then it needs to immplement all the
      // non-implemented abstract members
      if (cd.isConcrete() && cd.hasParentClass() && classTable(cd.getParentClassName()).isAbstract()) {
        doAbstractCheck(classTable, className)
      }
    }
  }

  private def doAbstractCheck(classTable: mutable.Map[String, ClassDefinition], className: String) = {
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
    while (!stack.isEmpty) {
      val className = stack.pop()
      // add all abstract methods
      set.addAll(classTable(className).getMethods().values.filter(md => md.isAbstract).map(md => md.name))
      // remove all concrete methods
      classTable(className).getMethods().values.filter(md => md.isConcrete()).map(md => md.name).foreach(cn => {
        set.remove(cn)
      })
    }
    assertp(set.size == 0, "Some abstract method is not implemented")
  }

  private def checkDefs(classTable: mutable.Map[String, ClassDefinition], interfaceTable: mutable.Map[String, InterfaceDefinition]) = {
    for ((className, cd) <- classTable) {
      if (cd.hasParentClass()) {
        if(!classTable.contains(cd.getParentClassName())) {
          val message = s"class ${cd.getParentClassName()} present in the extends clause of $className is not defined"
          println(message)
          assert(false, message)
        }
      }
    }
    for ((className, cd) <- interfaceTable) {
      if (cd.hasParentInterface()) {
        if(!classTable.contains(cd.getParentInterface())) {
          val message = s"interface ${cd.getParentInterface()} present in the extends clause of $className is not defined"
          println(message)
          assert(false, message)
        }
      }
    }
  }

  @main
  private def testRunChecks() = {
    val ct = mutable.Map.empty[String, ClassDefinition]
    val it = mutable.Map.empty[String, InterfaceDefinition]

    ct("A") = new ClassDefinition("A", Extends("B"))
    ct("B") = new ClassDefinition("B", Extends("A"))
//    ct("A").setParentClass("B")
    ct("B").setParentClass("A")

    runChecks(ct, it)
  }
}
