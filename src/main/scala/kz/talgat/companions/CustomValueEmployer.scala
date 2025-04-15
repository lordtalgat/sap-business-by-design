package kz.talgat.companions

import kz.talgat.models.CustomValue
import scalikejdbc.{WrappedResultSet, autoColumns, autoConstruct}
import skinny.orm.{Alias, SkinnyCRUDMapper}

object CustomValueEmployer  extends SkinnyCRUDMapper[CustomValue]{

  override def defaultAlias: Alias[CustomValue] = createAlias("cv_er")

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[CustomValue]): CustomValue = autoConstruct(rs, n)

  override lazy val columns: Seq[String] = autoColumns[CustomValue]()

}
