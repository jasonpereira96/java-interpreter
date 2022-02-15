// import the dsl package
import dsl._

object Main {
  def main(args: Array[String]): Unit = {

    // Create an object of class dsl.Evaluator - this is mandatory
    val evaluator = new Evaluator()

    // Call evaluator.run and pass the commands to run as args
    evaluator.run(
      Assign("i", Value(4)), // i = 4
      CreateNewSet("A"), // Creating a new set A
      Insert("A", Value(4), Value(5)), // A.insert(4, 5)
      CreateNewSet("X"), // Creating a new set X
      CreateNewSet("Y"), // Creating a new set Y
      Insert("X", Value(10), Value(20), Value(30)), // X.insert(10, 20, 30)
      Insert("Y", Value(30), Value(40)), // Y.insert(30, 40)
      Assign("X U Y", Union(Variable("X"), Variable("Y"))), // X U Y = the union of X and Y
      Assign("X - Y", Difference(Variable("X"), Variable("Y"))), // X - y = the difference between X and Y
      Assign("X intersect Y", Intersection(Variable("X"), Variable("Y"))), // X intersect Y = the intersection of X and Y
      Assign("X x Y", CartesianProduct(Variable("X"), Variable("Y"))), // X x Y = the cartesian product of X and Y
      Assign("is 300 in Y?", CheckIfContains(Variable("Y"), Value(300))), // Check whether Y contains 300
      Display("i is", "i"), // Display the value of i
      Display("Contents of X", "X"), // Display the contents of X
      Display("Contents of Y", "Y"),
      Display("Contents of X U Y", "X U Y"),
      Display("Contents of X - Y", "X - Y"),
      Display("Contents of X intersect Y", "X intersect Y"),
      Display("Contents of X x Y", "X x Y"),
    )

    // After evaluator.run() has finished, you can use Check() and CheckVariable() to verify the results
    if (evaluator.Check("Y", Value(30))) {
      println("Y contains 300")
    }


    val cd = new ClassDefinition("ds")
  }
}
