package org.hathitrust.htrc.tools.scala.io

import scala.collection.compat.immutable._
import java.io.{File, InputStream, OutputStream}
import java.nio.file.attribute.FileAttribute
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util.Scanner
import scala.language.reflectiveCalls
import scala.util.{Try, Using}
import scala.util.matching.Regex

object IOUtils {
  val OSTmpDir: String = System.getProperty("java.io.tmpdir")
  private val BUFFER_SIZE = 16384

  /**
    * Lazily traverses a folder structure and returns all files recursively.
    *
    * @param root       The root of the folder hierarchy to traverse
    * @param skipHidden True to skip hidden files, False to include them
    * @return The stream of discovered files
    */
  @SuppressWarnings(Array("org.wartremover.warts.Null", "org.wartremover.warts.Any"))
  def recurseFileTree(root: File, skipHidden: Boolean = false): LazyList[File] = {
    if (!root.exists || (skipHidden && root.isHidden)) {
      LazyList.empty
    } else {
      root #:: (root.listFiles match {
        case null => LazyList.empty
        case files => files.to(LazyList).flatMap(recurseFileTree(_, skipHidden))
      })
    }
  }

  /**
    * Manages the automated closing of resources
    *
    * @param closeable The closeable resource to manage
    * @param f         Code block to execute once the resource is available
    * @tparam A Code block return type
    * @tparam B Managed resource type
    * @return The result of applying the code block to the resource
    */
  @deprecated("Use scala.util.Using in 2.13 or via scala-collections-compat in 2.12")
  def using[A, B <: {def close() : Unit}](closeable: B)(f: B => A): A =
    try {
      f(closeable)
    }
    finally {
      Try(closeable.close())
    }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  def copy(source: InputStream, sink: OutputStream): Try[Long] = Try {
    val buf = new Array[Byte](BUFFER_SIZE)
    var numRead = 0L
    var n = source.read(buf)
    while (n > 0) {
      sink.write(buf, 0, n)
      numRead += n
      n = source.read(buf)
    }

    numRead
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def saveToTempFile(is: InputStream,
                     prefix: String = null,
                     suffix: String = null,
                     tmpDir: String = OSTmpDir,
                     fileAttributes: List[FileAttribute[_]] = Nil): Try[Path] = Try {
    val tmpPath = Paths.get(tmpDir)
    val tmpFile = Files.createTempFile(tmpPath, prefix, suffix, fileAttributes: _*)

    Using.resource(Files.newOutputStream(tmpFile, StandardOpenOption.WRITE)) { tmpStream =>
      copy(is, tmpStream)
    }

    tmpFile
  }

  def readLinesWithDelimiters(scanner: Scanner, delimiters: Regex = """.*\R|.+\z""".r): Iterator[String] =
    Iterator.continually(scanner.findWithinHorizon(delimiters.pattern, 0)).takeWhile(_ != null)
}
