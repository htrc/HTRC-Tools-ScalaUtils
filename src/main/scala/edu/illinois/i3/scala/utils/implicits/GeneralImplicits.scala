package edu.illinois.i3.scala.utils.implicits

import scala.util.{ Either, Failure, Left, Right, Success, Try }

object GeneralImplicits {

  implicit class EitherEx[L <: Throwable, R](either: Either[L, R]) {
    def toTry: Try[R] = either match {
      case Right(obj) => Success(obj)
      case Left(err) => Failure(err)
    }
  }

  implicit class TryEx[T](t: Try[T]) {
    def toEither: Either[Throwable, T] = t match {
      case Success(something) => Right(something)
      case Failure(err) => Left(err)
    }
  }

}
