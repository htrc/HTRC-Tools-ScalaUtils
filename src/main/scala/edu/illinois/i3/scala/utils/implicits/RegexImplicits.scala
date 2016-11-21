package edu.illinois.i3.scala.utils.implicits


object RegexImplicits {
  import java.util.regex.Matcher

  implicit class MatcherEx(matcher: Matcher) {
    def findCount = {
      var count = 0
      while (matcher.find()) count += 1
      count
    }
  }
}
