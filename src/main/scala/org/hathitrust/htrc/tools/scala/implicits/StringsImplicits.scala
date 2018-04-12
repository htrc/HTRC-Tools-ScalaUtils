package org.hathitrust.htrc.tools.scala.implicits

import org.hathitrust.htrc.tools.java.metrics.StringMetrics

object StringsImplicits {

  implicit class Regex(sc: StringContext) {
    /**
      * Enables use of regex in pattern matching<br>
      * Example:  "test 123" match { case r"""\w+\s(\d+)\\$num""" => println(s"num is: \\$num") }
      *
      * @return A regex interpolator that can be used for extracting regex matches
      */
    def r: scala.util.matching.Regex =
      new util.matching.Regex(sc.parts.mkString, sc.parts.drop(1).map(_ => "x"): _*)
  }

  implicit class StringEx(s: String) {
    /**
      * Calculates the (Levenshtein) edit distance between two strings<br>
      * credit: https://gist.github.com/tixxit/1246894
      *
      * @param other The other string
      * @return
      */
    def editDistance(other: String): Int = StringMetrics.levenshteinDistance(s, other)

    /**
      * Returns a string quoted with the specified `quoteChar`
      *
      * @param quoteChar The quote character (default is double-quote)
      * @return The quoted string
      */
    def quoted(quoteChar: Char = '"'): String = {
      val s2 = s.replaceAllLiterally(quoteChar.toString, """\""" + quoteChar.toString)
      s"$quoteChar$s2$quoteChar"
    }

    /**
      * Returns a snippet of text (up to a specified maximum length) followed by ellipses
      * if the text is longer than the displayed snippet.
      *
      * @param n        The maximum length of the snippet
      * @param ellipsis The ellipsis text to use (default: ...)
      * @return The snippet
      */
    def snippet(n: Int = 30, ellipsis: String = "..."): String = {
      if (s.length > n) {
        s take (n - ellipsis.length) concat ellipsis
      } else {
        s
      }
    }

  }

}
