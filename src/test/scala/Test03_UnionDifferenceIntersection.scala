import dsl._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.collection.mutable.ListBuffer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
class Test03_UnionDifferenceIntersection extends AnyFlatSpec with Matchers {
  behavior of "testing union, intersection and difference"
  it should "compute the union of 2 sets" in {

    val p = new Program(List(
      CreateNewSet("A"),
      CreateNewSet("B"),
      Insert("A", Value("red"), Value("orange"), Value("yellow"), Value("green")),
      Insert("B", Value("blue"), Value("indigo"), Value("violet")),
      Assign(Variable("A union B"), Union(Variable("A"), Variable("B")))
    ))
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    assert(evaluator.Check("A union B", Value("red")))
    assert(evaluator.Check("A union B", Value("orange")))
    assert(evaluator.Check("A union B", Value("yellow")))
    assert(evaluator.Check("A union B", Value("green")))
    assert(evaluator.Check("A union B", Value("blue")))
    assert(evaluator.Check("A union B", Value("indigo")))
    assert(evaluator.Check("A union B", Value("violet")))
  }

  it should "compute the union of 3 sets" in {

    val p = new Program(List(
      CreateNewSet("A"),
      CreateNewSet("B"),
      CreateNewSet("C"),
      Insert("A", Value("red"), Value("orange"), Value("yellow"), Value("green")),
      Insert("B", Value("blue"), Value("indigo")),
      Insert("C", Value("violet")),
      Assign(Variable("A union B union C"), Union(Union(Variable("A"), Variable("B")), Variable("C")))
    ))
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    assert(evaluator.Check("A union B union C", Value("red")))
    assert(evaluator.Check("A union B union C", Value("orange")))
    assert(evaluator.Check("A union B union C", Value("yellow")))
    assert(evaluator.Check("A union B union C", Value("green")))
    assert(evaluator.Check("A union B union C", Value("blue")))
    assert(evaluator.Check("A union B union C", Value("indigo")))
    assert(evaluator.Check("A union B union C", Value("violet")))
  }

  it should "compute the difference of 2 sets" in {

    val p = new Program(List(
      CreateNewSet("A"),
      CreateNewSet("B"),
      Insert("A", Value(1), Value(2), Value(3), Value(4)),
      Insert("B", Value(3), Value(4), Value(5)),
      Assign(Variable("A - B"), Difference(Variable("A"), Variable("B")))
    ))
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    assert(evaluator.Check("A - B", Value(1)))
    assert(evaluator.Check("A - B", Value(2)))
    assert(!evaluator.Check("A - B", Value(5)))
  }

  it should "compute A - B - C" in {

    val p = new Program(List(
      CreateNewSet("A"),
      CreateNewSet("B"),
      CreateNewSet("C"),
      Insert("A", Value(1), Value(2), Value(3), Value(4), Value(5), Value(6)),
      Insert("B", Value(3), Value(4), Value(5)),
      Insert("C", Value(6)),
      Assign(Variable("A - B - C"), Difference(Difference(Variable("A"), Variable("B")), Variable("C")))
    ))
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    assert(evaluator.Check("A - B - C", Value(1)))
    assert(evaluator.Check("A - B - C", Value(2)))
    assert(!evaluator.Check("A - B - C", Value(3)))
    assert(!evaluator.Check("A - B - C", Value(4)))
    assert(!evaluator.Check("A - B - C", Value(5)))
    assert(!evaluator.Check("A - B - C", Value(6)))
  }

  it should "compute A intersection B" in {

    val p = new Program(List(
      CreateNewSet("Europe"),
      CreateNewSet("Asia"),
      CreateNewSet("X"),
      Insert("Europe", Value("UK"), Value("France"), Value("Germany"), Value("Russia")),
      Insert("Asia", Value("China"), Value("Russia"), Value("Turkey"), Value("Japan")),
      Assign(Variable("X"), Intersection(Variable("Europe"), Variable("Asia")))
    ))
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    assert(evaluator.Check("X", Value("Russia")))
    assert(!evaluator.Check("X", Value("China")))
    assert(!evaluator.Check("X", Value("UK")))
    assert(!evaluator.Check("X", Value("France")))
    assert(!evaluator.Check("X", Value("Germany")))
    assert(!evaluator.Check("X", Value("Turkey")))
    assert(!evaluator.Check("X", Value("Japan")))
  }

  it should "compute A intersection B intersection C" in {
    val m2 = ListBuffer[Command]()
    val m3 = ListBuffer[Command]()
    val m5 = ListBuffer[Command]()
    for (i <- 1 to 100) {
      if (i % 2 == 0) {
        m2.addOne(Insert("Multiples of 2", Value(i)))
      }
      if (i % 3 == 0) {
        m3.addOne(Insert("Multiples of 3", Value(i)))
      }
      if (i % 5 == 0) {
        m5.addOne(Insert("Multiples of 5", Value(i)))
      }
    }
    val p = new Program(List(
      CreateNewSet("Multiples of 2"),
      CreateNewSet("Multiples of 3"),
      CreateNewSet("Multiples of 5"),
      CreateNewSet("dsl.Intersection")) ++
      m2.toList ++
      m3.toList ++
      m5.toList ++
      List(Assign(Variable("dsl.Intersection"), Intersection(Variable("Multiples of 2"), Intersection(Variable("Multiples of 3"),
        Variable("Multiples of 5"))))
    ))
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    for (i <- 1 to 100) {
      val shouldBePresent = i % 2 == 0 && i % 3 == 0 && i % 5 == 0
      assert(evaluator.Check("dsl.Intersection", Value(i)) == shouldBePresent)
    }
  }
}

