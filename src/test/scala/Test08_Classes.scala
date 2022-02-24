import dsl.{Print, _}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Test08_Classes extends AnyFlatSpec with Matchers {
  behavior of "testing union, intersection and difference"

  it should "create a class and use it" in {
    val evaluator = new Evaluator()

    val finalState = evaluator.run(
      DefineClass("Point",
        Field("x"),
        Field("y"),
        Constructor(
          Assign(This("x"), Value(0)),
          Assign(This("y"), Value(0)),
        ),
        Method("setX",
          Assign(This("x"), Variable("x"))
        )
      ),

      Assign(Variable("p1"), NewObject("Point")),
      dsl.InvokeMethod(Variable("_"), "p1", "setX", ("x", Value(50)))
    )

    assert(finalState.contains("p1"))
    val p1 = finalState("p1")

    p1 match {
      case dsl.Value(v) => {
        v match {
          case o: dsl.Object => {
            assert(o.getClassName == "Point")
            assert(o.getField("x") == Value(50))
            assertThrows[Exception] {
              o.getField("x1") == Value(50)
            }
          }
          case _ => {
            assert(false)
          }
        }
      }
      case _ => {
        assert(false)
      }
    }
  }

  it should "create a class and a child class and use them" in {
    val evaluator = new Evaluator()

    val finalState = evaluator.run(
      DefineClass("Point",
        Field("x"),
        Field("y"),
        Constructor(
          Assign(This("x"), Value(0)),
          Assign(This("y"), Value(0)),
        ),
        Method("setX",
          Assign(This("x"), Variable("x"))
        )
      ),
      DefineClass("3DPoint",
        Extends("Point"),
        Field("z"),
        Constructor(
          Assign(This("z"), Value(0)),
        ),
        Method("setZ",
          Assign(This("z"), Variable("z"))
        )
      ),

      Assign(Variable("p1"), NewObject("3DPoint")),
      dsl.InvokeMethod(Variable("_"), "p1", "setX", ("x", Value(50))),
      dsl.InvokeMethod(Variable("_"), "p1", "setZ", ("z", Value(60)))
    )

    assert(finalState.contains("p1"))
    val p1 = finalState("p1")

    p1 match {
      case dsl.Value(v) => {
        v match {
          case o: dsl.Object => {
            assert(o.getClassName == "3DPoint")
            assert(o.getField("x") == Value(50))
            assert(o.getField("y") == Value(0))
            assert(o.getField("z") == Value(60))
            assertThrows[Exception] {
              o.getField("x1") == Value(50)
            }
          }
          case _ => {
            assert(false)
          }
        }
      }
      case _ => {
        assert(false)
      }
    }
  }
  it should "create a class and a child class and override methods" in {
    val evaluator = new Evaluator()

    val finalState = evaluator.run(
      DefineClass("Animal",
        Constructor(
        ),
        Method("makeNoise",
          Print("generic animal noise....."),
          dsl.Return(Value("generic animal noise....."))
        )
      ),
      DefineClass("Cat",
        Extends("Animal"),
        Constructor(
        ),
        Method("makeNoise",
          Print("meow...."),
          Return(Value("meow....."))
        )
      ),
      DefineClass("Dog",
        Extends("Animal"),
        Constructor(
        ),
        Method("makeNoise",
          Print("woof...."),
          Return(Value("woof....."))
        )
      ),

      Assign(Variable("animal"), NewObject("Animal")),
      Assign(Variable("cat"), NewObject("Cat")),
      Assign(Variable("dog"), NewObject("Dog")),
      dsl.InvokeMethod(Variable("animalNoise"), "animal", "makeNoise"),
      dsl.InvokeMethod(Variable("catNoise"), "cat", "makeNoise"),
      dsl.InvokeMethod(Variable("dogNoise"), "dog", "makeNoise")
    )

    assert(finalState.contains("animal"))
    assert(finalState("animalNoise") == Value("generic animal noise....."))
    assert(finalState("catNoise") == Value("meow....."))
    assert(finalState("dogNoise") == Value("woof....."))
  }
}

