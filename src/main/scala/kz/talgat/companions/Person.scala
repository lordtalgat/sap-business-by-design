package kz.talgat.companions

import kz.talgat.models.Person
import scalikejdbc._
import skinny.orm._

object Person extends SkinnyCRUDMapper[Person] {

  override def defaultAlias: Alias[Person] = createAlias("per")

  override lazy val columns: Seq[String] = autoColumns[Person]()

  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[Person]): Person = autoConstruct(rs, n)
}
