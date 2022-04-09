import dsl.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ListBuffer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
class Test11_TryCatch extends AnyFlatSpec with Matchers {
  behavior of "testing try catch finally"

  it should "test try catch" in {
    val evaluator = new Evaluator()

    evaluator.run(
      ExceptionClassDef("E"),
      Try(
        List[Command](
          Throw("E"),
          Assert(false)
        ),
        List[CatchBlock](
          CatchBlock(Constants.ANY,
            Print("Exception caught")
          )
        )
      )
    )
  }
  it should "test try catch and throw with a reason" in {
    val evaluator = new Evaluator()

    evaluator.run(
      ExceptionClassDef("E"),
      Try(
        List[Command](
          Throw("E", reason = "Some reason"),
          Assert(false)
        ),
        List[CatchBlock](
          CatchBlock("E",
            Print("Exception caught")
          )
        )
      )
    )
  }
  it should "test try catch ladder" in {
    val evaluator = new Evaluator()

    evaluator.run(
      ExceptionClassDef("E1"),
      ExceptionClassDef("E2"),
      ExceptionClassDef("E3"),
      ExceptionClassDef("E4"),
      ExceptionClassDef("E5"),
      Try(
        List[Command](
          Throw("E5"),
          Assert(false)
        ),
        List[CatchBlock](
          CatchBlock("E1",
            Print("E1 caught"),
            Assert(false)
          ),
          CatchBlock("E2",
            Print("E2 caught"),
            Assert(false)
          ),
          CatchBlock("E3",
            Print("E3 caught"),
            Assert(false)
          ),
          CatchBlock("E4",
            Print("E4 caught"),
            Assert(false)
          ),
          CatchBlock("E5",
            Print("E5 caught"),
            Assert(true)
          ),
          CatchBlock(Constants.ANY,
            Print("Exception caught"),
            Assert(false)
          ),
        )
      )
    )
  }
  it should "fail when throwing an exception which is not defined" in {
    val evaluator = new Evaluator()
    assertThrows[Throwable] {
      evaluator.run(
        ExceptionClassDef("E1"),
        Try(
          List[Command](
            Throw("E2"),
            Assert(false)
          ),
          List[CatchBlock](
            CatchBlock("E1",
              Print("E1 caught"),
              Assert(false)
            ),
          )
        )
      )
    }
  }
  it should "test nested try catch" in {
    val evaluator = new Evaluator()

    evaluator.run(
      ExceptionClassDef("E1"),
      ExceptionClassDef("E2"),
      ExceptionClassDef("E3"),
      Try(
        List[Command](
          Try(
            List[Command](
              Throw("E3")
            ),
            List[CatchBlock](
              CatchBlock("E1", Print("Exception of type E1 caught"), Assert(false)),
              CatchBlock("E2", Print("Exception of type E2 caught"), Assert(false)),
            ),
            FinallyBlock(
              Print("finally 1 executed")
            )
          )
        ),
        List[CatchBlock](
          CatchBlock("E3", Print("Exception of type E3 caught"), Assert(true)),
        ),
        FinallyBlock(
          Print("finally 2 executed")
        )
      )
    )
  }

  it should "test the if inside try" in {
    val evaluator = new Evaluator()

    val finalState = evaluator.run(
      ExceptionClassDef("E1"),
      Try(
        List[Command](
          Assign(Variable("condition"), Value(false)),
          Assign(Variable("c"), Value(0)),
          IfElse(Variable("condition"),
            List[Command](
              Assign(Variable("c"), Value(10))
            ),
            List[Command](
              Assign(Variable("c"), Value(20)),
              Throw("E1")
            )
          )
        ), List[CatchBlock](
            CatchBlock(
              Constants.ANY,
              Assign(Variable("c"), Value(30))
          )
        )
      )
    )
    assert(finalState("c") == Value(30))
  }
}


