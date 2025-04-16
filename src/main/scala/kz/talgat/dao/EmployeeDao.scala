package kz.talgat.dao

import java.time.LocalDate
import kz.talgat.helpers._
import kz.talgat.models.{Employee, EmployeeHiring, EmployeeTermination}
import kz.talgat.companions.{Assignment => AssignmentC, AssignmentDetail => AssignmentDetailC, CodeTableDetail => CodeTableDetailC, Employee => EmployeeC, EmployerPosition => EmployerPositionC, Person => PersonC, PersonAddress => PersonAddressC, SalaryGrade => SalaryGradeC}
import kz.talgat.daos.DAO
import kz.talgat.util.Constants.CustomFields.SAP_DEFAULT_TEXT
import scalikejdbc._

import scala.util.{Failure, Success, Try}

trait EmployeeDao {

  def insert(employeeData: (Employee, Int, Int), employerId: EmployerId, countryMap: Map[String, Int])(implicit addressLocationCd: Int): Long

  def findByPersonId(personId: Int, employerId: Int): Option[EmployeeHiring]

  def find(employeeId: EmployeeId, employerId: Int): Option[EmployeeHiring]

  def updateNumber(employeeId: EmployeeId, employeeNumber: String): Unit

  def getTerminated(employeeId: EmployeeId): EmployeeTermination

  def getEmployeeNumbers(employerId: EmployerId): List[String]

  def getEmployeeIds(employerId: Option[EmployerId]): List[(EmployeeId, EmployeeNumber)]
}

