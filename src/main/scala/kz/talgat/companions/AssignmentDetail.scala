package kz.talgat.companions

import kz.talgat.models.AssignmentDetail
import skinny.orm.SkinnyCRUDMapper
import scalikejdbc._
import skinny.orm._

object AssignmentDetail extends SkinnyCRUDMapper[AssignmentDetail] {

  override def defaultAlias: Alias[AssignmentDetail] = createAlias("ad")

  override lazy val columns: Seq[String] = autoColumns[AssignmentDetail]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[AssignmentDetail]): AssignmentDetail = autoConstruct(rs, n)
}
