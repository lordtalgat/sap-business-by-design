package kz.talgat.companions

import kz.talgat.models.CodeTableDetail
import scalikejdbc.{WrappedResultSet, autoColumns, autoConstruct}
import skinny.orm.{Alias, SkinnyCRUDMapper}

object CodeTableDetail extends SkinnyCRUDMapper[CodeTableDetail]{

  override def defaultAlias: Alias[CodeTableDetail] = createAlias("ctd")

  override lazy val columns: Seq[String] = autoColumns[CodeTableDetail]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[CodeTableDetail]): CodeTableDetail = autoConstruct(rs, n)
}
