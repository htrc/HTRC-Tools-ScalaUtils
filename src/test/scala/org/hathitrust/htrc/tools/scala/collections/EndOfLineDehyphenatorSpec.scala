package org.hathitrust.htrc.tools.scala.collections

import java.util.Scanner

import org.hathitrust.htrc.tools.scala.implicits.CollectionsImplicits._
import org.hathitrust.htrc.tools.scala.io.IOUtils.readLinesWithDelimiters
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.io.Source

// TODO write better tests -- these were just quick-and-dirty
class EndOfLineDehyphenatorSpec extends FlatSpec
  with ScalaCheckPropertyChecks with Matchers {
  private val text = "The title.\n\n\nThis is the be-\nginning of a one-of-a-\nkind friendship between ape and hu-\nman.\n   We should always cheer\nfor our best-\nin-class companion animal!\nThis text makes no sense and is con-\ntrieved to test this fea-\nture."
  private val expected = "The title.\n\n\nThis is the beginning\nof a one-of-a-\nkind friendship between ape and human.\n   We should always cheer\nfor our best-\nin-class companion animal!\nThis text makes no sense and is contrieved\nto test this feature."

  "EnfOfLineDehyphenator" should "correctly dehyphenate some sample text" in {
    val it = readLinesWithDelimiters(new Scanner(text))
    val dehyphenator = new EndOfLineDehyphenator(it, "-")
    val dehyphenatedText = dehyphenator.mkString

    dehyphenatedText shouldBe expected
  }

  it should "work when invoked on an iterable" in {
    val lines = Source.fromString(text).getLines().toSeq
    val dehyphenated = lines.dehyphenate()

    Source.fromString(expected).getLines.toList should contain theSameElementsInOrderAs dehyphenated
  }

  it should "work when invoked on an iterator" in {
    val dehyphenated = Source.fromString(text).getLines().dehyphenate()

    assert(Source.fromString(expected).getLines sameElements dehyphenated)
  }

}
