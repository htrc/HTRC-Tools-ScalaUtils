package org.hathitrust.htrc.tools.scala.implicits

import org.hathitrust.htrc.tools.scala.implicits.CollectionsImplicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.ParallelTestExecution
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class GroupConsecutiveWhenSpec extends AnyFlatSpec
  with ScalaCheckPropertyChecks with should.Matchers with ParallelTestExecution {

  "IteratorGroupConsecutiveWhen" should "group consecutive iterator elements according to the predicate" in {
    val iterator = List(0,3,6,7,8,9,14,18,19,24,29,31,35,36,37,38,41).iterator
    val grouped = iterator.groupConsecutiveWhen[List]((a, b) => b-a == 1)

    grouped.toList should contain theSameElementsInOrderAs List(
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

}
