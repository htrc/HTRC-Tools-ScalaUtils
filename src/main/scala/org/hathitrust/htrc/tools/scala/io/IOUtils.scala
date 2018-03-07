package org.hathitrust.htrc.tools.scala.io

import java.io.File
import scala.language.reflectiveCalls

object IOUtils {

  /**
    * Lazily traverses a folder structure and returns all files recursively.
    *
    * @param root The root of the folder hierarchy to traverse
    * @param skipHidden True to skip hidden files, False to include them
    * @return The stream of discovered files
    */
  def recurseFileTree(root: File, skipHidden: Boolean = false): Stream[File] = {
    if (!root.exists || (skipHidden && root.isHidden)) {
      Stream.empty
    } else {
      root #:: (root.listFiles match {
        case null => Stream.empty
        case files => files.toStream.flatMap(recurseFileTree(_, skipHidden))
      })
    }
  }

  def using[A, B <: {def close() : Unit}](closeable: B)(f: B => A): A =
    try {
      f(closeable)
    }
    finally {
      closeable.close()
    }

}
