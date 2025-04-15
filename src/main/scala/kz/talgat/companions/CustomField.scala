package kz.talgat.companions

import kz.talgat.models.CustomField
import scalikejdbc._
import skinny.orm._

object CustomField extends SkinnyMapper[CustomField] {
  override def defaultAlias: Alias[CustomField] = ???

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[CustomField]): CustomField = ???
}
