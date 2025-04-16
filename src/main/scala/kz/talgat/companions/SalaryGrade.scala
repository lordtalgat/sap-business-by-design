package kz.talgat.companions

import kz.talgat.models.SalaryGrade
import scalikejdbc._
import skinny.orm._

object SalaryGrade extends SkinnyCRUDMapper[SalaryGrade] {

  override def defaultAlias: Alias[SalaryGrade] = createAlias("sg")

  override lazy val columns: Seq[String] = autoColumns[SalaryGrade]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[SalaryGrade]): SalaryGrade = autoConstruct(rs, n)
}
