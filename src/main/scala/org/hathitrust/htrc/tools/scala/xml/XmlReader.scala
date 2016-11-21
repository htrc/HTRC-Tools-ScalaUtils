package org.hathitrust.htrc.tools.scala.xml

import java.io.Reader
import javax.xml.parsers.DocumentBuilderFactory

import org.xml.sax.InputSource

import scala.util.Try

trait XmlReader {
  def isXmlReaderNamespaceAware = false
  def getXmlReaderFeatures = Set("http://apache.org/xml/features/nonvalidating/load-external-dtd" -> false)

  protected def getDocumentBuilderFactory() = {
    val documentBuilderFactory = DocumentBuilderFactory.newInstance()
    documentBuilderFactory.setNamespaceAware(isXmlReaderNamespaceAware)
    getXmlReaderFeatures.foreach { case (name, value) => documentBuilderFactory.setFeature(name, value)}

    documentBuilderFactory
  }

  def readXml(reader: Reader) = Try {
    val inputSource = new InputSource(reader)
    val documentBuilder = getDocumentBuilderFactory().newDocumentBuilder
    documentBuilder.parse(inputSource)
  }
}
