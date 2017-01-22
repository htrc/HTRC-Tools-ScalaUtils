package org.hathitrust.htrc.tools.scala.implicits

import scala.util.{Either, Failure, Left, Right, Success, Try}

object GeneralImplicits {

  implicit class EitherEx[L <: Throwable, R](either: Either[L, R]) {
    /**
      * Converts Either to Try
      *
      * @return The Try
      */
    def toTry: Try[R] = either match {
      case Right(obj) => Success(obj)
      case Left(err) => Failure(err)
    }
  }

  implicit class TryEx[T](t: Try[T]) {
    /**
      * Converts Try to Either
      * @return The Either
      */
    def toEither: Either[Throwable, T] = t match {
      case Success(something) => Right(something)
      case Failure(err) => Left(err)
    }
  }

}
