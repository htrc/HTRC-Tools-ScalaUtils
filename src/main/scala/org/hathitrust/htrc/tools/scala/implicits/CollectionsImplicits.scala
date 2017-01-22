package org.hathitrust.htrc.tools.scala.implicits

import org.hathitrust.htrc.tools.scala.collections.PowerSet

import scala.collection.AbstractIterator
import scala.reflect.ClassTag

object CollectionsImplicits {

  implicit class IterableWithPowerSet[A: ClassTag](it: Iterable[A]) {
    /**
      * Returns a new Iterable over the power set of the elements in this collection
      *
      * @return The power set of the elements as an Iterable
      */
    def powerSet: PowerSet[A] = new PowerSet[A](it)
  }

  implicit class SeqWithGroupWhen[+A](s: Seq[A]) {

    import org.hathitrust.htrc.tools.scala.collections.RewindableIterator

    /**
      * Groups consecutive elements that match the given predicate.
      *
      * @param p The predicate indicating the grouping condition.
      * @return An iterator containing the sequences of grouped elements
      */
    def groupWhen(p: (A, A) => Boolean): Iterator[Seq[A]] = new AbstractIterator[Seq[A]] {
      val (it1, it2) = s.iterator.duplicate
      val ritr = new RewindableIterator(it1, 1)

      override def hasNext: Boolean = it2.hasNext

      override def next(): Seq[A] = {
        val count = (ritr.rewind().sliding(2) takeWhile {
          case Seq(a1, a2) => p(a1, a2)
          case _ => false
        }).length

        (it2 take (count + 1)).toSeq
      }
    }
  }

  implicit class SeqWithPowerSet[+A](s: Seq[A]) {

    /**
      * Constructs the power set of a sequence of elements, including only those
      * elements that satisfy a predicate.
      *
      * @param p The predicate
      * @return The power set
      */
    def powerSetWithExclusiveFilter(p: Seq[A] => Boolean): Seq[Seq[A]] = {
      @annotation.tailrec
      def pwr(s: Seq[A], acc: Seq[Seq[A]]): Seq[Seq[A]] =
        if (s.isEmpty) acc
        else pwr(s.tail, acc ++ acc.map(_ :+ s.head).filter(p))

      pwr(s, Seq(Seq.empty[A]))
    }

  }

  implicit class SeqWithIsIncreasing[A](seq: Seq[A]) {
    /**
      * Checks whether the elements of a sequence are in monotonic order.
      *
      * @param cmp Function comparing two elements to determine their monotonicity
      * @return True if monotonic, False otherwise
      */
    def isMonotonic(cmp: (A, A) => Boolean): Boolean = {
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
    /**
      * Returns the Levenshtein distance between two element sequences.
      *
      * @param s1 The other sequence
      * @return The Levenshtein distance
      */
    def levenshteinScore(s1: Seq[A]): Double = {
      if (s0 == s1) return 0

      val len0 = s0.size
      val len1 = s1.size

      if (len0 == 0) return len1
      if (len1 == 0) return len0

      var cost = new Array[Int](len0 + 1)
      var newCost = new Array[Int](len0 + 1)

      for (i <- cost.indices)
        cost(i) = i

      for (j <- 0 until len1) {
        // initial cost of skipping prefix in String s1
        newCost(0) = j + 1

        for (i <- 0 until len0) {
          // matching current token in both sequences
          val tokenMatch = if (s0(i) == s1(j)) 0 else 1

          // computing cost for each transformation
          val costReplace = cost(i) + tokenMatch
          val costInsert = cost(i + 1) + 1
          val costDelete = newCost(i) + 1

          // keep minimum cost
          newCost(i + 1) = math.min(math.min(costInsert, costDelete), costReplace)
        }

        // swap cost/newCost arrays
        val swap = cost
        cost = newCost
        newCost = swap
      }

      // the distance is the cost for transforming all letters in both strings
      cost(len0)
    }
  }

  implicit class TraversableOnceToSortedMap[A, B](tuples: TraversableOnce[(A, B)])
    (implicit ordering: Ordering[A]) {

    import scala.collection.immutable.SortedMap

    /**
      * Converts a TraversableOnce to SortedMap.
      *
      * @return The SortedMap
      */
    def toSortedMap: SortedMap[A, B] = SortedMap(tuples.toSeq: _*)
  }

  implicit class IterableWithAvg[T: Numeric](data: Iterable[T]) {
    private val numeric = implicitly[Numeric[T]]

    /**
      * Computes the average (mean) of a sequence of numbers from an `Iterable`.
      *
      * @return The average (mean)
      */
    def avg: Double = {
      val (sum, count) = data.foldLeft((numeric.zero, 0)) {
        case ((s, c), e) => (numeric.plus(s, e), c + 1)
      }

      numeric.toDouble(sum) / count
    }
  }

}
