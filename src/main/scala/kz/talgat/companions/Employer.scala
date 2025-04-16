package kz.talgat.companions

import kz.talgat.models.Employer
import scalikejdbc._
import skinny.orm._

object Employer extends SkinnyMapper[Employer] {

  override def defaultAlias: Alias[Employer] = createAlias("er")

  override lazy val columns: Seq[String] = autoColumns[Employer]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[Employer]): Employer =  autoConstruct(rs, n)
}
