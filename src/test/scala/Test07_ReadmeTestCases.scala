import dsl._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Test07_ReadmeTestCases extends AnyFlatSpec with Matchers {
  behavior of "testing union, intersection and difference"

  it should "compute the difference of 2 sets" in {

    val p = new Program(List(
      CreateNewSet("A"),
      CreateNewSet("B"),
      Insert("A", Value(1), Value(2), Value(3), Value(4)),
      Insert("B", Value(3), Value(4), Value(5)),
      Assign(Variable("A U B"), Union(Variable("A"), Variable("B"))),
      Assign(Variable("A - B"), Difference(Variable("A"), Variable("B"))),
      Assign(Variable("A intersect B"), Intersection(Variable("A"), Variable("B"))),
      Assign(Variable("A symdiff B"), SymmetricDifference(Variable("A"), Variable("B"))),
      Assign(Variable("A x B"), CartesianProduct(Variable("A"), Variable("B")))
    ))
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    assert(evaluator.Check("A - B", Value(1)))
    assert(evaluator.Check("A - B", Value(2)))
    assert(!evaluator.Check("A - B", Value(5)))

    assert(evaluator.Check("A U B", Value(1)))
    assert(evaluator.Check("A U B", Value(2)))
    assert(evaluator.Check("A U B", Value(3)))
    assert(evaluator.Check("A U B", Value(4)))
    assert(evaluator.Check("A U B", Value(5)))

    assert(evaluator.Check("A intersect B", Value(3)))
    assert(evaluator.Check("A intersect B", Value(4)))

    assert(evaluator.Check("A symdiff B", Value(1)))
    assert(evaluator.Check("A symdiff B", Value(2)))
    assert(evaluator.Check("A symdiff B", Value(5)))

    assert(evaluator.Check("A x B", Value((1, 3))))
    assert(evaluator.Check("A x B", Value((1, 4))))
    assert(evaluator.Check("A x B", Value((1, 5))))

    assert(evaluator.Check("A x B", Value((2, 3))))
    assert(evaluator.Check("A x B", Value((2, 4))))
    assert(evaluator.Check("A x B", Value((2, 5))))

    assert(evaluator.Check("A x B", Value((3, 3))))
    assert(evaluator.Check("A x B", Value((3, 4))))
    assert(evaluator.Check("A x B", Value((3, 5))))

    assert(evaluator.Check("A x B", Value((4, 3))))
    assert(evaluator.Check("A x B", Value((4, 4))))
    assert(evaluator.Check("A x B", Value((4, 5))))

  }
}

