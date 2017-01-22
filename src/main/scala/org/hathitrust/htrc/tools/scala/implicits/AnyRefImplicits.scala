package org.hathitrust.htrc.tools.scala.implicits

object AnyRefImplicits {

  implicit class AnyRefWithNeq(obj: AnyRef) {
    /**
      * Tests whether the argument (`other`) is a reference to the receiver object (`this`)
      *
      * @param other The other object to test against
      * @return true if the argument is a reference to the receiver object; false otherwise
      */
    def neq(other: AnyRef): Boolean = !(obj eq other)
  }

}
