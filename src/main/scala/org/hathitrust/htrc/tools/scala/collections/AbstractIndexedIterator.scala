package org.hathitrust.htrc.tools.scala.collections

import scala.collection.AbstractIterator

/**
 * Defines an indexed iterator
 *
 * @param size The size of the collection it wraps
 * @tparam A The type parameter
 */
abstract class AbstractIndexedIterator[+A](override val size: Int) extends AbstractIterator[A] {
  private var position = 0

  override def hasNext: Boolean = position < size

  override def next(): A = {
    if (!hasNext) throw new NoSuchElementException
    val res = get(position)
    position += 1
    res
  }

  protected def get(index: Int): A
}
