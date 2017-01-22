package org.hathitrust.htrc.tools.scala.metrics

object Timer {

  /**
    * Executes the given code block and prints the elapsed time.
    *
    * @param block The block to execute
    * @tparam R The type of the result returned by the code block
    * @return The result of running the code block
    */
  def printElapsedTime[R](block: => R): R = {
    val t0 = System.currentTimeMillis()
    val result = block
    val t1 = System.currentTimeMillis()
    val elapsed = t1 - t0
    println(f"Elapsed time: $elapsed%,d ms")
    result
  }

  /**
    * Executes the given code block and returns the result and the elapsed time.
    *
    * @param block The code block
    * @tparam R The return type of the code block
    * @return A tuple containing the return value of the executed block and
    *         the elapsed time in milliseconds
    */
  def time[R](block: => R): (R, Long) = {
    val t0 = System.currentTimeMillis()
    val result = block
    val t1 = System.currentTimeMillis()
    (result, t1 - t0)
  }

}
