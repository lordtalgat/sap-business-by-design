package kz.talgat.companions

import kz.talgat.models.Employee
import scalikejdbc._
import skinny.orm._

object Employee extends SkinnyCRUDMapper[Employee] {

  override def defaultAlias: Alias[Employee] = createAlias("ee")

  override lazy val columns: Seq[String] = autoColumns[Employee]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[Employee]): Employee = autoConstruct(rs, n)
}
