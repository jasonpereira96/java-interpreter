import dsl._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Test05_Scope extends AnyFlatSpec with Matchers {
  behavior of "testing variable shadowing and scope"
  it should "create a scope and implement variable shadowing" in {
    val s1 = CreateNewSet("A")
    val s2 = Assign(Variable("x"), Value("outer x"))
    val s3 = Assign(Variable("y"), Value("outer y"))
    val s4 = CreateNewSet("Result")
    val s5 = dsl.NamedScope("scope1",
      Assign(Variable("x"), Value("inner x")),
      Insert("A", Variable("x"), Variable("y"))
    )
    val evaluator = new Evaluator()
    evaluator.run(s1, s2, s3, s4, s5)

    assert(evaluator.Check("A", Value("inner x")))
    assert(evaluator.Check("A", Value("outer y")))
  }

  /*
  A = {}
  x = "outermost x"
  y = "outermost y"
  {
    x = "outer x"
    {
      x = "inner x"
      A.insert(x)
      A.insert(y)
    }
  }
  // A should be {"inner x", "outermost y"}
   */
  it should "create a nested scope and implement variable shadowing" in {
    val s1 = CreateNewSet("A")
    val s2 = Assign(Variable("x"), Value("outermost x"))
    val s3 = Assign(Variable("y"), Value("outermost y"))
    val s4 = NamedScope("scope1",
      Assign(Variable("x"), Value("outer x")),
      NamedScope("scope2",
        Assign(Variable("x"), Value("inner x")),
        Insert("A", Variable("x")),
        Insert("A", Variable("y"))
      )
    )
    val p = new Program(List(s1, s2, s3, s4))
    val evaluator = new Evaluator()
    evaluator.runProgram(p)

    assert(evaluator.Check("A", Value("inner x")))
    assert(evaluator.Check("A", Value("outermost y")))
  }

  it should "create a deeply nested scope and implement variable shadowing" in {

    val program = new Program(List(
      CreateNewSet("A"),
      Assign(Variable("x"), Value(0)),
      Assign(Variable("y"), Value(0)),
      NamedScope("scope1",
        Assign(Variable("x"), Value(1)),
        NamedScope("scope2",
          Assign(Variable("x"), Value(2)),
          NamedScope("scope3",
            Assign(Variable("x"), Value(3)),
            NamedScope("scope4",
              Assign(Variable("x"), Value(4)),
              NamedScope("scope5",
                Assign(Variable("x"), Value(5)),
                NamedScope("scope6",
                  Assign(Variable("x"), Value(6)),
                  NamedScope("scope7",
                    Assign(Variable("x"), Value(7)),
                    Assign(Variable("y"), Variable("x")),
                    Insert("A", Variable("y"))
                  )
                )
              )
            )
          )
        )
      )
    ))
    val evaluator = new Evaluator()
    evaluator.runProgram(program)

    assert(evaluator.Check("A", Value(7)))
  }
}

