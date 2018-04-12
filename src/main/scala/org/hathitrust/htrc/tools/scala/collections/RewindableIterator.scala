package org.hathitrust.htrc.tools.scala.collections

import scala.reflect.ClassTag

object RewindableIterator {
  def apply[A: ClassTag](it: Iterator[A], remember: Int): RewindableIterator[A] =
    new RewindableIterator(it, remember)
}

/**
  * Provides an iterator that remembers the last `remember` values
  *
  * @param underlying       The iterator to wrap
  * @param remember The number of items to remember
  * @tparam A The type parameter of the wrapped iterator
  */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
class RewindableIterator[A: ClassTag](underlying: Iterator[A], remember: Int) extends Iterator[A] {
  require(remember >= 0)

  private val memory = Array.ofDim[A](remember)
  private var index = 0
  private var memoryStart = 0
  private var numRemembered = 0

  override def next(): A = index match {
    case _ if index < numRemembered =>
      val memoryIndex = (memoryStart + index) % remember
      val next = memory(memoryIndex)
      index += 1
      next

    case _ if remember > 0 =>
      val next = underlying.next()
      val i = (memoryStart + numRemembered) % remember
      memory(i) = next
      if (numRemembered == remember)
        memoryStart = (memoryStart + 1) % remember
      else numRemembered += 1
      index = numRemembered
      next

    case _ => underlying.next()
  }

  /**
    * Checks whether the `rewind` operation can be performed safely
    *
    * @param n The number of items to go back
    * @return True if the iterator can be rewound, False otherwise
    */
  def canRewind(n: Int): Boolean = index - n >= 0

  /**
    * Rewind the iterator by going back a specified number of items
    *
    * @param n The number of items to go back
    * @return This iterator, for continuations
    */
  def rewind(n: Int): RewindableIterator[A] = {
    require(index - n >= 0, "Attempted to rewind past 'remember' limit")
    index -= n
    this
  }

  /**
    * Rewinds this iterator as far back as possible (specified by the 'remember' constructor parameter)
    *
    * @return This iterator, for continuations
    */
  def rewind(): RewindableIterator[A] = {
    index = 0
    this
  }

  override def hasNext: Boolean = index < numRemembered || underlying.hasNext
}