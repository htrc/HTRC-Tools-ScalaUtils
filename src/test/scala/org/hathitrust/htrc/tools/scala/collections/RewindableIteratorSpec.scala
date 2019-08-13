package org.hathitrust.htrc.tools.scala.collections

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.util.Random

@SuppressWarnings(Array("org.wartremover.warts.Var"))
class RewindableIteratorSpec extends FlatSpec
  with ScalaCheckPropertyChecks with Matchers {

  def consume[T](it: Iterator[T], n: Int): List[T] =
    Iterator.range(0, n).map(_ => it.next()).toList

  "RewindableIterator" should "throw an error on negative `remember` argument" in {
    an[IllegalArgumentException] should be thrownBy {
      RewindableIterator(Iterator.empty, -1)
    }
  }

  it should "throw an error when attempting to rewind past its bounds" in {
    forAll(Gen.posNum[Int], Gen.posNum[Int]) { (size, n) =>
      whenever(size >= 0 && size < 1000 && n > size) {
        val it = Iterator.continually(Random.nextInt())
        val rit = RewindableIterator(it, size)
        consume(rit, n)

        an[IllegalArgumentException] should be thrownBy {
          rit.rewind(n)
        }
      }
    }
  }

  it should "correctly return all memoized elements when rewound" in {
    var genData = for {
      lst <- arbitrary[List[Int]]
      n <- Gen.choose(0, lst.size)
    } yield (lst, n)

    // to fix shrinking issue not respecting the original generator constraint of m <= n
    // see https://gist.github.com/davidallsopp/f65d73fea8b5e5165fc3
    genData = genData suchThat {
      case (lst, n) => n <= lst.size
    }

    forAll(genData) { case (lst, n) =>
      val it = lst.iterator
      val rit = RewindableIterator(it, n)
      val lst1 = consume(rit, n)
      val lst2 = consume(rit.rewind(), n)
      lst1 should contain theSameElementsInOrderAs lst2
    }
  }

  it should "correctly return selected memoized elements when rewound" in {
    var genData = for {
      n <- Gen.choose(0, 1000)
      m <- Gen.choose(0, n)
    } yield (n, m)

    // to fix shrinking issue not respecting the original generator constraint of m <= n
    // see https://gist.github.com/davidallsopp/f65d73fea8b5e5165fc3
    genData = genData suchThat {
      case (n, m) => m <= n
    }

    forAll(genData) { case (n, m) =>
      val it = Iterator.continually(Random.nextInt())
      val rit = RewindableIterator(it, n)
      val lst1 = consume(rit, n)
      val lst2 = consume(rit.rewind(m), m)
      lst1.takeRight(m) should contain theSameElementsInOrderAs lst2
    }
  }

  it should "correctly iterate over memoized and new elements" in {
    val genData = for {
      n <- Gen.choose(0, 20)
      m <- Gen.choose(0, 30)
    } yield (n, m)

    forAll(genData) { case (n, m) =>
      val it = Iterator.from(0)
      val rit = RewindableIterator(it, n+1)
      consume(rit, 3*n)
      val lst2 = consume(rit.rewind(n), n+m)
      val expected = Iterator.range(2 * n, 3 * n + m).toList
      lst2 should contain theSameElementsInOrderAs expected
    }
  }
}
