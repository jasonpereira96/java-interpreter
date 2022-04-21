import dsl.{Assign, *}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ListBuffer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Test13_OptimizingTransformer extends AnyFlatSpec with Matchers {
  behavior of "optimizing transformer functions"

  
  it should "test map" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("S"),
      Insert("S", Value(1)),
      Insert("S", Value(2)),
      Insert("S", Value(3)),
      Insert("S", Value(4)),
      Assign(Variable("P"), Map(Variable("S"), AnonymousFunction(
        Return(Add(Variable(Constants.ELEMENT), Value(2)))
      )
      ))
    )
    print(fs)
  }
  it should "test optimizing add" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("x"), Add(Variable("y"), Value(0)))
    )
    print(fs)
  }

  it should "test optimizing if else expression" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("result"), IfElseExpression(Value(false), Variable("x"), Variable("y")))
    )
    print(fs)
  }

  it should "test optimizing intersect" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("B"),
      Assign(Variable("result"), Intersection(Variable("B"), Variable("A")))
    )
    print(fs)
  }
  it should "test optimizing intersect 2" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("B"),
      Assign(Variable("result"), Intersection(Variable("A"), Variable("B")))
    )
    print(fs)
  }

  it should "test optimizing intersect 3" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("result"), Intersection(Variable("A"), Variable("A")))
    )
    print(fs)
  }

  it should "test optimizing union" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("result"), Union(Variable("A"), Variable("A")))
    )
    print(fs)
  }
  it should "test optimizing union 2" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("result"), Union(Union(Variable("A"), Variable("A")), Variable("A")))
    )
    print(fs)
  }

  it should "test optimizing difference" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("B"),
      Assign(Variable("result"), Difference(Variable("A"), Variable("B")))
    )
    print(fs)
  }
}


