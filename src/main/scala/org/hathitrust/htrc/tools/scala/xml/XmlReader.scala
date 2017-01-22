package org.hathitrust.htrc.tools.scala.xml

import java.io.Reader
import javax.xml.parsers.DocumentBuilderFactory

import org.w3c.dom.Document
import org.xml.sax.InputSource

import scala.util.Try

trait XmlReader {
  /**
    * Tries to load an XML document from the given reader.
    *
    * @param reader The reader
    * @return Success or Failure depending on the outcome
    */
  def readXml(reader: Reader): Try[Document] = Try {
    val inputSource = new InputSource(reader)
    val documentBuilder = getDocumentBuilderFactory.newDocumentBuilder
    documentBuilder.parse(inputSource)
  }

  protected def getDocumentBuilderFactory: DocumentBuilderFactory = {
    val documentBuilderFactory = DocumentBuilderFactory.newInstance()
    documentBuilderFactory.setNamespaceAware(isXmlReaderNamespaceAware)
    getXmlReaderFeatures.foreach {
      case (name, value) => documentBuilderFactory.setFeature(name, value)
    }

    documentBuilderFactory
  }

  protected def isXmlReaderNamespaceAware: Boolean = false

  protected def getXmlReaderFeatures: Set[(String, Boolean)] =
    Set("http://apache.org/xml/features/nonvalidating/load-external-dtd" -> false)
}
