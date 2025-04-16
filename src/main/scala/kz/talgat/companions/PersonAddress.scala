package kz.talgat.companions

import kz.talgat.models.PersonAddress
import scalikejdbc._
import skinny.orm._

object PersonAddress extends SkinnyCRUDMapper[PersonAddress] {

  override def defaultAlias: Alias[PersonAddress] = createAlias("pa")

  override lazy val columns: Seq[String] = autoColumns[PersonAddress]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[PersonAddress]): PersonAddress = autoConstruct(rs, n)

  override val nameConverters: Map[String, String] = Map("^address1$" -> "address_1", "^address2$" -> "address_2")
}
