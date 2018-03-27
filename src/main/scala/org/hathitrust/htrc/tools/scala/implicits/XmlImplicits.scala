package org.hathitrust.htrc.tools.scala.implicits

import java.io.{OutputStream, StringWriter, Writer}
import java.util.Properties
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.{OutputKeys, TransformerFactory}

import org.w3c.dom.Document

import scala.io.Codec

object XmlImplicits {

  import org.w3c.dom.{Node, NodeList}

  import scala.language.implicitConversions

  implicit def nodeList2Traversable(nodeList: NodeList): Traversable[Node] = new Traversable[Node] {
    override def foreach[A](process: Node => A): Unit = {
      for (i <- 0 until nodeList.getLength) {
        process(nodeList.item(i))
      }
    }
  }

  implicit class DocumentEx(document: Document) {

    /**
      * Serialize an XML document to an output stream
      *
      * @param outputStream     The output stream
      * @param outputProperties Optional: specific transformer properties to set
      */
    def writeTo(outputStream: OutputStream)(implicit outputProperties: Option[Properties]): Unit = {
      val tf = TransformerFactory.newInstance()

      val transformer = tf.newTransformer()
      outputProperties.foreach(transformer.setOutputProperties(_))

      transformer.transform(new DOMSource(document), new StreamResult(outputStream))
    }

    override def toString: String = toString(indent = true)(Codec.UTF8)

    /**
      * Returns a string representation of this XML document
      *
      * @param indent true to indent output, false otherwise
      * @param codec  Implicit: specifies the codec to use
      * @return The string representation
      */
    def toString(indent: Boolean)(implicit codec: Codec): String = {
      val sw = new StringWriter()

      implicit val outputProperties: Some[Properties] = Some(new Properties())
      outputProperties.foreach { props =>
        props.setProperty(OutputKeys.INDENT, if (indent) "yes" else "no")
        props.setProperty(OutputKeys.ENCODING, codec.name)
      }

      writeTo(sw)

      sw.toString
    }

    /**
      * Serialize an XML document to a writer
      *
      * @param writer           The writer
      * @param outputProperties Optional: specific transformer properties to set
      */
    def writeTo(writer: Writer)(implicit outputProperties: Option[Properties]): Unit = {
      val tf = TransformerFactory.newInstance()

      val transformer = tf.newTransformer()
      outputProperties.foreach(transformer.setOutputProperties(_))

      transformer.transform(new DOMSource(document), new StreamResult(writer))
    }
  }

}
