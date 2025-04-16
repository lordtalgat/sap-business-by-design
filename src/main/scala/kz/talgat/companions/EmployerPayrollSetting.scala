package kz.talgat.companions

import kz.talgat.models.EmployerPayrollSetting
import scalikejdbc._
import skinny.orm._

object EmployerPayrollSetting extends SkinnyCRUDMapper[EmployerPayrollSetting] {

  override def defaultAlias: Alias[EmployerPayrollSetting] = createAlias("ps")

  override lazy val columns: Seq[String] = autoColumns[EmployerPayrollSetting]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[EmployerPayrollSetting]): EmployerPayrollSetting = autoConstruct(rs, n)
}
