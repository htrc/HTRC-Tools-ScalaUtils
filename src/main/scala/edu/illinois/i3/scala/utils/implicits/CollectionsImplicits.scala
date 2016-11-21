package edu.illinois.i3.scala.utils.implicits

import edu.illinois.i3.scala.utils.collections.PowerSet

import scala.collection.AbstractIterator
import scala.reflect.ClassTag

object CollectionsImplicits {

  implicit class IterableWithPowerSet[A: ClassTag](it: Iterable[A]) {
    /**
     * Returns a new Iterable over the power set of the elements in this collection
     *
     * @return The power set of the elements as an Iterable
     */
    def powerSet = new PowerSet[A](it)
  }

  implicit class SeqWithGroupWhen[+A](s: Seq[A]) {
    import edu.illinois.i3.scala.utils.collections.RewindableIterator

    def groupWhen(p: (A, A) => Boolean): Iterator[Seq[A]] = new AbstractIterator[Seq[A]] {
      val (it1, it2) = s.iterator.duplicate
      val ritr = new RewindableIterator(it1, 1)

      override def hasNext = it2.hasNext

      override def next() = {
        val count = (ritr.rewind().sliding(2) takeWhile {
          case Seq(a1, a2) => p(a1, a2)
          case _ => false
        }).length

        (it2 take (count + 1)).toSeq
      }
    }
  }

  implicit class SeqWithPowerSet[+A](s: Seq[A]) {

    def powerSetWithExclusiveFilter(p: Seq[A] => Boolean) = {
      @annotation.tailrec
      def pwr(s: Seq[A], acc: Seq[Seq[A]]): Seq[Seq[A]] =
        if (s.isEmpty) acc
        else pwr(s.tail, acc ++ acc.map(_ :+ s.head).filter(p))

      pwr(s, Seq(Seq.empty[A]))
    }
    
  }

  implicit class SeqWithIsIncreasing[A](seq: Seq[A]) {
    def isMonotonic(cmp: (A,A) => Boolean) = {
      import scala.util.control.Breaks._

      var last: Option[A] = None
      var result = false

      breakable {
        for (e <- seq) {
          last.filterNot(l => cmp(l, e)).foreach(_ => break())
          last = Some(e)
        }
        result = true
      }

      result
    }
  }

  implicit class SeqWithLevenshtein[-A](s0: Seq[A]) {
    def levenshteinScore(s1: Seq[A]): Double = {
      if (s0 == s1) return 0

      val len0 = s0.size
      val len1 = s1.size

      if (len0 == 0) return len1
      if (len1 == 0) return len0

      var cost = new Array[Int](len0 + 1)
      var newcost = new Array[Int](len0 + 1)

      for (i <- cost.indices)
        cost(i) = i

      for (j <- 0 until len1) {
        // initial cost of skipping prefix in String s1
        newcost(0) = j + 1

        for (i <- 0 until len0) {
          // matching current token in both sequences
          val tokenMatch = if (s0(i) == s1(j)) 0 else 1

          // computing cost for each transformation
          val cost_replace = cost(i) + tokenMatch
          val cost_insert = cost(i + 1) + 1
          val cost_delete = newcost(i) + 1

          // keep minimum cost
          newcost(i + 1) = math.min(math.min(cost_insert, cost_delete), cost_replace)
        }

        // swap cost/newcost arrays
        val swap = cost
        cost = newcost
        newcost = swap
      }

      // the distance is the cost for transforming all letters in both strings
      cost(len0)
    }
  }

  implicit class TraversableOnceToSortedMap[A,B](tuples: TraversableOnce[(A, B)])(implicit ordering: Ordering[A]) {
    import scala.collection.immutable.SortedMap

    def toSortedMap = SortedMap(tuples.toSeq: _*)
  }

  implicit class IterableWithAvg[T : Numeric](data: Iterable[T]) {
    val numeric = implicitly[Numeric[T]]
    def avg = {
      val (sum, count) = data.foldLeft((numeric.zero, 0)) { case ((s, c), e) => (numeric.plus(s, e), c+1) }
      numeric.toDouble(sum) / count
    }
  }
}
