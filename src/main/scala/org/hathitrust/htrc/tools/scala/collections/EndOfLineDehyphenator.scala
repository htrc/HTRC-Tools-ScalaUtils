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
  private val hyphenLeftRegex = raw"""((?:^|.*\s)[^$allowedHyphenChars\s]+\p{L})[$allowedHyphenChars]$$""".r
  private val hyphenRightRegex = raw"""^(\p{L}[^$allowedHyphenChars\s]+)(\s+|$$)""".r
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
    * @return The resulting lines after dehyphenation was performed, or the original lines if
    *         no dehyphenation was necessary
    */
  protected def dehyphenate(l1: String, l2: String): (String, String) = {
    hyphenLeftRegex.findFirstMatchIn(l1).map(_.group(1)) match {
      case None => (l1, l2)
      case Some(left) =>
        hyphenRightRegex.findFirstMatchIn(l2).map(m => m.group(1) -> m.group(2).length) match {
          case None => (l1, l2)
          case Some((right, numSpaces)) => (left concat right, l2.substring(right.length + numSpaces))
        }
    }
  }

  override def hasNext: Boolean = linePairs.hasNext || lastLine.nonEmpty

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps", "org.wartremover.warts.Throw"))
  override def next(): String = {
    if (linePairs.hasNext) {
      linePairs.next() match {
        case _l1 :: _l2 :: Nil =>
          val (l1, l2) = dehyphenate(lastLine.getOrElse(_l1), _l2)
          lastLine = Some(l2).filter(_.nonEmpty) orElse {
            if (linePairs.hasNext)
              Some(linePairs.next().last)
            else None
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