package kz.talgat.dao


import kz.talgat.models.CodeTableDetail
import kz.talgat.companions.{CodeTable => CodeTableC, CodeTableDetail => CodeTableDetailC}
import kz.talgat.util.PredefinedCodeTableDetailValue
import kz.talgat.daos.DAO
import scalikejdbc.{sqls, _}

trait CodeTableDetailDao {

  def getById(id: Int): CodeTableDetail

  def getCode(id: Int): CodeTableDetailCode

  def getByCode(code: String, codeTableCode: String): CodeTableDetail

  def findByCode(code: String, codeTableCode: String): Option[CodeTableDetail]

  def findByCode(code: String, codeTableId: Int): Option[CodeTableDetail]

  def listByCodeTableCode(codeTableCode: String): Seq[CodeTableDetail]

  def listByCodeTableId(codeTableId: Int): Seq[CodeTableDetail]

  def mapByCodeTableName(codeTableCode: String): Map[String, CodeTableDetail]

  def insert(entity: CodeTableDetail): Int

  def insert(entities: Seq[CodeTableDetail]): Seq[Long]

  def insert(codeTableId: Int, predefinedValues: Seq[PredefinedCodeTableDetailValue]): Unit

  def update(entities: Seq[CodeTableDetail]): Unit
}

class CodeTableDetailDaoImpl(protected val dao: DAO)
  extends CodeTableDetailDao
    with DaoImpl {

  def getById(id: Int): CodeTableDetail = localTxQuery { implicit session =>
    CodeTableDetailC
      .findById(id)
      .getOrElse {
        throw new RuntimeException(s"Can't find code table detail by id $id")
      }
  }

  def getCode(id: Int): CodeTableDetailCode = localTxQuery { implicit session =>
    val ctd = CodeTableDetailC.defaultAlias
    val codeOpt = withSQL {
        select(ctd.result.code)
          .from(CodeTableDetailC as ctd)
          .where(sqls.eq(ctd.id, id))
      }.map(_.string(ctd.resultName.code)).first.apply()

    codeOpt.getOrElse {
      throw new RuntimeException(s"Can't find code table detail by id $id")
    }
  }

  def getByCode(code: String, codeTableName: String): CodeTableDetail = localTxQuery { implicit session =>
    val ctd = CodeTableDetailC.defaultAlias
    val ct = CodeTableC.defaultAlias
    val result = withSQL {
      select
        .from(CodeTableDetailC as ctd)
        .innerJoin(CodeTableC as ct).on(ct.id, ctd.codeTableId)
        .where(sqls.eq(ct.name, codeTableName) and sqls.eq(ctd.code, code))
    }.map(CodeTableDetailC(_)).first().apply()

    result.getOrElse {
      throw new RuntimeException(s"Can't find code table detail by code $code for code table $codeTableName")
    }
  }

  def findByCode(code: String, codeTableName: String): Option[CodeTableDetail] = localTxQuery { implicit session =>
    val ctd = CodeTableDetailC.defaultAlias
    val ct = CodeTableC.defaultAlias
    withSQL {
      select
        .from(CodeTableDetailC as ctd)
        .innerJoin(CodeTableC as ct).on(ct.id, ctd.codeTableId)
        .where(sqls.eq(ct.name, codeTableName) and sqls.eq(ctd.code, code))
    }.map(CodeTableDetailC(_)).first().apply()
  }

  def findByCode(code: String, codeTableId: Int): Option[CodeTableDetail] = localTxQuery { implicit session =>
    val ctd = CodeTableDetailC.defaultAlias
    val ct = CodeTableC.defaultAlias
    withSQL {
      select
        .from(CodeTableDetailC as ctd)
        .innerJoin(CodeTableC as ct).on(ct.id, ctd.codeTableId)
        .where(sqls.eq(ct.id, codeTableId) and sqls.eq(ctd.code, code))
    }.map(CodeTableDetailC(_)).first().apply()
  }

  def listByCodeTableCode(codeTableName: String): Seq[CodeTableDetail] = localTxQuery { implicit session =>
    val ctd = CodeTableDetailC.defaultAlias
    val ct = CodeTableC.defaultAlias
    val result = withSQL {
      select
        .from(CodeTableDetailC as ctd)
        .innerJoin(CodeTableC as ct).on(ct.id, ctd.codeTableId)
        .where(sqls.eq(ct.name, codeTableName))
    }.map(CodeTableDetailC(_)).list().apply()

    result
  }

  def listByCodeTableId(codeTableId: Int): Seq[CodeTableDetail] = localTxQuery { implicit session =>
    val ctd = CodeTableDetailC.defaultAlias
    CodeTableDetailC.findAllBy(sqls.eq(ctd.codeTableId, codeTableId))
  }

  def insert(entity: CodeTableDetail): Int = localTxQuery { implicit session =>
    val column = CodeTableDetailC.column

    val id = CodeTableDetailC.createWithNamedValues(
      column.codeTableId -> entity.codeTableId,
      column.code -> entity.code,
      column.description -> entity.description
    )

    id.toInt
  }

  override def mapByCodeTableName(codeTableName: String): Map[String, CodeTableDetail] = localTxQuery { implicit session =>
    val ctd = CodeTableDetailC.defaultAlias
    val ct = CodeTableC.defaultAlias

    withSQL {
      select
        .from(CodeTableDetailC as ctd)
        .innerJoin(CodeTableC as ct).on(ct.id, ctd.codeTableId)
        .where(sqls.eq(ct.name, codeTableName))
    }.map(CodeTableDetailC(_))
      .list()
      .apply()
      .map(ctd => ctd.code -> ctd)
      .toMap
  }

  override def insert(entities: Seq[CodeTableDetail]): Seq[Long] = localTxQuery { implicit session =>
    val column = CodeTableDetailC.column
    val batchParams: Seq[Seq[Any]] = {
      entities.map { entity =>
        Seq(
          entity.codeTableId,
          entity.code,
          entity.description,
          entity.attribute1,
          entity.attribute2,
          entity.attribute3,
          entity.attribute4,
          entity.attribute5,
          entity.isDefault,
          entity.isActive
        )
      }
    }

    withSQL {
      QueryDSL.insert
        .into(CodeTableDetailC)
        .namedValues(
          column.codeTableId -> sqls.?,
          column.code -> sqls.?,
          column.description -> sqls.?,
          column.attribute1 -> sqls.?,
          column.attribute2 -> sqls.?,
          column.attribute3 -> sqls.?,
          column.attribute4 -> sqls.?,
          column.attribute5 -> sqls.?,
          column.isDefault -> sqls.?,
          column.isActive -> sqls.?
        )
    }.batchAndReturnGeneratedKey(batchParams: _*).apply()
  }

  override def update(entities: Seq[CodeTableDetail]): Unit = localTxQuery { implicit session =>
    val column = CodeTableDetailC.column

    def execute(entity: CodeTableDetail) = {
      withSQL {
        QueryDSL.update(CodeTableDetailC).set(
          column.codeTableId -> entity.codeTableId,
          column.code -> entity.code,
          column.description -> entity.description,
          column.attribute1 -> entity.attribute1,
          column.attribute2 -> entity.attribute2,
          column.attribute3 -> entity.attribute3,
          column.attribute4 -> entity.attribute4,
          column.attribute5 -> entity.attribute5,
          column.isDefault -> entity.isDefault,
          column.isActive -> entity.isActive
        ).where.eq(column.id, entity.id)
      }.update.apply()
    }

    entities.foreach(execute)
  }

  override def insert(codeTableId: ProjectId, predefinedValues: Seq[PredefinedCodeTableDetailValue]): Unit = localTxQuery { implicit session =>
    val column = CodeTableDetailC.column
    val batchParams: Seq[Seq[Any]] = {
      predefinedValues.map { predefinedValue =>
        Seq(
          codeTableId,
          predefinedValue.code,
          predefinedValue.description,
          null,
          null,
          null,
          null,
          null,
          predefinedValue.isDefault,
          true
        )
      }
    }

    withSQL {
      QueryDSL.insert
        .into(CodeTableDetailC)
        .namedValues(
          column.codeTableId -> sqls.?,
          column.code -> sqls.?,
          column.description -> sqls.?,
          column.attribute1 -> sqls.?,
          column.attribute2 -> sqls.?,
          column.attribute3 -> sqls.?,
          column.attribute4 -> sqls.?,
          column.attribute5 -> sqls.?,
          column.isDefault -> sqls.?,
          column.isActive -> sqls.?
        )
    }.batch(batchParams: _*).apply()
  }
}
