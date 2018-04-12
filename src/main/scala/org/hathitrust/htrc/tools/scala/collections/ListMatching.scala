package org.hathitrust.htrc.tools.scala.collections

/**
  * Matches the last element in a list
  */
object ::> {
  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  def unapply[A](l: List[A]): Option[(List[A], A)] = l match {
    case Nil => None
    case _ => Some(l.init -> l.last)
  }
}