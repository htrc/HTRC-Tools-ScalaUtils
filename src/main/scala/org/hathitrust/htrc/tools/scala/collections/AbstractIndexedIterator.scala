package org.hathitrust.htrc.tools.scala.collections

import scala.collection.AbstractIterator

/**
  * Defines an indexed iterator
  *
  * @param size The size of the collection it wraps
  * @tparam A The type parameter
  */
abstract class AbstractIndexedIterator[+A](override val size: Int) extends AbstractIterator[A] {
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var position = 0

  override def next(): A = {
    require(hasNext, "hasNext must be true")
    val res = get(position)
    position += 1
    res
  }

  override def hasNext: Boolean = position < size

  protected def get(index: Int): A
}
