package org.hathitrust.htrc.tools.scala.implicits

object SqlImplicits {

  import java.sql.{Connection, Statement}

  import scala.util.Try

  implicit class StatementEx(stmt: Statement) {
    /**
      * Tries to close a Statement object.
      *
      * @return The Try object representing either Success or Failure depending on the outcome
      */
    def tryClose: Try[Unit] = Try(stmt.close())
  }

  implicit class ConnectionEx(conn: Connection) {
    /**
      * Tries to roll back a transaction.
      *
      * @return The Try object representing either Success or Failure depending on the outcome
      */
    def tryRollback: Try[Unit] = Try(conn.rollback())

    /**
      * Tries to close multiple Statements.
      *
      * @return The Try object representing either Success or Failure depending on the outcome
      */
    def tryRelease(stmts: Statement*): Try[Unit] = {
      stmts.foreach(_.tryClose)
      Try(conn.close())
    }
  }

}


object RichSQL {

  import java.sql._

  import scala.annotation.tailrec

  private def strm[X](f: RichResultSet => X, rs: ResultSet): Stream[X] = {
    if (rs.next) {
      Stream.cons(f(new RichResultSet(rs)), strm(f, rs))
    } else {
      rs.close()
      Stream.empty
    }
  }

  implicit def query[X](s: String, f: RichResultSet => X)(implicit stat: Statement): Stream[X] = {
    strm(f, stat.executeQuery(s))
  }

  implicit def conn2Statement(conn: Connection): Statement = conn.createStatement

  implicit def rrs2Boolean(rs: RichResultSet): Option[Boolean] = rs.nextBoolean

  implicit def rrs2Byte(rs: RichResultSet): Option[Byte] = rs.nextByte

  implicit def rrs2Int(rs: RichResultSet): Option[Int] = rs.nextInt

  implicit def rrs2Long(rs: RichResultSet): Option[Long] = rs.nextLong

  implicit def rrs2Float(rs: RichResultSet): Option[Float] = rs.nextFloat

  implicit def rrs2Double(rs: RichResultSet): Option[Double] = rs.nextDouble

  implicit def rrs2String(rs: RichResultSet): Option[String] = rs.nextString

  implicit def rrs2Date(rs: RichResultSet): Option[Date] = rs.nextDate

  implicit def resultSet2Rich(rs: ResultSet): RichResultSet = new RichResultSet(rs)

  implicit def rich2ResultSet(r: RichResultSet): ResultSet = r.rs


  /**
    * Wrapper class for ResultSet.
    * Note: NOT thread safe
    *
    * @param rs The wrapped ResultSet object
    */
  class RichResultSet(val rs: ResultSet) {

    private var pos = 1

    def apply(i: Int): RichResultSet = {
      pos = i
      this
    }

    def nextBoolean: Option[Boolean] = {
      val ret = rs.getBoolean(pos)
      pos += 1
      if (rs.wasNull) {
        None
      } else {
        Some(ret)
      }
    }

    def nextByte: Option[Byte] = {
      val ret = rs.getByte(pos)
      pos += 1
      if (rs.wasNull) {
        None
      } else {
        Some(ret)
      }
    }

    def nextInt: Option[Int] = {
      val ret = rs.getInt(pos)
      pos += 1
      if (rs.wasNull) {
        None
      } else {
        Some(ret)
      }
    }

    def nextLong: Option[Long] = {
      val ret = rs.getLong(pos)
      pos += 1
      if (rs.wasNull) {
        None
      } else {
        Some(ret)
      }
    }

    def nextFloat: Option[Float] = {
      val ret = rs.getFloat(pos)
      pos += 1
      if (rs.wasNull) {
        None
      } else {
        Some(ret)
      }
    }

    def nextDouble: Option[Double] = {
      val ret = rs.getDouble(pos)
      pos += 1
      if (rs.wasNull) {
        None
      } else {
        Some(ret)
      }
    }

    def nextString: Option[String] = {
      val ret = rs.getString(pos)
      pos += 1
      if (rs.wasNull) {
        None
      } else {
        Some(ret)
      }
    }

    def nextDate: Option[Date] = {
      val ret = rs.getDate(pos)
      pos += 1
      if (rs.wasNull) {
        None
      } else {
        Some(ret)
      }
    }

    @tailrec
    final def foldLeft[X](init: X)(f: (ResultSet, X) => X): X = {
      if (rs.next) {
        foldLeft(f(rs, init))(f)
      } else {
        init
      }
    }

    def map[X](f: ResultSet => X): IndexedSeq[X] = {
      var ret = Vector[X]()
      while (rs.next())
        ret :+= f(rs)

      ret
    }
  }

  implicit def ps2Rich(ps: PreparedStatement): RichPreparedStatement = new RichPreparedStatement(ps)

  implicit def rich2PS(r: RichPreparedStatement): PreparedStatement = r.ps

  implicit def str2RichPrepared(s: String)(implicit conn: Connection): RichPreparedStatement =
    conn.prepareStatement(s)

  implicit def conn2Rich(conn: Connection): RichConnection = new RichConnection(conn)

  implicit def st2Rich(s: Statement): RichStatement = new RichStatement(s)

  implicit def rich2St(rs: RichStatement): Statement = rs.s


  class RichPreparedStatement(val ps: PreparedStatement) {
    private var pos = 1

    def <<![X](f: RichResultSet => X): Stream[X] = execute(f)

    def execute[X](f: RichResultSet => X): Stream[X] = {
      pos = 1
      strm(f, ps.executeQuery)
    }

    def <<! : Boolean = execute

    def execute: Boolean = {
      pos = 1
      ps.execute
    }

    def <<(x: Option[Any]): RichPreparedStatement = {
      x match {
        case None =>
          ps.setNull(pos, Types.NULL)
          inc
        case Some(y) => this << y
      }
    }

    def <<(x: Any): RichPreparedStatement = {
      x match {
        case z: Boolean =>
          ps.setBoolean(pos, z)
        case z: Byte =>
          ps.setByte(pos, z)
        case z: Int =>
          ps.setInt(pos, z)
        case z: Long =>
          ps.setLong(pos, z)
        case z: Float =>
          ps.setFloat(pos, z)
        case z: Double =>
          ps.setDouble(pos, z)
        case z: String =>
          ps.setString(pos, z)
        case z: Date =>
          ps.setDate(pos, z)
        case z => ps.setObject(pos, z)
      }
      inc
    }

    private def inc: RichPreparedStatement = {
      pos = pos + 1
      this
    }
  }


  class RichConnection(val conn: Connection) {
    def <<(sql: String): RichStatement = new RichStatement(conn.createStatement) << sql

    def <<(sql: Seq[String]): RichStatement = new RichStatement(conn.createStatement) << sql
  }


  class RichStatement(val s: Statement) {
    def <<(sql: String): RichStatement = {
      s.execute(sql)
      this
    }

    def <<(sql: Seq[String]): RichStatement = {
      for (x <- sql) {
        s.execute(x)
      }
      this
    }
  }

}