package kz.talgat.companions

import kz.talgat.models.EmployerTask
import scalikejdbc._
import skinny.orm._

object EmployerTask  extends SkinnyCRUDMapper[EmployerTask] {

  override def defaultAlias: Alias[EmployerTask] = createAlias("et")

  override lazy val columns: Seq[String] = autoColumns[EmployerTask]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[EmployerTask]): EmployerTask = autoConstruct(rs, n)
}
