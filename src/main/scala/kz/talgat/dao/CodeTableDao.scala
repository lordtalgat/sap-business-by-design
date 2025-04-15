package kz.talgat.dao

import kz.talgat.models.CodeTable
import kz.talgat.companions.{CodeTable => CodeTableC}
import kz.talgat.daos.DAO
import scalikejdbc.{sqls, _}

trait CodeTableDao {

  def getByName(name: String): CodeTable

  def findByName(name: String): Option[CodeTable]

  def getById(id: Int): CodeTable

  def getListByNames(codeTableId: Int, codes: List[String]): List[String]

  def findById(id: Int): Option[CodeTable]

  def insert(name: String, description: String): Int

  def listByNames(names: Seq[String]): Seq[(String, Int)]
}

class CodeTableDaoImpl(protected val dao: DAO)
  extends CodeTableDao
    with DaoImpl {

  override def getByName(name: String): CodeTable = localTxQuery { implicit session =>
    findByName(name).getOrElse(throw new RuntimeException(s"CodeTable(name = $name) is not found"))
  }

  override def findByName(name: String): Option[CodeTable] = localTxQuery { implicit session =>
    val ct = CodeTableC.defaultAlias
    CodeTableC.findBy(sqls.eq(ct.name, name))
  }

  def getById(id: Int): CodeTable = localTxQuery { implicit session =>
    findById(id).getOrElse(throw new RuntimeException(s"CodeTable(id = $id) is not found"))
  }

  def findById(id: Int): Option[CodeTable] = localTxQuery { implicit session =>
    CodeTableC.findById(id)
  }

  override def insert(name: String, description: String): Int = localTxQuery { implicit session =>
    val column = CodeTableC.column

    val id = CodeTableC.createWithNamedValues(
      column.name -> name,
      column.description -> description,
      column.isSystem -> false,
      column.isCustom -> true,
      column.attribute1Caption -> null,
      column.attribute2Caption -> null,
      column.attribute3Caption -> null,
      column.attribute4Caption -> null,
      column.attribute5Caption -> null
    )

    id.toInt
  }

  // TODO: need to review why do we need this
  override def getListByNames(codeTableId: ProjectId, names: List[String]): List[String] = localTxQuery { implicit session =>
    val ct = CodeTableC.defaultAlias
    withSQL {
      select
        .from(CodeTableC as ct)
        .where(
          sqls.in(ct.name, names)
        )
    }.map(CodeTableC(_).name).toList().apply()

  }

  override def listByNames(names: Seq[String]): Seq[(String, Int)] = localTxQuery { implicit session =>
    val ct = CodeTableC.defaultAlias

    withSQL {
      select(ct.name, ct.id)
        .from(CodeTableC as ct)
        .where(sqls.in(ct.name, names))
    }.map { e =>
      val name = e.string(1)
      val id = e.int(2)
      name -> id
    }.list().apply()
  }
}
