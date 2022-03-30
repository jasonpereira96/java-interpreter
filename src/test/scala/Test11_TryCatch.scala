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
}


