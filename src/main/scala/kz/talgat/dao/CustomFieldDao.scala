package kz.talgat.dao

import kz.talgat.companions.{CustomField => CustomFieldC}
import kz.talgat.daos.DAO
import scalikejdbc.{sqls, _}

trait CustomFieldDao {

  def listExistingCustomFields(codes: Seq[String]): Seq[String]

  def createCustomField(code: String,
                        label: String,
                        entityId: Int,
                        dataTypeCd: Int,
                        sequenceNumber: Int,
                        codeTableId: Option[Int] = None): Unit
}

class CustomFieldDaoImpl(protected val dao: DAO)
  extends CustomFieldDao
    with DaoImpl {

  override def listExistingCustomFields(codes: Seq[String]): Seq[String] = localTxQuery { implicit session =>
    val cf = CustomFieldC.defaultAlias

    withSQL {
      select(cf.code)
        .from(CustomFieldC as cf)
        .where(
          sqls.in(cf.code, codes)
        )
    }.map(e => e.string(1)).list().apply()
  }

  override def createCustomField(code: String,
                                 label: String,
                                 entityId: Int,
                                 dataTypeCd: Int,
                                 sequenceNumber: Int,
                                 codeTableId: Option[Int] = None): Unit = localTxQuery { implicit session =>
    val c = CustomFieldC.column

    withSQL {
      insert
        .into(CustomFieldC)
        .columns(c.id, c.entityTypeCd, c.label, c.dataTypeCd, c.maximumSize, c.isHidden, c.codeTableId, c.sequenceNumber, c.code)
        .values(0, entityId, label, dataTypeCd, 0, false, codeTableId, sequenceNumber, code)
    }.update.apply()
  }
}
