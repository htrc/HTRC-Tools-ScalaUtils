package edu.illinois.i3.scala.utils.collections

/**
 * Matches the last element in a list
 */
object ::> { def unapply[A] (l: List[A]) = Some((l.init, l.last)) }