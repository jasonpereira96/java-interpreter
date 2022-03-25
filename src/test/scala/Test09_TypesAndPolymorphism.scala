import dsl.{Print, _}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Test09_TypesAndPolymorphism extends AnyFlatSpec with Matchers {
  behavior of "testing typed behaviour"

  it should "create a class and use it" in {
    val evaluator = new Evaluator()

    val finalState = evaluator.run(
      DefineClass("Point",
        Field("x"),
        Field("y"),
        isAbstract(true),
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

  it should "fail for cyclic inheritance" in {
    val evaluator = new Evaluator()
    assertThrows[Throwable] {
      val finalState = evaluator.run(
        DefineInterface("A1", ExtendsInterface("A2")),
        DefineInterface("A2", ExtendsInterface("A1")),
      )
    }
  }
  it should "fail for cyclic inheritance for classes" in {
    val evaluator = new Evaluator()
    val finalState = evaluator.run(
      DefineClass("A1", Extends("A4")),
      DefineClass("A2", Extends("A1")),
      DefineClass("A3", Extends("A2")),
      DefineClass("A4", Extends("A3"))
    )
  }
  it should "test for class not defined" in {
    val evaluator = new Evaluator()
    assertThrows[Throwable] {
      evaluator.run(
        DefineClass("A1", Extends("A4")),
      )
    }
  }
  it should "test for interface not defined" in {
    val evaluator = new Evaluator()
    assertThrows[Throwable] {
      evaluator.run(
        DefineInterface("I1", ExtendsInterface("I2"))
      )
    }
  }
  it should "Concrete classes should not have abstract methods" in {
    val evaluator = new Evaluator()
    assertThrows[Throwable] {
      evaluator.run(
        DefineClass("Shape",
          AbstractMethod("getArea")
        )
      )
    }
  }
  it should "not allow objects of abstract classes to be instantiated" in {
    val evaluator = new Evaluator()
    assertThrows[Throwable] {
      evaluator.run(
        DefineClass("A",
          isAbstract(true),
        ),
        Assign(Variable("a"), NewObject("A"))
      )
    }
  }
  it should "test if derived classes implement abstract methods" in {
    val evaluator = new Evaluator()
    evaluator.run(
      DefineClass("A",
        isAbstract(true),
        AbstractMethod("m1")
      ),
      DefineClass("B",
        Extends("A"),
        isAbstract(true),
        AbstractMethod("m2"),
        Method("m1")
      ),
      DefineClass("C",
        Extends("B"),
        isAbstract(false),
        Method("m2")
      )
    )
  }
  it should "test if derived classes implement abstract methods 2" in {
    val evaluator = new Evaluator()
    assertThrows[Throwable] {
      evaluator.run(
        DefineClass("A",
          isAbstract(true),
          AbstractMethod("m1")
        ),
        DefineClass("B",
          Extends("A"),
          isAbstract(true),
          AbstractMethod("m2"),
          Method("m1")
        ),
        DefineClass("C",
          Extends("B"),
          isAbstract(false),
//          Method("m2")
        )
      )
    }
  }

  it should "test that a class that implements an interface implements all its methods" in {
    val evaluator = new Evaluator()
    evaluator.run(
      DefineInterface("Bicycle",
        InterfaceMethod("applyBrakes"),
        InterfaceMethod("speedUp")
      ),
      DefineClass("BMX",
        Implements("Bicycle"),
        Method("applyBrakes",
          Print("applying brakes")
        ),
        Method("speedUp",
          Print("speeding up")
        )
      ),
    )
  }

  it should "throw an error if a required interface method is not implemented" in {
    val evaluator = new Evaluator()
    assertThrows[Throwable] {
      evaluator.run(
        DefineInterface("Bicycle",
          InterfaceMethod("applyBrakes"),
          InterfaceMethod("speedUp")
        ),
        DefineClass("BMX",
          Implements("Bicycle"),
          Method("applyBrakes",
            Print("applying brakes")
          ),
          // method speedup is now missing
        ),
      )
    }
  }

  it should "test non abstract classes having abstract methods" in {
    val evaluator = new Evaluator()
    evaluator.run(
      DefineClass("Shape",
        isAbstract(true),
        AbstractMethod("getArea")
      ),
      DefineClass("Square",
        Extends("Shape"),
        Field("side")
      )
    )
  }
}



