package org.hathitrust.htrc.tools.scala.implicits

object RegexImplicits {

  import java.util.regex.Matcher

  implicit class MatcherEx(matcher: Matcher) {
    /**
      * Returns the number of matches for this regular expression on this input.
      *
      * @return The number of matches
      */
    def findCount: Int = {
      var count = 0
      while (matcher.find()) count += 1
      count
    }
  }

}
