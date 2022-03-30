import dsl._
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
}


