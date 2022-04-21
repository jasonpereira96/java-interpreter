import dsl.{Assign, *}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ListBuffer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Test14_Map extends AnyFlatSpec with Matchers {
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
    print(fs)
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
    print(fs)
  }
}


