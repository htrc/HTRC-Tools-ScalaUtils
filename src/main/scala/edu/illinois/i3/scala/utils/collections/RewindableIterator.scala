package edu.illinois.i3.scala.utils.collections

object RewindableIterator {
  def apply[A](it: Iterator[A], remember: Int) = new RewindableIterator(it, remember)
}

/**
 * Provides an iterator that remembers the last `remember` values
 * @param it The iterator to wrap
 * @param remember The number of items to remember
 * @tparam A The type parameter of the wrapped iterator
 */
class RewindableIterator[A](it: Iterator[A], remember: Int) extends Iterator[A] {
  private var memory = List.empty[A]
  private var memoryIndex = 0

  override def next() = {
    if (memoryIndex < memory.length) {
      val next = memory(memoryIndex)
      memoryIndex += 1
      next
    } else {
      val next = it.next()
      memory = memory :+ next
      if (memory.length > remember)
        memory = memory drop 1
      memoryIndex = memory.length
      next
    }
  }

  /**
   * Checks whether the `rewind` operation can be performed safely
   *
   * @param n The number of items to go back
   * @return True if the iterator can be rewound, False otherwise
   */
  def canRewind(n: Int) = memoryIndex - n >= 0

  /**
   * Rewind the iterator by going back a specified number of items
   *
   * @param n The number of items to go back
   * @return This iterator, for continuations
   */
  def rewind(n: Int) = {
    require(memoryIndex - n >= 0, "Attempted to rewind past 'remember' limit")
    memoryIndex -= n
    this
  }

  /**
   * Rewinds this iterator as far back as possible (specified by the 'remember' constructor parameter)
   * @return This iterator, for continuations
   */
  def rewind() = {
    memoryIndex = 0
    this
  }

  override def hasNext = it.hasNext
}