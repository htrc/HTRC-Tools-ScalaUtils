package org.hathitrust.htrc.tools.scala.collections

import scala.collection.{AbstractIterator, mutable}
import scala.reflect.ClassTag

/**
  * Class represents the power set of a collection of elements
  *
  * @param elements The elements
  * @tparam A The type of the elements
  */
class PowerSet[A: ClassTag](elements: Iterable[A]) extends Iterable[Iterable[A]] {
  private val inputSize = elements.size
  require(inputSize <= 30, s"Too many elements: $inputSize > 30")

  private val elementsMap = mutable.LinkedHashMap(elements.zipWithIndex.toSeq: _*)

  override def iterator: Iterator[Iterable[A]] = new AbstractIndexedIterator[Iterable[A]](size) {
    override protected def get(bitMask: Int): Iterable[A] = new SubSet[A](elementsMap, bitMask)

    override def contains(obj: Any): Boolean = obj match {
      case elems: Iterable[A] => elems.forall(e => elements.exists(_ == e))
      case _ => super.contains(obj)
    }
  }

  override def size: Int = 1 << inputSize

  override def isEmpty: Boolean = false

  override def nonEmpty: Boolean = true

  override def equals(obj: scala.Any): Boolean = obj match {
    case other: PowerSet[A] => elementsMap.equals(other.elementsMap)
    case _ => super.equals(obj)
  }

  override def hashCode(): Int = elementsMap.keySet.hashCode() << (inputSize - 1)

  override def toString(): String = s"PowerSet($elements)"
}

/**
  * Class defines a subset based on a bitmask representing set membership in the supplied map
  *
  * @param setMap  The element map (element -> position)
  * @param bitMask The bitmask
  * @tparam A The type of the elements
  */
class SubSet[+A: ClassTag](setMap: mutable.LinkedHashMap[A, Int], bitMask: Int) extends Iterable[A] {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  override def iterator: Iterator[A] = new AbstractIterator[A] {
    private val elements = setMap.keys.toArray
    private var setBits = bitMask

    override def hasNext: Boolean = setBits != 0

    override def next(): A = {
      require(hasNext, "hasNext must be true")
      val index = Integer.numberOfTrailingZeros(setBits)
      setBits &= ~(1 << index)
      elements(index)
    }

    override def contains(obj: Any): Boolean = obj match {
      case elem: A => setMap.get(elem).exists(i => (bitMask & (1 << i)) != 0)
      case _ => super.contains(obj)
    }
  }

  override def size: Int = Integer.bitCount(bitMask)
}