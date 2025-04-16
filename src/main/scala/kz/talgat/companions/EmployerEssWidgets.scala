package kz.talgat.companions

import kz.talgat.models.EmployerEssWidgets
import scalikejdbc._
import skinny.orm._

object EmployerEssWidgets extends SkinnyCRUDMapper[EmployerEssWidgets] {

  override def defaultAlias: Alias[EmployerEssWidgets] = createAlias("erew")

  override lazy val columns: Seq[String] = autoColumns[EmployerEssWidgets]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[EmployerEssWidgets]): EmployerEssWidgets = autoConstruct(rs, n)
}