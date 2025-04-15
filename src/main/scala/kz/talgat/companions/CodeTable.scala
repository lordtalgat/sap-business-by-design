package kz.talgat.companions

import kz.talgat.models.CodeTable
import scalikejdbc.{WrappedResultSet, autoColumns, autoConstruct}
import skinny.orm.{Alias, SkinnyCRUDMapper}

object CodeTable extends SkinnyCRUDMapper[CodeTable]{

  override lazy val defaultAlias: Alias[CodeTable] = createAlias("ct")

  override lazy val columns: Seq[String] = autoColumns[CodeTable]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[CodeTable]): CodeTable = autoConstruct(rs, n)
}
