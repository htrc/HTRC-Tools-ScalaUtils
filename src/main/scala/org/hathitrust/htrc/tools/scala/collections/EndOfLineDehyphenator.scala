package org.hathitrust.htrc.tools.scala.collections

import scala.collection.AbstractIterator

/**
  * Dehyphenates a set of lines of text (i.e. joins end-of-line hyphenated words)
  * Loosely follows the rules specified at http://englishplus.com/grammar/00000129.htm
  *
  * @param lines The text lines
  * @param allowedHyphenChars The allowed hyphen characters (used for determining when a word
  *                           is hyphenated or not) (Technical note: these are added to a regular
  *                           expression, so take care to escape things if necessary)
  */
class EndOfLineDehyphenator(lines: Iterator[String], allowedHyphenChars: String = "-‐‑‒–―−") extends AbstractIterator[String] {
  private val hyphenLeftRegex = raw"""((?:^|.*\s)[^$allowedHyphenChars\s]+\p{L})[$allowedHyphenChars](\R?)$$""".r
  private val hyphenRightRegex = raw"""^(\p{L}[^$allowedHyphenChars\s]+)(?:(\h+)|$$)""".r
  private val linePairs = lines.sliding(2)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var lastLine = Option.empty[String]

  /**
    * Dehyphenates two lines of text containing an end-of-line hyphenated word by fusing together
    * the word parts into the first line (and removing the remnants of the hyphenated word from
    * the second line); it loosely follows the hyphenation rules from
    * http://englishplus.com/grammar/00000129.htm
    *
    * @param l1 Line 1
    * @param l2 Line 2
    * @return A `Some` containing the resulting lines after dehyphenation was performed, or `None` if
    *         no dehyphenation was necessary
    */
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  protected def dehyphenate(l1: String, l2: String): Option[(String, String)] = {
    hyphenLeftRegex.findFirstMatchIn(l1).map(m => m.group(1) -> m.group(2)) match {
      case None => None
      case Some((left, l1Eol)) =>
        hyphenRightRegex.findFirstMatchIn(l2).map(m => m.group(1) -> m.group(2)) match {
          case None => None
          case Some((right, spaces)) =>
            val numSpaces = if (spaces == null) 0 else spaces.length
            var eol = l1Eol
            var str = l2.substring(right.length + numSpaces)
            if (str.isEmpty || str.matches("""\R""")) {
              eol = str
              str = ""
            }

            Some((left concat right concat eol, str))
        }
    }
  }

  override def hasNext: Boolean = linePairs.hasNext || lastLine.nonEmpty

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps", "org.wartremover.warts.Var", "org.wartremover.warts.Throw"))
  override def next(): String = {
    if (linePairs.hasNext) {
      linePairs.next() match {
        case _l1 :: _l2 :: Nil =>
          var l1 = lastLine.getOrElse(_l1)
          dehyphenate(l1, _l2) match {
            case Some((l1_, l2_)) =>
              l1 = l1_
              lastLine = Some(l2_).filter(_.nonEmpty) orElse {
                if (linePairs.hasNext)
                  Some(linePairs.next().last)
                else None
              }

            case None =>
              lastLine = Some(_l2)
          }

          l1

        case l1 :: Nil => l1

        case _ => throw new RuntimeException("SHOULD NOT HAPPEN")
      }
    } else {
      @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
      val line = lastLine.get
      lastLine = None
      line
    }
  }
}