package org.hathitrust.htrc.tools.scala.implicits

import java.util.StringTokenizer

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
      val s2 = s.replace(quoteChar.toString, """\""" + quoteChar.toString)
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

    /**
      * Tokenizes a string around the specified delimiters.
      * The delimiters are not returned as tokens.
      *
      * @param delim The delimiters
      * @return The tokens found
      */
    def tokenize(delim: String): Iterator[String] = {
      val st = new StringTokenizer(s, delim)
      Iterator.continually(st).takeWhile(_.hasMoreTokens).map(_.nextToken())
    }

    def takeRightWhile(p: Char => Boolean): String = s.drop(s.lastIndexWhere(!p(_)) + 1)

    /**
      * Returns a new string built by taking every n'th character of the original string
      *
      * @param n The skip value
      * @return A new string built by taking every n'th character of the original string
      */
    def takeEvery(n: Int): String = {
      require(n >= 0)
      (0 until s.length by n).map(s.charAt).mkString
    }
  }

}
