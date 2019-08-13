package org.hathitrust.htrc.tools.scala.implicits
import org.hathitrust.htrc.tools.scala.implicits.CollectionsImplicits._
import org.scalatest.{FlatSpec, Matchers, ParallelTestExecution}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

@SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
class TraversableOnceExSpec extends FlatSpec with ScalaCheckPropertyChecks with Matchers with ParallelTestExecution {

  "maxByOpt" should "work like maxBy for non-empty collections" in {
    forAll { l: List[Int] =>
      whenever(l.nonEmpty) {
        l.maxByOpt(identity) should be(Some(l.max))
      }
    }
  }

  it should "return None when given an empty collection" in {
    List.empty[Int].maxByOpt(identity) should be(None)
  }

  "minByOpt" should "work like minBy for non-empty collections" in {
    forAll { l: List[Int] =>
      whenever(l.nonEmpty) {
        l.minByOpt(identity) should be(Some(l.min))
      }
    }
  }

  it should "return None when given an empty collection" in {
    List.empty[Int].minByOpt(identity) should be(None)
  }

}
