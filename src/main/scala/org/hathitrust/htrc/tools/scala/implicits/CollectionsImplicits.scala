package org.hathitrust.htrc.tools.scala.implicits

import org.hathitrust.htrc.tools.scala.collections.PowerSet

import scala.collection.generic.CanBuildFrom
import scala.collection.{AbstractIterator, IterableLike}
import scala.reflect.ClassTag
import scala.language.higherKinds

object CollectionsImplicits {

  implicit class IterableWithPowerSet[A: ClassTag](it: Iterable[A]) {
    /**
      * Returns a new Iterable over the power set of the elements in this collection
      *
      * @return The power set of the elements as an Iterable
      */
    def powerSet: PowerSet[A] = new PowerSet[A](it)
  }

  implicit class IteratorWithGroupConsecutiveWhen[+A: ClassTag](s: Iterator[A]) {
    import org.hathitrust.htrc.tools.scala.collections.RewindableIterator

    /**
      * Groups consecutive elements that match the given predicate.
      *
      * @param p The predicate indicating the grouping condition.
      * @return An iterator containing the sequences of grouped elements
      */
    def groupConsecutiveWhen(p: (A, A) => Boolean): Iterator[List[A]] = new AbstractIterator[List[A]] {
      private val (it1, it2) = s.duplicate
      private val ritr = new RewindableIterator(it1, 1)

      override def hasNext: Boolean = it2.hasNext

      override def next(): List[A] = {
        val count = (ritr.rewind().sliding(2) takeWhile {
          case Seq(a1, a2) => p(a1, a2)
          case _ => false
        }).length

        (it2 take (count + 1)).toList
      }
    }
  }

  implicit class IterableWithGroupConsecutiveWhen[A: ClassTag, C[X] <: IterableLike[X, C[X]]](s: C[A])(implicit cbf: CanBuildFrom[C[A], List[A], C[List[A]]]) {

    import org.hathitrust.htrc.tools.scala.collections.RewindableIterator

    /**
      * Groups consecutive elements that match the given predicate.
      *
      * @param p The predicate indicating the grouping condition.
      * @return An `IterableLike` containing the sequences of grouped elements
      */
    def groupConsecutiveWhen(p: (A, A) => Boolean): C[List[A]] = {
      val it = new AbstractIterator[List[A]] {
        private val it1 = s.iterator
        private val it2 = s.iterator
        private val ritr = new RewindableIterator(it1, 1)

        override def hasNext: Boolean = it2.hasNext

        override def next(): List[A] = {
          val count = (ritr.rewind().sliding(2) takeWhile {
            case Seq(a1, a2) => p(a1, a2)
            case _ => false
          }).length

          (it2 take (count + 1)).toList
        }
      }

      val bf = cbf(s)
      while (it.hasNext)
        bf += it.next()

      bf.result()
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
    @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
    def powerSetWithExclusiveFilter(p: Seq[A] => Boolean): List[List[A]] = {
      @annotation.tailrec
      def pwr(s: Seq[A], acc: List[List[A]]): List[List[A]] =
        if (s.isEmpty) acc
        else pwr(s.tail, acc ++ acc.map(_ :+ s.head).filter(p))

      pwr(s, List(List.empty[A]))
    }

  }

  implicit class SeqWithIsIncreasing[A](seq: Seq[A]) {
    /**
      * Checks whether the elements of a sequence are in monotonic order.
      *
      * @param cmp Function comparing two elements to determine their monotonicity
      * @return True if monotonic, False otherwise
      */
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
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
    @SuppressWarnings(Array("org.wartremover.warts.Return", "org.wartremover.warts.Var"))
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
