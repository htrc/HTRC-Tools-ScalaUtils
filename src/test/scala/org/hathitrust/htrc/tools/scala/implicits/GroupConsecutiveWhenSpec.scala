package org.hathitrust.htrc.tools.scala.implicits

import org.hathitrust.htrc.tools.scala.implicits.CollectionsImplicits._
import org.scalatest.{FlatSpec, Matchers, ParallelTestExecution}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class GroupConsecutiveWhenSpec extends FlatSpec
  with ScalaCheckPropertyChecks with Matchers with ParallelTestExecution {

  "IterableGroupConsecutiveWhen" should "group consecutive elements according to the predicate" in {
    val elems = List(0,3,6,7,8,9,14,18,19,24,29,31,35,36,37,38,41)
    val grouped = elems.groupConsecutiveWhen((a, b) => b-a == 1)

    grouped should contain theSameElementsInOrderAs List(
      List(0),
      List(3),
      List(6, 7, 8, 9),
      List(14),
      List(18, 19),
      List(24),
      List(29),
      List(31),
      List(35, 36, 37, 38),
      List(41)
    )
  }

  "IteratorGroupConsecutiveWhen" should "group consecutive iterator elements according to the predicate" in {
    val iterator = List(0,3,6,7,8,9,14,18,19,24,29,31,35,36,37,38,41).iterator
    val grouped = iterator.groupConsecutiveWhen((a, b) => b-a == 1)

    assert {
      grouped sameElements List(
        List(0),
        List(3),
        List(6, 7, 8, 9),
        List(14),
        List(18, 19),
        List(24),
        List(29),
        List(31),
        List(35, 36, 37, 38),
        List(41)
      ).iterator
    }
  }

}
