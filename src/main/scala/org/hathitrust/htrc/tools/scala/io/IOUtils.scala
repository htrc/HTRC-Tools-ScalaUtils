package org.hathitrust.htrc.tools.scala.io

import java.io.File

object IOUtils {

  def recurseFileTree(root: File, skipHidden: Boolean = false): Stream[File] =
    if (!root.exists || (skipHidden && root.isHidden))
      Stream.empty
    else
      root #:: (root.listFiles match {
        case null => Stream.empty
        case files => files.toStream.flatMap(recurseFileTree(_, skipHidden))
      })

}
