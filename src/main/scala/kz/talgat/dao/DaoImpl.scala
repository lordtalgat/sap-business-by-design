package kz.talgat.dao

import java.sql
import java.sql.Connection

import com.criterionhcm.apps.dao.DAO
import com.criterionhcm.apps.exceptions.AppException
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc.{DB, DBSession}

import cats.syntax.either._

trait DaoImpl extends StrictLogging {

  def localTxQuery[T](f: DBSession => T): T = {
    var result: Either[Throwable, T] = new AppException("Unexpected exception running query").asLeft

    dao.withConnection(
      (conn: sql.Connection) => {
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED)
        DB(conn).localTx { session =>
          result = Either.catchNonFatal(f(session))
        }
      }
    )

    result match {
      case Right(value) => value
      case Left(ex) =>
        logger.info(ex.getMessage)
        throw ex
    }
  }

  protected def dao: DAO
}