class EmployeeDaoImpl(protected val dao: DAO)
  extends EmployeeDao
    with DaoImpl {

  override def insert(employeeTuple: (Employee, Int, Int), employerId: EmployerId, countryMap: Map[String, Int])(implicit addressLocationCd: Int): Long = localTxQuery { implicit session =>
    val (employeeData, genderCd, statusCd) = employeeTuple
    val cee = EmployeeC.column
    val cper = PersonC.column
    val cpa = PersonAddressC.column

    val hireDate = Try(employeeData.jobAssignment.map(x => x.map(_.validPeriod.startDate)).head.headOption.getOrElse(LocalDate.now())) match {
      case Success(v) => v
      case Failure(_) => LocalDate.now()
    }

    val email = Try(employeeData.workplaceAddressInformation.map(_.emailURI).getOrElse("")) match {
      case Success(v) => v
      case Failure(_) => ""
    }

    val personId = withSQL {
      insertInto(PersonC)
        .namedValues(
          cper.firstName -> employeeData.givenName,
          cper.lastName -> employeeData.familyName,
          cper.genderCd -> genderCd,
          cper.email -> email,
          cper.statusCd -> statusCd)
    }.updateAndReturnGeneratedKey().apply()


    employeeData.workplaceAddressInformation.map(_.countryCode).foreach { country =>
      countryMap.get(country).foreach { countryCd =>
        withSQL {
          insertInto(PersonAddressC)
            .namedValues(cpa.personId -> personId,
              cpa.countryCd -> countryCd,
              cpa.addressLocationCd -> addressLocationCd,
              cpa.address1 -> SAP_DEFAULT_TEXT,
              cpa.city -> SAP_DEFAULT_TEXT,
              cpa.postalCode -> "00000",
              cpa.statusCd -> statusCd)
        }.update().apply()
      }
    }


    withSQL {
      insertInto(EmployeeC)
        .namedValues(cee.personId -> personId, cee.employerId -> employerId, cee.employeeNumber -> employeeData.employeeId, cee.hireDate -> hireDate, cee.statusCd -> statusCd, cee.maxSenderPoints -> 0)
    }.updateAndReturnGeneratedKey().apply()
  }

  override def findByPersonId(personId: Int, employerId: Int): Option[EmployeeHiring] = localTxQuery { implicit session =>
    // TODO: implement
    None
  }

  override def find(employeeId: EmployeeId, employerId: Int): Option[EmployeeHiring] = localTxQuery { implicit session =>
    val ee = EmployeeC.defaultAlias
    val per = PersonC.defaultAlias
    val ast = AssignmentC.defaultAlias
    val ad = AssignmentDetailC.defaultAlias
    val pa = PersonAddressC.defaultAlias
    val ep = EmployerPositionC.defaultAlias
    val ctdDepartment = CodeTableDetailC.createAlias("ctdDepartment")
    val ctdAssignmentAction = CodeTableDetailC.createAlias("ctdAssignmentAction")
    val ctdRateUnit = CodeTableDetailC.createAlias("ctdRateUnit")
    val ctdCountry = CodeTableDetailC.createAlias("ctdCountry")
    val ctdPositionType = CodeTableDetailC.createAlias("ctdPositionType")
    val ctdSalaryStep = CodeTableDetailC.createAlias("ctdSalaryStep")
    val sg = SalaryGradeC.defaultAlias

    def getJobId(positionCode: String, stepCode: String): String = {
      positionCode + "_" + stepCode.split("\\D+").filter(_.nonEmpty).toList.map(_.toInt).headOption.getOrElse("1")
    }

    withSQL {
      select
        .from(EmployeeC as ee)
        .innerJoin(PersonC as per).on(ee.personId, per.id)
        .innerJoin(PersonAddressC as pa).on(sqls.eq(per.id, pa.personId) and pa.isPrimary)
        .innerJoin(AssignmentC as ast).on(ee.id, ast.employeeId)
        .innerJoin(AssignmentDetailC as ad).on(ast.id, ad.assignmentId)
        .leftJoin(EmployerPositionC as ep).on(ad.positionId, ep.id)
        .innerJoin(CodeTableDetailC as ctdAssignmentAction).on(ad.assignmentActionCd, ctdAssignmentAction.id)
        .innerJoin(CodeTableDetailC as ctdRateUnit).on(ad.rateUnitCd, ctdRateUnit.id)
        .innerJoin(CodeTableDetailC as ctdDepartment).on(ad.departmentCd, ctdDepartment.id)
        .innerJoin(CodeTableDetailC as ctdCountry).on(pa.countryCd, ctdCountry.id)
        .leftJoin(CodeTableDetailC as ctdPositionType).on(ad.positionTypeCd, ctdPositionType.id)
        .leftJoin(SalaryGradeC as sg).on(ad.salaryGradeId, sg.id)
        .leftJoin(CodeTableDetailC as ctdSalaryStep).on(sg.salaryStepCd, ctdSalaryStep.id)
        .where(
          sqls.eq(ee.id, employeeId) and
            sqls.eq(ast.isPrimary, true) and
            (sqls.le(ad.effectiveDate, LocalDate.now) and
              (sqls.isNull(ad.expirationDate) or sqls.ge(ad.expirationDate, LocalDate.now)))
            and sqls.eq(ee.employerId, employerId)
        )
    }.map { e =>
      val employeeNumber = e.string(ee.resultName.employeeNumber)
      val baseMeasureUnits = e.string(ctdRateUnit.resultName.code)
      val positionId = getJobId(e.string(ep.resultName.code), e.stringOpt(ctdSalaryStep.resultName.code).getOrElse(""))
      val workAgreementTypeCodes = e.string(ctdPositionType.resultName.code)
      val hireDate = e.localDate(ee.resultName.hireDate)
      val givenName = e.string(per.resultName.firstName)
      val familyName = e.string(per.resultName.lastName)
      val administrativeCategoryCode = e.boolean(ad.resultName.isSalary)
      val countryCode = e.string(ctdCountry.resultName.attribute1)
      val email = e.string(per.resultName.email)
      val workPhone = e.stringOpt(per.resultName.workPhone)
      val homePhone = e.stringOpt(per.resultName.homePhone)
      val mobilePhone = e.stringOpt(per.resultName.mobilePhone)
      val costCenterId = e.string(ctdDepartment.resultName.code)
      val transferDate = e.localDateOpt(ad.resultName.effectiveDate)

      val rateDecimal = baseMeasureUnits match {
        case "M" => e.double(ad.resultName.averageDays) * e.double(ad.resultName.averageHours) * 4
        case "B" => e.double(ad.resultName.averageDays) * e.double(ad.resultName.averageHours) * 2
        case "H" => e.double(ad.resultName.averageHours)
        case _ => e.double(ad.resultName.averageDays) * e.double(ad.resultName.averageHours) //"W"
      }

      EmployeeHiring(
        employeeId = employeeId,
        employeeNumber = employeeNumber,
        hireDate = hireDate,
        transferDate = transferDate,
        givenName = givenName,
        familyName = familyName,
        countryCode = countryCode,
        typeCode = WorkAgreementTypeCodes.valueOfString(workAgreementTypeCodes),
        administrativeCategoryCode = AdministrativeCategoryCode.valueOfBoolean(administrativeCategoryCode),
        agreedWorkingHoursRate = rateDecimal,
        baseMeasureUnitCode = BaseMeasureUnitCode.valueOfString(baseMeasureUnits),
        organisationalCentreId = costCenterId,
        jobId = positionId,
        email = email,
        phone = homePhone,
        cell = mobilePhone
      )
    }.headOption().apply()
  }

  override def updateNumber(employeeId: EmployeeId, employeeNumber: String): Unit = localTxQuery { implicit session =>
    val column = EmployeeC.column
    withSQL {
      QueryDSL.update(EmployeeC).set(
        column.employeeNumber -> employeeNumber
      ).where.eq(column.id, employeeId)
    }.update.apply()
  }

  override def getTerminated(employeeId: Int): EmployeeTermination = localTxQuery { implicit session =>
    val ee = EmployeeC.defaultAlias
    val ctdTermReason = CodeTableDetailC.createAlias("ctdTermReason")

    withSQL {
      select
        .from(EmployeeC as ee)
        .innerJoin(CodeTableDetailC as ctdTermReason).on(ee.terminationCd, ctdTermReason.id)
        .where(
          sqls.eq(ee.id, employeeId)
        )
    }.map { e =>
      val termReason = e.string(ctdTermReason.resultName.code)
      val terminationCode = e.string(ctdTermReason.resultName.description)
      val employeeNumber = e.string(ee.resultName.employeeNumber)
      val terminationDate = e.localDate(ee.resultName.terminationDate)
      EmployeeTermination(employeeId = employeeId,
        employeeNumber = employeeNumber,
        terminationDate = terminationDate,
        personnelEventTypeCode = PersonnelEventTypeCode.valueOfString(termReason),
        personnelEventReasonCode = PersonnelEventReasonCode.valueOfString(terminationCode),
      )
    }.headOption().apply()
      .getOrElse(throw new RuntimeException(s"Employee($employeeId) could not be found in database"))
  }

  override def getEmployeeNumbers(employerId: EmployerId): List[String] = localTxQuery { implicit session =>
    val ee = EmployeeC.defaultAlias
    withSQL {
      select
        .from(EmployeeC as ee)
        .where(
          sqls.eq(ee.employerId, employerId)
        )
    }.map(EmployeeC(_).employeeNumber).toList().apply()
  }

  override def getEmployeeIds(employerId: Option[EmployerId] = None): List[(EmployeeId, EmployeeNumber)] = localTxQuery { implicit session =>
    val ee = EmployeeC.defaultAlias
    val employerFilter = employerId.map(employerId => sqls.eq(ee.employerId, employerId))
    withSQL {
      select
        .from(EmployeeC as ee)
        .where(sqls.isNull(ee.terminationDate)
          and employerFilter
        )
    }.map { e =>
      val id = e.int(ee.resultName.id)
      val numbeer = e.string(ee.resultName.employeeNumber)
      (id, numbeer)
    }.toList().apply()
  }
}