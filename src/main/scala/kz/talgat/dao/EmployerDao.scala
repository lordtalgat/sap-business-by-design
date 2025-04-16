package kz.talgat.dao

import kz.talgat.daos.DAO
import kz.talgat.models.{Employer, EmployerEssWidgets, EmployerPayrollSettingConstants, PayrollImport}
import kz.talgat.companions.{CodeTable => CodeTableC, CodeTableDetail => CodeTableDetailC, CustomField => CustomFieldC, CustomValueEmployer => CustomValueEmployerC, Employer => EmployerC, EmployerEssWidgets => EmployerEssWidgetsC, EmployerNotificationSetting => EmployerNotificationSettingC, EmployerPayrollSetting => EmployerPayrollSettingC, InterfaceSettings => InterfaceSettingsC}
import kz.talgat.util.Constants.CustomFields
import scalikejdbc.{sqls, _}

trait EmployerDao {

  def list(): Seq[Employer]

  def listAll(): Seq[Employer]

  def getById(employerId: Int): Option[Employer]

  def getListByNames(names: List[String]): List[String]

  def create(companyName: String): Int
}

class EmployerDaoImpl(protected val dao: DAO)
  extends EmployerDao
    with DaoImpl {

  override def list(): Seq[Employer] = localTxQuery { implicit session =>
    // only employers with all data needed
    // since we transfer all settings in their custom fields
    val er = EmployerC.defaultAlias
    val cf = CustomFieldC.defaultAlias
    val cver = CustomValueEmployerC.defaultAlias

    withSQL {
      select(sqls.distinct(er.result.*))
        .from(EmployerC as er)
        .leftJoin(CustomValueEmployerC as cver).on(cver.entityId, er.id)
        .leftJoin(CustomFieldC as cf).on(cf.id, cver.customFieldId)
        .where(sqls.in(cf.code, CustomFields.AllCodes))
        .groupBy(er.id)
        .having(sqls.eq(sqls.count(er.id), CustomFields.AllCodes.size))
        .orderBy(er.id asc)
    }.map(EmployerC(_)).list().apply()
  }

  override def listAll(): Seq[Employer] = localTxQuery { implicit session =>
    EmployerC.findAll()
  }

  override def getById(employerId: Int): Option[Employer] = localTxQuery { implicit session =>
    val er = EmployerC.defaultAlias

    val sql = withSQL {
      select
        .from(EmployerC as er)
        .where(sqls.eq(er.id, employerId))
    }

    sql
      .map(EmployerC(_))
      .first()
      .apply()
  }

  override def create(companyName: String): Int = localTxQuery { implicit session =>
    val ct = CodeTableC.defaultAlias
    val ctd = CodeTableDetailC.defaultAlias

    val erColumn = EmployerC.column
    val erpsColumn = EmployerPayrollSettingC.column
    val isColumn = InterfaceSettingsC.column
    val eresswColumn = EmployerEssWidgetsC.column
    val ernsColumn = EmployerNotificationSettingC.column

    // Taking predefined default values
    val notificationEventTypeIds = withSQL{
      select(ctd.id)
        .from(CodeTableC as ct)
        .innerJoin(CodeTableDetailC as ctd).on(ct.id, ctd.codeTableId)
        .where(
          sqls.eq(ct.name, "EVENT_TYPE")
        )
    }.map(e => e.int(1)).list().apply()

    val defaultTaxEngine = withSQL {
      select(ctd.id)
        .from(CodeTableC as ct)
        .innerJoin(CodeTableDetailC as ctd).on(ct.id, ctd.codeTableId)
        .where(
          sqls.eq(ct.name, "TAX_ENGINE")
            and sqls.eq(ctd.code, "US_CANADA_TE")
        )
    }.map(e => e.int(1)).headOption().apply()

    val t4InterfaceSettings = withSQL {
      select(ctd.id)
        .from(CodeTableC as ct)
        .innerJoin(CodeTableDetailC as ctd).on(ct.id, ctd.codeTableId)
        .where(
          sqls.eq(ct.name, "ENTITY_TYPE")
            and sqls.eq(ctd.code, "PAYROLL_INTERFACE_T4_SETTINGS")
        )
    }.map(e => e.int(1)).headOption().apply()

    // Inserting employer record
    val employerId = withSQL {
      insertInto(EmployerC)
        .columns(erColumn.legalName)
        .values(companyName)
    }.updateAndReturnGeneratedKey.apply().toInt

    // Inserting payroll settings record
    withSQL {
      insertInto(EmployerPayrollSettingC)
        .namedValues(
          erpsColumn.employerId -> employerId,
          erpsColumn.importFileFormat -> PayrollImport.DEFAULT_FORMAT.toString(),
          erpsColumn.taxEngineCd -> defaultTaxEngine,
          erpsColumn.formatIncome -> EmployerPayrollSettingConstants.FormatIncome.CODE
        )
    }.update().apply()

    // Inserting interface settings record
    withSQL {
      insertInto(InterfaceSettingsC)
        .namedValues(
          isColumn.employerId -> employerId,
          isColumn.interfaceCd -> t4InterfaceSettings
        )
    }.update().apply()

    // Inserting employer ess widgets record
    withSQL {
      insertInto(EmployerEssWidgetsC)
        .namedValues(
          eresswColumn.employerId -> employerId,
          eresswColumn.widgets -> EmployerEssWidgets.allWidgets
        )
    }.update().apply()

    // Inserting employer notification settings record
    notificationEventTypeIds.foreach { notificationEventTypeId =>
      withSQL {
        insertInto(EmployerNotificationSettingC)
          .namedValues(
            ernsColumn.employerId -> employerId,
            ernsColumn.eventTypeCd -> notificationEventTypeId,
            ernsColumn.isActive -> true,
            ernsColumn.isEmail -> true,
            ernsColumn.isSMS -> false,
            ernsColumn.isESS -> false
          )
      }.update().apply()
    }

    employerId
  }

  override def getListByNames(names: List[String]): List[String] = localTxQuery { implicit session =>
    val er = EmployerC.defaultAlias

    withSQL {
      select
        .from(EmployerC as er)
        .where(sqls.in(er.legalName, names))
    }
      .map(EmployerC(_).legalName)
      .list()
      .apply()
  }
}