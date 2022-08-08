import dsl.{InvokeMethod, Method, Print, *}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Test09_TypesAndPolymorphism extends AnyFlatSpec with Matchers {
  behavior of "testing typed behaviour"

  it should "create a class and use it" in {
    val evaluator = new Evaluator()
    assertThrows[Throwable] {
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
      )
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
    assertThrows[Throwable] {
      val finalState = evaluator.run(
        DefineClass("A1", Extends("A4")),
        DefineClass("A2", Extends("A1")),
        DefineClass("A3", Extends("A2")),
        DefineClass("A4", Extends("A3"))
      )
    }
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
  it should "not allow objects of interfaces to be instantiated" in {
    val evaluator = new Evaluator()
    assertThrows[Throwable] {
      evaluator.run(
        DefineInterface("I"),
        Assign(Variable("a"), NewObject("I"))
      )
    }
  }
  it should "enforce that an abstract class must contain at least one abstract method" in {
    assertThrows[Throwable] {
      val evaluator = new Evaluator()
      evaluator.run(
        DefineClass("Shape",
          isAbstract(true)
        )
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
  it should "test that a class cannot inherit from itself" in {
    val evaluator = new Evaluator()
    assertThrows[Throwable] {
      evaluator.run(
        DefineClass("A",
          Extends("A")
        )
      )
    }
  }
  it should "test that a class can implement two or more different interfaces that declare methods with exactly the same signatures" in {
    val evaluator = new Evaluator()
    evaluator.run(
      DefineInterface("I1",
        InterfaceMethod("m")
      ),
      DefineInterface("I2",
        InterfaceMethod("m")
      ),
      DefineClass("A",
        Implements("I1"),
        Implements("I2"),
        Method("m",
          Print("hello")
        )
      )
    )
  }
  it should "test that an abstract class can implement interfaces" in {
    val evaluator = new Evaluator()
    evaluator.run(
      DefineInterface("I1",
        InterfaceMethod("m")
      ),
      DefineClass("A",
        isAbstract(true),
        Implements("I1"),
        AbstractMethod("m"),
      )
    )
  }
  it should "test that an abstract class can inherit from a concrete class" in {
    val evaluator = new Evaluator()
    evaluator.run(
      DefineClass("A",
        Method("m")
      ),
      DefineClass("B",
        isAbstract(true),
        Extends("A"),
        AbstractMethod("n"),
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

  it should "test the use of interface fields" in {
    val evaluator = new Evaluator()
    // The interface Car contains a field hp which is set to 700. Interface fields are implicitly final
    val finalState = evaluator.run(
      DefineInterface("Car",
        InterfaceField("hp", Value(700))
      ),
      DefineClass("Honda",
        Implements("Car"),
        Method("getHp",
          Return(This("hp"))
        )
      ),
      Assign(Variable("honda"), NewObject("Honda")),
      InvokeMethod(Variable("hp"), "honda", "getHp")
    )
    assert(finalState("hp") == Value(700))
  }
  it should "test the use of interface fields 2" in {
    val evaluator = new Evaluator()
    // The interface Car contains a field hp which is set to 700. Interface fields are implicitly final
    // Car has both fields hp and engine, since it extends Vehicle
    val finalState = evaluator.run(
      DefineInterface("Vehicle",
        InterfaceField("engine", Value("MD500"))
      ),
      DefineInterface("Car",
        ExtendsInterface("Vehicle"),
        InterfaceField("hp", Value(700))
      ),
      DefineClass("Honda",
        Implements("Car"),
        Method("getHp",
          Return(This("hp"))
        ),
        Method("getEngine",
          Return(This("engine"))
        )
      ),
      Assign(Variable("honda"), NewObject("Honda")),
      InvokeMethod(Variable("hp"), "honda", "getHp"),
      InvokeMethod(Variable("engine"), "honda", "getEngine")
    )
    assert(finalState("hp") == Value(700))
    assert(finalState("engine") == Value("MD500"))
  }
  it should "test that if a concrete derived class inherits from an abstract class then all abstract methods of the parent classes must be implemented in the derived class" in {
    val evaluator = new Evaluator()
    // Since Square is concrete but it extends the abstract class Shape and doesn't implement the abstract method getName(),
    // this code throws and error
    assertThrows[Throwable] {
      evaluator.run(
        DefineClass("Shape",
          isAbstract(true),
          AbstractMethod("getName")
        ),
        DefineClass("Square",
          Extends("Shape"),
          Field("side")
        )
      )
    }

    // Since Square is concrete and it extends the abstract class Shape and implements the
    // abstract method getName() as well, this code compiles correctly
    val evaluator2 = new Evaluator()
    evaluator2.run(
      DefineClass("Shape",
        isAbstract(true),
        AbstractMethod("getName")
      ),
      DefineClass("Square",
        Extends("Shape"),
        Field("side"),
        Constructor(
          Assign(This("side"), Value(1))
        ),
        Method("getName",
          Return(Value("Square"))
        )
      )
    )

    // Square extends Shape, but in this case both are abstract, so Square does not need to implement all
    // the abstract methods
    val evaluator3 = new Evaluator()
    evaluator3.run(
      DefineClass("Shape",
        isAbstract(true),
        AbstractMethod("getName")
      ),
      DefineClass("Square",
        isAbstract(true),
        Extends("Shape"),
        AbstractMethod("m"),
        Field("side")
      )
    )
  }
}



