package kz.talgat.companions

import kz.talgat.models.EmployeeWorkLocation
import scalikejdbc._
import skinny.orm._

object EmployeeWorkLocation extends SkinnyCRUDMapper[EmployeeWorkLocation]{

  override def defaultAlias: Alias[EmployeeWorkLocation] = createAlias("eewl")

  override lazy val columns: Seq[String] = autoColumns[EmployeeWorkLocation]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[EmployeeWorkLocation]): EmployeeWorkLocation = autoConstruct(rs, n)
}
