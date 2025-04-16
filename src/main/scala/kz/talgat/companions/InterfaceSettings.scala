package kz.talgat.companions

import kz.talgat.models.InterfaceSettings
import scalikejdbc._
import skinny.orm._

object InterfaceSettings extends SkinnyCRUDMapper[InterfaceSettings] {

  override def defaultAlias: Alias[InterfaceSettings] = createAlias("is")

  override lazy val columns: Seq[String] = autoColumns[InterfaceSettings]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[InterfaceSettings]): InterfaceSettings = autoConstruct(rs, n)
}
