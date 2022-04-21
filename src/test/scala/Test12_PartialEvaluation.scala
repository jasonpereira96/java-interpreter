import dsl.{Assign, *}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ListBuffer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Test12_PartialEvaluation extends AnyFlatSpec with Matchers {
  behavior of "partial evaluation"

  it should "partial eval" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("A"),
      Insert("A", Value(1)),
      Assign(Variable("C"), Union(Variable("A"), Variable("B")))
    )

    print(fs)
  }

  it should "basic addition" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("x"), Value(1)),
      Assign(Variable("y"), Value(2)),
      Assign(Variable("s"), Add(Variable("x"), Variable("y")))
    )
    print(fs)
  }

  it should "partially evaluated addition" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("x"), Value(1)),
      Assign(Variable("y"), Value(2)),
      Assign(Variable("s"), Add(Variable("x"), Variable("y"))),
      Assign(Variable("z"), Add(Variable("s"), Value(4))),
      Assign(Variable("undef"), Add(Variable("z"), Variable("h")))
    )

//    println(List(1,2,3,4).map(item => item * 2))

    print(fs)
  }

  it should "partial eval if-else" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("x"), Value(1)),
      Assign(Variable("s"), IfElseExpression(
        EqualTo(Variable("x"), Variable("y")),
        Add(Variable("x"), Value(2)),
        Add(Variable("x"), Value(3))
      ))
    )
    print(fs)
  }

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
}


