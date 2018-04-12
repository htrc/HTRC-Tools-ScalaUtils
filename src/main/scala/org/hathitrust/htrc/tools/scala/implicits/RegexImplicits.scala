package org.hathitrust.htrc.tools.scala.implicits

object RegexImplicits {

  import java.util.regex.Matcher

  implicit class MatcherEx(matcher: Matcher) {
    /**
      * Returns the number of matches for this regular expression on this input.
      *
      * @return The number of matches
      */
    def findCount: Int = Iterator.continually(matcher.find()).takeWhile(identity).size
  }

}
