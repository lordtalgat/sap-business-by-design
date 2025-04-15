package kz.talgat.dao

import kz.talgat.models.{Company, CompanyCustomValues}
import kz.talgat.companions.{CustomField => CustomFieldC, CustomValueEmployer => CustomValueEmployerC}
import kz.talgat.util.Constants.CustomFields
import kz.talgat.daos.DAO
import scalikejdbc.{sqls, _}

trait CustomValueEmployerDao {

  def getCustomValues(employerId: Int): CompanyCustomValues

  def setCustomValues(employerId: Int, company: Company): Unit

  def updateCustomValues(employerId: Int, company: Company): Unit
}

class CustomValueEmployerDaoImpl(protected val dao: DAO)
  extends CustomValueEmployerDao
    with DaoImpl {

  override def getCustomValues(employerId: Int): CompanyCustomValues = localTxQuery { implicit session =>
    val cf = CustomFieldC.defaultAlias
    val cver = CustomValueEmployerC.defaultAlias

    val result = withSQL {
      select
        .from(CustomValueEmployerC as cver)
        .leftJoin(CustomFieldC as cf).on(cf.id, cver.customFieldId)
        .where(sqls.eq(cver.entityId, employerId) and sqls.in(cf.code, CustomFields.AllCodes))
    }.map(e => (e.string(cver.resultName.value), e.string(cf.resultName.code))).list().apply()

    val changeStateId = result.find(_._2 == CustomFields.SAP_CHANGE_STATE_ID_CODE).map(_._1).getOrElse(throw new RuntimeException("Change state not set"))
    val companyID = result.find(_._2 == CustomFields.SAP_COMPANY_ID_CODE).map(_._1).getOrElse(throw new RuntimeException("Company Id not set"))
    val companyUUID = result.find(_._2 == CustomFields.SAP_COMPANY_UUID_CODE).map(_._1).getOrElse(throw new RuntimeException("Company UUID is not set"))

    CompanyCustomValues(employerId = employerId, companyID = companyID, companyUUID = companyUUID, changeStateId = changeStateId)
  }

  override def setCustomValues(employerId: Int, company: Company): Unit = localTxQuery { implicit session =>
    val cf = CustomFieldC.defaultAlias
    val c = CustomValueEmployerC.column

    def mapFieldsForCompany(code: String): String = {
      code match {
        case CustomFields.SAP_CHANGE_STATE_ID_CODE => company.changeStateId.trim
        case CustomFields.SAP_COMPANY_UUID_CODE => company.companyUUID
        case CustomFields.SAP_COMPANY_ID_CODE => company.companyId.toString
        case _ => CustomFields.SAP_NONE
      }
    }

    val customField = withSQL {
      select
        .from(CustomFieldC as cf)
        .where(sqls.in(cf.code, CustomFields.AllCodes))
    }.map(e => (e.string(cf.resultName.id), e.string(cf.resultName.code))).list().apply()

    val batchParams: Seq[Seq[Any]] = customField.map { case (idcustom, code) =>
      Seq(0, idcustom, employerId, mapFieldsForCompany(code))
    }

    withSQL {
      insert.into(CustomValueEmployerC)
        .namedValues(c.id -> sqls.?, c.customFieldId -> sqls.?, c.entityId -> sqls.?, c.value -> sqls.?)
    }.batch(batchParams: _*).apply()
  }

  override def updateCustomValues(employerId: Int, company: Company): Unit = localTxQuery { implicit session =>
    val cf = CustomFieldC.defaultAlias
    val customFieldMap = withSQL {
       select
         .from(CustomFieldC as cf)
         .where(sqls.in(cf.code, CustomFields.AllCodes))
    }.map(e => (e.string(cf.resultName.code), e.int(cf.resultName.id))).list().apply().toMap


    // SAP_COMPANY_UUID_CODE
    createOrUpdateCustomField(
      employerId = employerId,
      customFieldId = customFieldMap(CustomFields.SAP_COMPANY_UUID_CODE),
      value = company.companyUUID
    )

    // SAP_CHANGE_STATE_ID_CODE
    createOrUpdateCustomField(
      employerId = employerId,
      customFieldId = customFieldMap(CustomFields.SAP_CHANGE_STATE_ID_CODE),
      value = company.changeStateId
    )

    // SAP_COMPANY_ID_CODE
    createOrUpdateCustomField(
      employerId = employerId,
      customFieldId = customFieldMap(CustomFields.SAP_COMPANY_ID_CODE),
      value = company.companyId.toString
    )
  }

  private def createOrUpdateCustomField(employerId: Int,
                                        customFieldId: Int,
                                        value: String)
                                       (implicit session: DBSession): Unit = {
    val column = CustomValueEmployerC.column
    val cfer = CustomValueEmployerC.defaultAlias

    val existingCompanyUUIDField = withSQL {
      select(cfer.id)
        .from(CustomValueEmployerC as cfer)
        .where(
          sqls.eq(cfer.customFieldId, customFieldId)
            and sqls.eq(cfer.entityId, employerId)
        )
    }.map(e => e.int(1)).headOption().apply()

    if (existingCompanyUUIDField.isEmpty) {
      CustomValueEmployerC.createWithNamedValues(
        column.customFieldId -> customFieldId,
        column.entityId -> employerId,
        column.value -> value
      )
    } else {
      withSQL {
        QueryDSL.update(CustomValueEmployerC).set(
          column.value -> value
        ).where.eq(column.entityId, employerId).and.eq(column.customFieldId, customFieldId)
      }.update.apply()
    }
  }
}