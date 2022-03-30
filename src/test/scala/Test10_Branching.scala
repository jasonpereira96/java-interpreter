import dsl._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.collection.mutable.ListBuffer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
class Test10_Branching extends AnyFlatSpec with Matchers {
  behavior of "testing if else constructs"

  it should "test the if construct" in {
    val evaluator = new Evaluator()

    evaluator.run(
      Assign(Variable("condition"), Value(true)),
      If(Variable("condition"),
        Print("condition is true")
      )
    )
  }
  it should "test the if else construct" in {
    val evaluator = new Evaluator()

    evaluator.run(
      Assign(Variable("condition"), Value(true)),
      IfElse(Variable("condition"), List[Command](
          Print("condition is true")
        ),
        List[Command](
          Print("condition is false"),
          Assert(false) // code should never reach here
        )
      )
    )
  }

  it should "test the if else construct for other truthy values" in {
    val evaluator = new Evaluator()

    evaluator.run(
      IfElse(Value(true), List[Command](
        Print("condition is true since true is a truthy value")
      ),
        List[Command](
          Assert(false) // code should never reach here
        )
      ),
      IfElse(Value(1), List[Command](
        Print("condition is true since 1 is a truthy value")
      ),
        List[Command](
          Assert(false) // code should never reach here
        )
      ),
      IfElse(Value("A"), List[Command](
        Print("condition is true since A is a truthy value")
      ), List[Command](
        Assert(false)
      )
      ),


      IfElse(Value(false), List[Command](
        Assert(false)
      ),
        List[Command](
          Print("condition is false since false is a falsey value")
        )
      ),
      IfElse(Value(0), List[Command](
        Assert(false)
      ),
        List[Command](
          Print("condition is false since 0 is a falsey value")
        )
      ),
      IfElse(Value(""), List[Command](
        Assert(false)
      ),
        List[Command](
          Print("condition is false since '' is a falsey value")
        )
      )
    )
  }
  it should "test an if else ladder" in {
    val evaluator = new Evaluator()

    evaluator.run(
      // setting a = 4
      Assign(Variable("a"), Value(4)),
      IfElse(EqualTo(Variable("a"), Value(1)), ifStatements=List[Command](
        Print("a is 1"), Assert(false)), elseStatements=List[Command](
        IfElse(EqualTo(Variable("a"), Value(2)), ifStatements=List[Command](
          Print("a is 2"), Assert(false)), elseStatements=List[Command](
          IfElse(EqualTo(Variable("a"), Value(3)), ifStatements=List[Command](
            Print("a is 3"), Assert(false)), elseStatements=List[Command](
            IfElse(EqualTo(Variable("a"), Value(4)), ifStatements=List[Command](
              Print("a is 4"), Assert(true)), elseStatements=List[Command](
              IfElse(EqualTo(Variable("a"), Value(5)), ifStatements=List[Command](
                Print("a is 5"), Assert(false)), elseStatements=List[Command](
                Print("in the else clause"),
                Assert(false)
              ))
            ))
          ))
        ))
      ))
    )
  }
}


