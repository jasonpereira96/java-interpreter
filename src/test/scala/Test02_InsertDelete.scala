import dsl._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.collection.mutable.ListBuffer

class Test02_InsertDelete extends AnyFlatSpec with Matchers {
  behavior of "testing insert and delete operations"
  it should "create a set and insert objects into it" in {
    val s1 = CreateNewSet("A")
    val s2 = Insert("A", Value(4), Value(5))
    val s3 = Insert("A", Value("Ford"), Value("Honda"))

    val evaluator = new Evaluator()
    evaluator.run(s1, s2, s3)

    assert(evaluator.Check("A", Value(4)))
    assert(evaluator.Check("A", Value(5)))
    assert(evaluator.Check("A", Value("Ford")))
    assert(evaluator.Check("A", Value("Honda")))
    assert(!evaluator.Check("A", Value("Toyota")))
  }

  it should "create a set and insert and delete objects from it" in {
    val s1 = CreateNewSet("A")
    val s2 = Insert("A", Value(4), Value(5))
    val s3 = Insert("A", Value("Ford"), Value("Honda"))
    val s4 = Delete("A", Value("Ford"))

    val evaluator = new Evaluator()
    evaluator.run(s1, s2, s3, s4)

    assert(evaluator.Check("A", Value(4)))
    assert(evaluator.Check("A", Value(5)))
    assert(!evaluator.Check("A", Value("Ford")))
    assert(evaluator.Check("A", Value("Honda")))
    assert(!evaluator.Check("A", Value("Toyota")))
  }

  it should "create a set and insert and delete many objects from it" in {
    val s1 = CreateNewSet("A")

    val inserts = ListBuffer[Command]()
    val deletes = ListBuffer[Command]()

    for (i <- 1 to 100) {
      inserts.addOne(Insert("A", Value(i)))
    }

    for (i <- 1 to 100) {
      if (i % 2 == 0) {
        deletes.addOne(Delete("A", Value(i)))
      }
    }

    val p = new Program(List(s1) ++ inserts.toList ++ deletes.toList)
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    for (i <- 1 to 100) {
      if (i % 2 == 0) {
        assert(!evaluator.Check("A", Value(i)))
      } else {
        assert(evaluator.Check("A", Value(i)))
      }
    }

    assert(!evaluator.Check("A", Value(101)))

  }
}
