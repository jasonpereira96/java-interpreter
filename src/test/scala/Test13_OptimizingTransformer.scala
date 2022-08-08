import dsl.{Assign, *}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ListBuffer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class Test13_OptimizingTransformer extends AnyFlatSpec with Matchers {
  behavior of "optimizing transformer functions"


  it should "test optimizing add" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("x"), Add(Variable("y"), Value(0)))
    )
//    print(fs)
    assert(fs("x") == Variable("y"))
  }

  it should "test optimizing if else expression" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("result"), IfElseExpression(Value(false), Variable("x"), Variable("y")))
    )
    assert(fs("result") == Variable("y"))
  }

  it should "test optimizing intersect" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("B"),
      Assign(Variable("result"), Intersection(Variable("B"), Variable("A")))
    )
    assert(fs("result").asInstanceOf[Value].value.asInstanceOf[mutable.Set[Expression]].isEmpty)
  }
  it should "test optimizing intersect 2" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("B"),
      Assign(Variable("result"), Intersection(Variable("A"), Variable("B")))
    )
    assert(fs("result").asInstanceOf[Value].value.asInstanceOf[mutable.Set[Expression]].isEmpty)
  }

  it should "test optimizing intersect 3" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("result"), Intersection(Variable("A"), Variable("A")))
    )
    assert(fs("result") == Variable("A"))
  }

  it should "test optimizing union" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("result"), Union(Variable("A"), Variable("A")))
    )
    assert(fs("result") == Variable("A"))
  }
  it should "test optimizing union 2" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("result"), Union(Union(Variable("A"), Variable("A")), Variable("A")))
    )
    assert(fs("result") == Variable("A"))
  }

  it should "test optimizing union 3" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("A"),
      CreateNewSet("B"),
      Insert("A", Value(1), Value(2)),
      Insert("B", Value(2), Value(3)),
      Assign(Variable("result"), Union(Union(Variable("A"), Variable("B")), Variable("C")))
    )
    assert(fs("result") == Union(Value(mutable.Set(Value(3), Value(2), Value(1))),Variable("C")))
  }

  it should "test optimizing difference" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("B"),
      Assign(Variable("result"), Difference(Variable("A"), Variable("B")))
    )
    assert(fs("result") == Variable("A"))
  }
}


