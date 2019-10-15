package org.hathitrust.htrc.tools.scala.implicits

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object FutureImplicits {


  implicit class OptionFutureEx[T](of: Option[Future[T]]) {
    /**
      * Converts a Option[ Future[T] ] into a Future[ Option[T] ]
      * @return
      */
    def toFutureOption(implicit ec: ExecutionContext): Future[Option[T]] = of match {
      case Some(f) => f.map(Some(_))
      case None => Future.successful(None)
    }
  }

  implicit class FutureEx[T](f: Future[T]) {
    def toFutureTry()(implicit ec: ExecutionContext): Future[Try[T]] =
      f.map(Success(_)).recover { case e => Failure(e) }


  }

}
