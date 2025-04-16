package kz.talgat.companions

import scalikejdbc._
import skinny.orm._

object ProjectC extends SkinnyCRUDMapper[ProjectC] {

  override def defaultAlias: Alias[ProjectC] = createAlias("pr")

  override lazy val columns: Seq[String] = autoColumns[ProjectC]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[ProjectC]): ProjectC = autoConstruct(rs, n)
}

case class ProjectC( id: Int,  employerId: Int,  employerWorkLocationId: Option[Int],  code: String,  name: String,  description: Option[String], customValue1: Option[String], customValue2: Option[String], customValue3: Option[String], customValue4: Option[String],  isActive: Boolean,  isValuable: Boolean)
