import dsl._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
class Test06_Macros extends AnyFlatSpec with Matchers {
  behavior of "testing the functionality of macros"

  /*
    A = {1, 2, 3} // creating a new set
    B = {4, 5} // creating a new set
    macro m = dsl.Union("A", "B") // defining a macro
    C = {6} // creating a new set
    D = dsl.Union(ComputeMacro("m"), "C") // substituting the macro
    // D is now {1, 2, 3, 4, 5, 6}
  */
  it should "create a macro and use it correctly" in {
    val s1 = CreateNewSet("A")
    val s2 = Insert("A", Value(1), Value(2), Value(3))
    val s3 = CreateNewSet("B")
    val s4 = Insert("A", Value(4), Value(5))
    val s5 = DefineMacro("m", Union(Variable("A"), Variable("B")))
    val s6 = CreateNewSet("C")
    val s7 = Insert("C", Value(6))
    val s8 = Assign(Variable("D"), Union(Variable("m"), Variable("C")))
    val p = new Program(List(s1, s2, s3, s4, s5, s6, s7, s8))
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    assert(evaluator.Check("D", Value(1)))
    assert(evaluator.Check("D", Value(2)))
    assert(evaluator.Check("D", Value(3)))
    assert(evaluator.Check("D", Value(4)))
    assert(evaluator.Check("D", Value(5)))
    assert(evaluator.Check("D", Value(6)))
  }
}

