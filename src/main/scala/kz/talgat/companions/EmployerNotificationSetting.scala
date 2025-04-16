package kz.talgat.companions

import kz.talgat.models.EmployerNotificationSetting
import scalikejdbc._
import skinny.orm._

object EmployerNotificationSetting extends SkinnyCRUDMapper[EmployerNotificationSetting] {

  override def defaultAlias: Alias[EmployerNotificationSetting] = createAlias("erns")

  override lazy val columns: Seq[String] = autoColumns[EmployerNotificationSetting]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[EmployerNotificationSetting]): EmployerNotificationSetting = autoConstruct(rs, n)
}
