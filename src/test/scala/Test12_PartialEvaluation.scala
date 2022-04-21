import dsl.{Assign, *}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ListBuffer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class Test12_PartialEvaluation extends AnyFlatSpec with Matchers {
  behavior of "partial evaluation"

  it should "partial eval" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("A"),
      Insert("A", Value(1)),
      Assign(Variable("C"), Union(Variable("A"), Variable("B")))
    )
//    println(fs)

    assert(fs("C").isInstanceOf[Union])
    val u1 = fs("C").asInstanceOf[Union].exp1.asInstanceOf[Value]
    val u2 = fs("C").asInstanceOf[Union].exp2.asInstanceOf[Variable]
    assert(u1.value.asInstanceOf[mutable.Set[Expression]].contains(Value(1)))
    assert(u2.name == "B")
  }

  it should "basic addition" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      Assign(Variable("x"), Value(1)),
      Assign(Variable("y"), Value(2)),
      Assign(Variable("s"), Add(Variable("x"), Variable("y")))
    )
//    print(fs)
    assert(fs("s") == Value(3))
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
    assert(fs("undef") == Add(Value(7), Variable("h")))

//    print(fs)
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
    assert(fs("s") == IfElseExpression(EqualTo(Value(1), Variable("y")), Value(3), Value(4)))
  }
  
}


