import dsl.{Print, _}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Test08_Classes extends AnyFlatSpec with Matchers {
  behavior of "testing the functionality of classes, inheritance and nested classes"

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
      dsl.InvokeMethod(Variable("_"), "p1", "setX", Parameter("x", Value(50)))
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
      dsl.InvokeMethod(Variable("_"), "p1", "setX", Parameter("x", Value(50))),
      dsl.InvokeMethod(Variable("_"), "p1", "setZ", Parameter("z", Value(60)))
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

  it should "not allow multiple inheritance" in {
    val evaluator = new Evaluator()

    try {
      val finalState = evaluator.run(
        DefineClass("Student",
          Constructor()
        ),
        DefineClass("Instructor",
          Constructor(),
        ),
        DefineClass("Teaching_Assistant",
          Extends("Student"),
          Extends("Instructor"),
          Constructor(),
        )
      )
      assert(false)
    } catch {
      case _: Throwable => {
        // reaches here because it throws an exception
      }
    }
  }

  it should "create a class and a child class and inherit methods" in {
    val evaluator = new Evaluator()

    val finalState = evaluator.run(
      DefineClass("Person",
        Field("name"),
        Constructor(
          Assign(This("name"), Value(""))
        ),
        Method("setName",
          Assign(This("name"), Variable("name"))
        )
      ),
      DefineClass("Student",
        Extends("Person"),
        Field("gpa"),
        Constructor(
          Assign(This("gpa"), Value(0))
        ),
        Method("setGpa",
          Assign(This("gpa") , Variable("gpa"))
        ),
        Method("getGpa",
          Return(This("gpa"))
        ),
        Method("getName",
          Return(This("name"))
        )
      ),

      Assign(Variable("student"), NewObject("Student")),
      dsl.InvokeMethod(Variable("_"), "student", "setName", Parameter("name", Value("Jason"))),
      dsl.InvokeMethod(Variable("_"), "student", "setGpa", Parameter("gpa", Value(4.0))),
      dsl.InvokeMethod(Variable("studentName"), "student", "getName"),
      dsl.InvokeMethod(Variable("studentGpa"), "student", "getGpa")
    )

    assert(finalState.contains("studentName"))
    assert(finalState.contains("studentGpa"))
    assert(finalState("studentGpa") == Value(4.0))
    assert(finalState("studentName") == Value("Jason"))
  }

  it should "create a class and a nested class and use them" in {
    val evaluator = new Evaluator()

    val finalState = evaluator.run(
      DefineClass("Car",
        Field("name"),
        Constructor(
          Assign(This("name"), Value("Honda"))
        ),
        Method("setName",
          Assign(This("name"), Variable("name"))
        ),
        Method("getName",
          Return(This("name"))
        ),
        NestedClass("Engine",
          Field("engine"),
          Constructor(
            Assign(This("engine"), Value("V8"))
          ),
          Method("setEngine",
            Assign(This("engine"), Variable("engineName"))
          ),
          Method("getCarName",
            Return(This("name", "Car"))
          ),
        ),
      ),

      Assign(Variable("car"), NewObject("Car")),
      dsl.InvokeMethod(Variable("_"), "car", "setName", Parameter("name", Value("Ford"))),
      Assign(Variable("engine"), NewObject("Engine", "car")),
      dsl.InvokeMethod(Variable("carName"), "engine", "getCarName")
    )

    assert(finalState.contains("carName"))
    assert(finalState("carName") == Value("Ford"))
  }
}



