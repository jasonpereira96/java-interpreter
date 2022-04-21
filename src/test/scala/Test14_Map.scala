import dsl.{Assign, *}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ListBuffer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class Test14_Map extends AnyFlatSpec with Matchers {
  behavior of "map"


  it should "test map" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("S"),
      Insert("S", Value(1)),
      Insert("S", Value(2)),
      Insert("S", Value(3)),
      Insert("S", Value(4)),
      Assign(Variable("P"), Map(Variable("S"), AnonymousFunction(
        Return(Add(Variable(Constants.ELEMENT), Value(10)))
      )
      ))
    )
    val resultSet = fs("P").asInstanceOf[Value].value.asInstanceOf[mutable.Set[Expression]]
    assert(resultSet.contains(Value(11)))
    assert(resultSet.contains(Value(12)))
    assert(resultSet.contains(Value(13)))
    assert(resultSet.contains(Value(14)))
    assert(resultSet.size == 4)
  }
  it should "test map 2" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("S"),
      Insert("S", Value("Pizza")),
      Insert("S", Value("Barbeque")),
      Insert("S", Value("Ramen")),
      Insert("S", Value("Sushi")),
      Assign(Variable("P"), Map(Variable("S"), AnonymousFunction(
        Return(Add(Variable(Constants.ELEMENT), Value(" is my favourite")))
      )
      ))
    )
    val resultSet = fs("P").asInstanceOf[Value].value.asInstanceOf[mutable.Set[Expression]]
    assert(resultSet.contains(Value("Pizza is my favourite")))
    assert(resultSet.contains(Value("Barbeque is my favourite")))
    assert(resultSet.contains(Value("Ramen is my favourite")))
    assert(resultSet.contains(Value("Sushi is my favourite")))
    assert(resultSet.size == 4)
  }

  it should "test map with partial evaluation" in {
    val evaluator = new Evaluator()

    val fs = evaluator.run(
      CreateNewSet("S"),
      Insert("S", Value(1)),
      Insert("S", Value(2)),
      Insert("S", Value(3)),
      Insert("S", Value(4)),
      Insert("S", Value(5)),
      Insert("S", Value(6)),
      Assign(Variable("P"), Map(Variable("S"), AnonymousFunction(
        Return(Add(Variable(Constants.ELEMENT), Variable("X")))
      )
      ))
    )
    val resultSet = fs("P").asInstanceOf[Value].value.asInstanceOf[mutable.Set[Expression]]
    assert(resultSet.contains(Add(Value(1), Variable("X"))))
    assert(resultSet.contains(Add(Value(2), Variable("X"))))
    assert(resultSet.contains(Add(Value(3), Variable("X"))))
    assert(resultSet.contains(Add(Value(4), Variable("X"))))
    assert(resultSet.contains(Add(Value(5), Variable("X"))))
    assert(resultSet.contains(Add(Value(6), Variable("X"))))
    assert(resultSet.size == 6)
  }
}


