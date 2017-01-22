package org.hathitrust.htrc.tools.scala.xml

import javax.xml.parsers.DocumentBuilderFactory

import org.w3c.dom.Document

object XmlUtils {

  /**
    * Creates a new XML document
    *
    * @return The new XML document
    */
  def newDocument(): Document = {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()

    builder.newDocument()
  }
}
