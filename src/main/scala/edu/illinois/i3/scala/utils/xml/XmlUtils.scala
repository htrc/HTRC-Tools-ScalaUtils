package edu.illinois.i3.scala.utils.xml

import javax.xml.parsers.DocumentBuilderFactory

object XmlUtils {

  def newDocument() = {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()

    builder.newDocument()
  }
}
