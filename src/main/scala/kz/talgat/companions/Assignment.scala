package kz.talgat.companions

import kz.talgat.models.Assignment
import scalikejdbc._
import skinny.orm._

object Assignment extends SkinnyCRUDMapper[Assignment] {
  override def defaultAlias: Alias[Assignment] = createAlias("a")

  override lazy val columns: Seq[String] = autoColumns[Assignment]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[Assignment]): Assignment = autoConstruct(rs, n)
}
