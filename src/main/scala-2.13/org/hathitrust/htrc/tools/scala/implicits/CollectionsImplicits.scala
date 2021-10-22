package org.hathitrust.htrc.tools.scala.implicits

import org.hathitrust.htrc.tools.scala.collections.{EndOfLineDehyphenator, PowerSet}

import scala.collection.generic.IsSeq
import scala.collection.{AbstractIterator, Factory, SeqOps}
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

  implicit class IteratorWithGroupConsecutiveWhen[A: ClassTag](s: Iterator[A]) {
    import org.hathitrust.htrc.tools.scala.collections.RewindableIterator

    /**
      * Groups consecutive elements that match the given predicate.
      *
      * @param p The predicate indicating the grouping condition.
      * @return An iterator containing the sequences of grouped elements
      */
    def groupConsecutiveWhen[C[X]](p: (A, A) => Boolean)
                               (implicit factory: Factory[A, C[A]]): Iterator[C[A]] = new AbstractIterator[C[A]] {
      private val (it1, it2) = s.duplicate
      private val ritr = new RewindableIterator(it1, 1)

      override def hasNext: Boolean = it2.hasNext

      override def next(): C[A] = {
        val count = (ritr.rewind().sliding(2) takeWhile {
          case collection.Seq(a1, a2) => p(a1, a2)
          case _ => false
        }).length

        (it2 take (count + 1)).to(factory)
      }
    }
  }

  implicit class StringIteratorWithDehyphenate(s: Iterator[String]) {
    /**
      * Dehyphenates a set of lines of text (i.e. joins end-of-line hyphenated words)
      * Loosely follows the rules specified at http://englishplus.com/grammar/00000129.htm
      *
      * @param allowedHyphenChars The allowed hyphen characters (used for determining when a word
      *                           is hyphenated or not) (Technical note: these are added to a regular
      *                           expression, so take care to escape things if necessary)
      * @return An iterator over the dehyphenated lines
      */
    def dehyphenate(allowedHyphenChars: String = "-‐‑‒–―−"): Iterator[String] =
      new EndOfLineDehyphenator(s, allowedHyphenChars)
  }

  implicit class StringSeqWithDehyphenate[C <: collection.Seq[String]](s: C) {
    /**
      * Dehyphenates a set of lines of text (i.e. joins end-of-line hyphenated words)
      * Loosely follows the rules specified at http://englishplus.com/grammar/00000129.htm
      *
      * @param allowedHyphenChars The allowed hyphen characters (used for determining when a word
      *                           is hyphenated or not) (Technical note: these are added to a regular
      *                           expression, so take care to escape things if necessary)
      * @return The dehyphenated lines
      */
    def dehyphenate(allowedHyphenChars: String = "-‐‑‒–―−")
                   (implicit factory: Factory[String, C]): C = {
      new EndOfLineDehyphenator(s.iterator, allowedHyphenChars).to(factory)
    }
  }

  final class SeqOpsWithTakeRightWhile[A, C](private val coll: SeqOps[A, Iterable, C]) extends AnyVal {
    def takeRightWhile(p: A => Boolean): C = coll.drop(coll.lastIndexWhere(!p(_)) + 1)
  }

  implicit def SeqOpsWithTakeRightWhile[Repr](coll: Repr)(implicit it: IsSeq[Repr]): SeqOpsWithTakeRightWhile[it.A, it.C] =
    new SeqOpsWithTakeRightWhile(it(coll))

  implicit class SeqWithPowerSet[+A](s: collection.Seq[A]) {

    /**
      * Constructs the power set of a sequence of elements, including only those
      * elements that satisfy a predicate.
      *
      * @param p The predicate
      * @return The power set
      */
    @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
    def powerSetWithExclusiveFilter(p: collection.Seq[A] => Boolean): List[List[A]] = {
      @annotation.tailrec
      def pwr(s: collection.Seq[A], acc: List[List[A]]): List[List[A]] =
        if (s.isEmpty) acc
        else pwr(s.tail, acc ++ acc.map(_ :+ s.head).filter(p))

      pwr(s, List(List.empty[A]))
    }

  }

  implicit class SeqWithIsMonotonic[A](seq: collection.Seq[A]) {
    /**
      * Checks whether the elements of a sequence are in monotonic order.
      *
      * @param cmp Function comparing two elements to determine their monotonicity
      * @return True if monotonic, False otherwise
      */
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    def isMonotonic(cmp: (A, A) => Boolean): Boolean =
      seq.sliding(2).forall {
        case x :: y :: Nil => cmp(x, y)
        case _ => true
      }
  }

  implicit class SeqWithLevenshtein[-A](s0: collection.Seq[A]) {
    /**
      * Returns the Levenshtein distance between two element sequences.
      *
      * @param s1 The other sequence
      * @return The Levenshtein distance
      */
    @SuppressWarnings(Array("org.wartremover.warts.Return", "org.wartremover.warts.Var"))
    def levenshteinScore(s1: collection.Seq[A]): Double = {
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

  implicit class IterableOnceWithMinMaxByOpt[A](t: IterableOnce[A]) {
    /**
      * Same as `maxBy` but guards against using maxBy on an empty collection
      *
      * @param f   The measuring function
      * @param cmp An ordering used when comparing elements
      * @tparam B The result type of the function f
      * @return An Option containing the first element of this collection or iterator with the largest value
      *         measured by function f with respect to the ordering cmp, or None if this collection or iterator is empty
      */
    def maxByOpt[B](f: A => B)(implicit cmp: Ordering[B]): Option[A] = t match {
      case _ if t.iterator.isEmpty => None
      case _ => Some(t.iterator.maxBy(f)(cmp))
    }

    /**
      * Same as `minBy` but guards against using minBy on an empty collection
      *
      * @param f   The measuring function
      * @param cmp An ordering used when comparing elements
      * @tparam B The result type of the function f
      * @return An Option containing the first element of this collection or iterator with the smallest value
      *         measured by function f with respect to the ordering cmp, or None if this collection or iterator is empty
      */
    def minByOpt[B](f: A => B)(implicit cmp: Ordering[B]): Option[A] = t match {
      case _ if t.iterator.isEmpty => None
      case _ => Some(t.iterator.minBy(f)(cmp))
    }
  }
}
