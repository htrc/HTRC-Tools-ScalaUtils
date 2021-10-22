package com.gilt.gfc.time

import java.lang.{Long => JLong}
import java.sql.{Timestamp => SqlTimestamp}
import java.util.{TimeZone, Date}
import java.text.{SimpleDateFormat, DateFormat}
import scala.beans.BeanProperty

/**
 * Immutable wrapper around a timestamp. Represents time the same as `java.util.Date`.
 */
object Timestamp {
  val IsoFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
  val JsFormat = "EEE, d MMM yyyy HH:mm:ss z"
  private val IsoFormatTls: ThreadLocal[DateFormat] = new ThreadLocal()
  private val JsFormatTls: ThreadLocal[DateFormat] = new ThreadLocal()

  private def getDateFormatter(tls: ThreadLocal[DateFormat], formatStr: String): DateFormat = {
    Option(tls.get).getOrElse {
      val dateFormat = new SimpleDateFormat(formatStr)
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
      tls.set(dateFormat)
      dateFormat
    }
  }

  def getJsDateFormatter: DateFormat = getDateFormatter(JsFormatTls, JsFormat)
  def getIsoDateFormatter: DateFormat = getDateFormatter(IsoFormatTls, IsoFormat)

  private val artificialNow = new ThreadLocal[JLong]
  private def getArtificialNow: Option[Long] = Option(artificialNow.get).map(_.longValue)

  /**
   * FOR TESTING ONLY. Used to replace the clock with a fixed time.
   * @param now time value for "now" to use on this thread
   */
  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def setArtificialNow(now: Long): Unit = {
    if(now == 0) {
      artificialNow.set(null)
    } else {
      artificialNow.set(now)
    }
  }

  def withArtificialNow[T](now: Long)(f: => T): T = {
    try {
      setArtificialNow(now)
      f
    } finally setArtificialNow(0)
  }

  def apply(): Timestamp = new Timestamp()
  def apply(date: Date): Timestamp = new Timestamp(date)
  def apply(timestamp: String): Timestamp = new Timestamp(timestamp)

  def valueOf(timestamp: String): Timestamp = apply(timestamp)
}

case class Timestamp(@BeanProperty time: Long) extends Ordered[Timestamp] {
  def this() = this(Timestamp.getArtificialNow.getOrElse(System.currentTimeMillis))
  def this(date: Date) = this(date.getTime)
  def this(timestamp: String) = this(Timestamp.getJsDateFormatter.parse(timestamp))

  def toDate: Date = new Date(time)

  def toSqlTimestamp: SqlTimestamp = new SqlTimestamp(time)

  override def equals(other: Any): Boolean = other match {
    case ts: Timestamp => ts.time == time
    case _ => false
  }

  override def hashCode: Int = time.##

  override def toString: String = Timestamp.getJsDateFormatter.format(toDate)

  def asString: String = toString

  // Need to override this here explicitly due to a scala compiler bug that generates an (incompatible)
  // "def compareTo(other: AnyRef): Int" if compareTo is provided by the Ordered trait.
  override def compareTo(other: Timestamp): Int = compare(other)

  override def compare(other: Timestamp): Int = {
    // we don't just subtract here, since we have
    // a long, but this returns an int. Rather than
    // risk undefined behavior with big time spans,
    // better to just to be explicit
    if (time < other.time) {
      -1
    } else if (time > other.time) {
      +1
    } else {
      0
    }
  }

  def +(millis: Long): Timestamp = Timestamp(time + millis)
  def -(millis: Long): Timestamp = this + (-millis)
  def unary_- : Timestamp = Timestamp(-time)
  def >(millis: Long): Boolean = time > millis
  def <(millis: Long): Boolean = time < millis
  def >=(millis: Long): Boolean = time >= millis
  def <=(millis: Long): Boolean = time <= millis
  def ==(millis: Long): Boolean = time == millis
  def after(millis: Long): Boolean = this > millis
  def before(millis: Long): Boolean = this < millis

  def +(other: Timestamp): Timestamp = this + other.time
  def -(other: Timestamp): Timestamp = this - other.time
  def after(other: Timestamp): Boolean = this > other
  def before(other: Timestamp): Boolean = this < other

  def +(date: Date): Timestamp = this + date.getTime
  def -(date: Date): Timestamp = this - date.getTime
  def >(date: Date): Boolean = time > date.getTime
  def <(date: Date): Boolean = time < date.getTime
  def >=(date: Date): Boolean = time >= date.getTime
  def <=(date: Date): Boolean = time <= date.getTime
  def ==(date: Date): Boolean = date != null && time == date.getTime
  def after(date: Date): Boolean = this > date
  def before(date: Date): Boolean = this < date
}
