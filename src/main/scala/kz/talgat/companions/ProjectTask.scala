package kz.talgat.companions

import kz.talgat.models.ProjectTask
import scalikejdbc._
import skinny.orm._

object ProjectTask extends SkinnyCRUDMapper[ProjectTask] {

  override def defaultAlias: Alias[ProjectTask] = createAlias("pt")

  override lazy val columns: Seq[String] = autoColumns[ProjectTask]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[ProjectTask]): ProjectTask = autoConstruct(rs, n)
}
