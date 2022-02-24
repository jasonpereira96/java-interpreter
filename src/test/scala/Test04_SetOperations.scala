import dsl._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Test04_SetOperations extends AnyFlatSpec with Matchers {
  behavior of "testing Symmetric difference and Cartesian Product"
  it should "compute the Symmetric difference of 2 sets" in {

    val p = new Program(List(
      CreateNewSet("A"),
      CreateNewSet("B"),
      Insert("A", Value(1), Value(2), Value(3)),
      Insert("B", Value(2), Value(3), Value(4)),
      Assign(Variable("Sym Diff of A and B"), SymmetricDifference(Variable("A"), Variable("B")))
    ))
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    assert(evaluator.Check("Sym Diff of A and B", Value(1)))
    assert(evaluator.Check("Sym Diff of A and B", Value(4)))
  }

  it should "compute the cartesian product of 2 sets" in {

    val p = new Program(List(
      CreateNewSet("A"),
      CreateNewSet("B"),
      Insert("A", Value("A"), Value("B"), Value("C")),
      Insert("B", Value(1), Value(2), Value(3)),
      Assign(Variable("CP"), CartesianProduct(Variable("A"), Variable("B")))
    ))
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    assert(evaluator.Check("CP", Value(("A", 1))))
    assert(evaluator.Check("CP", Value(("A", 2))))
    assert(evaluator.Check("CP", Value(("A", 3))))
    assert(evaluator.Check("CP", Value(("B", 1))))
    assert(evaluator.Check("CP", Value(("B", 2))))
    assert(evaluator.Check("CP", Value(("B", 3))))
    assert(evaluator.Check("CP", Value(("C", 1))))
    assert(evaluator.Check("CP", Value(("C", 2))))
    assert(evaluator.Check("CP", Value(("C", 3))))
  }
}

