package kz.talgat.companions

import kz.talgat.models.EmployerPosition
import scalikejdbc._
import skinny.orm._

object EmployerPosition extends SkinnyMapper[EmployerPosition] {

  override def defaultAlias: Alias[EmployerPosition] = createAlias("pos")

  override lazy val columns: Seq[String] = autoColumns[EmployerPosition]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[EmployerPosition]): EmployerPosition = autoConstruct(rs, n)
}
