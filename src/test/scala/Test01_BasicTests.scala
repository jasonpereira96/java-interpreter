import dsl._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
class Test01_BasicTests extends AnyFlatSpec with Matchers {
  behavior of "my first language for set theory operations"
  it should "create a set and insert objects into it" in {

    val evaluator = new Evaluator()
    evaluator.run(createBasicSets():_*)

    assert(evaluator.Check("A", Value(4)))
    assert(evaluator.Check("A", Value(5)))
    assert(evaluator.Check("A", Value("Ford")))
    assert(evaluator.Check("A", Value("Honda")))
    assert(evaluator.Check("B", Value(100)))
    assert(evaluator.Check("B", Value(200)))
    assert(evaluator.Check("B", Value("BMW")))
    assert(evaluator.Check("B", Value("Ferrari")))
  }

  it should "create a set and insert objects into it using a dsl.Program" in {
    val program = new Program(createBasicSets())
    val evaluator = new Evaluator()
    evaluator.runProgram(program)

    assert(evaluator.Check("A", Value(4)))
    assert(evaluator.Check("A", Value(5)))
    assert(evaluator.Check("A", Value("Ford")))
    assert(evaluator.Check("A", Value("Honda")))
    assert(evaluator.Check("B", Value(100)))
    assert(evaluator.Check("B", Value(200)))
    assert(evaluator.Check("B", Value("BMW")))
    assert(evaluator.Check("B", Value("Ferrari")))
  }

  def createBasicSets(): List[Command] = {
    val s1 = CreateNewSet("A")
    val s2 = Insert("A", Value(4), Value(5))
    val s3 = Insert("A", Value("Ford"), Value("Honda"))
    val s4 = CreateNewSet("B")
    val s5 = Insert("B", Value(100), Value(200))
    val s6 = Insert("B", Value("BMW"), Value("Ferrari"))
    List(s1, s2, s3, s4, s5, s6)
  }
}
