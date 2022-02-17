// import the dsl package
import dsl._

object Main {
  def main(args: Array[String]): Unit = {

    // Create an object of class dsl.Evaluator - this is mandatory
    val evaluator = new Evaluator()

    // Call evaluator.run and pass the commands to run as args
    /*evaluator.run(
      Assign(Variable("i"), Value(4)), // i = 4
      CreateNewSet("A"), // Creating a new set A
      Insert("A", Value(4), Value(5)), // A.insert(4, 5)
      CreateNewSet("X"), // Creating a new set X
      CreateNewSet("Y"), // Creating a new set Y
      Insert("X", Value(10), Value(20), Value(30)), // X.insert(10, 20, 30)
      Insert("Y", Value(30), Value(40)), // Y.insert(30, 40)
      Assign(Variable("X U Y"), Union(Variable("X"), Variable("Y"))), // X U Y = the union of X and Y
      Assign(Variable("X - Y"), Difference(Variable("X"), Variable("Y"))), // X - y = the difference between X and Y
      Assign(Variable("X intersect Y"), Intersection(Variable("X"), Variable("Y"))), // X intersect Y = the intersection of X and Y
      Assign(Variable("X x Y"), CartesianProduct(Variable("X"), Variable("Y"))), // X x Y = the cartesian product of X and Y
      Assign(Variable("is 300 in Y?"), CheckIfContains(Variable("Y"), Value(300))), // Check whether Y contains 300
      Display("i is", "i"), // Display the value of i
      Display("Contents of X", "X"), // Display the contents of X
      Display("Contents of Y", "Y"),
      Display("Contents of X U Y", "X U Y"),
      Display("Contents of X - Y", "X - Y"),
      Display("Contents of X intersect Y", "X intersect Y"),
      Display("Contents of X x Y", "X x Y"),
    )*/

    // After evaluator.run() has finished, you can use Check() and CheckVariable() to verify the results
    /*if (evaluator.Check("Y", Value(30))) {
      println("Y contains 300")
    }*/

    val e = new Evaluator()

    /*e.run(
      Assign(Variable("x"), Value("GLOBAL")),
      NamedScope("A",
        Assign(Variable("x"), Value(1)),
        NamedScope("B",
          Assign(Variable("x"), Value(2)),
          Assign(Variable("y"), Value(2)),
          Assign(ScopeResolvedVariable("A", "x"), Value(10)),

        )
      )
    )*/

    val finalState = e.run(
      Assign(Variable("x"), Value(2)),

      DefineClass("Point", Constructor(
        Display("creating a point", "x"),
      ), Field("x"), Field("y")),
      Assign(Variable("p1"), NewObject("Point")),

      DefineClass("3DPoint", Constructor(
        Display("creating a 3d point", "x"),
      ),
        Field("z"), Extends("Point")
      ),
      Assign(Variable("p2"), NewObject("3DPoint")),
      PrintStack()
    )

    println(finalState)
    println("done")


  }
}
