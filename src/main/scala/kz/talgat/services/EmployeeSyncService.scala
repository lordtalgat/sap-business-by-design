package kz.talgat.services

import com.criterionhcm.apps.exceptions.AppException
import com.criterionhcm.apps.models.AppResult
import com.criterionhcm.concurrent.AppExecutionContext.ec
import com.criterionhcm.dao.EmployeeDao
import com.criterionhcm.models.{EmployeeHiring, EmployeeTermination}
import com.criterionhcm.result.ResultConverter
import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

trait EmployeeSyncService {

  def createEmployee(employee: EmployeeHiring): Future[AppResult]

  def updateEmployee(employee: EmployeeHiring, changeStateId: String): Future[AppResult]

  def terminateEmployee(employee: EmployeeTermination): Future[AppResult]

  def transferEmployee(employee: EmployeeHiring): Future[AppResult]

  def rehireEmployee(employee: EmployeeHiring): Future[AppResult]
}

class EmployeeSyncServiceImpl(soapVerificationService: SoapVerificationService,
                              employeeDao: EmployeeDao)
  extends EmployeeSyncService
    with StrictLogging {

  implicit def toObjectNode(obj: JsObject): JsonNode = {
    play.libs.Json.parse(obj.toString())
  }


  override def createEmployee(employee: EmployeeHiring): Future[AppResult] = {
    soapVerificationService.queryEmployeeHiring(
      employeeId = employee.employeeId,
      hireDate = employee.hireDate,
      givenName = employee.givenName,
      familyName = employee.familyName,
      countryCode = employee.countryCode,
      typeCode = employee.typeCode.toString,
      administrativeCategoryCode = employee.administrativeCategoryCode.toString,
      agreedWorkingHoursRate = employee.agreedWorkingHoursRate,
      baseMeasureUnitCode = employee.baseMeasureUnitCode.toString,
      organisationalCentreID = employee.organisationalCentreId,
      jobId = employee.jobId
    ).flatMap {
      case Left(hireRes) =>
        soapVerificationService.queryEmployeeByFNameLName(employee.givenName, employee.familyName)
          .flatMap {
            case Left(employeeReq) =>
              employeeReq.find(_.changeStateId == hireRes.changeStateId)
                .fold(
                  throw new AppException("Employee with Fname and Lname couldn't find", Json.obj("err" -> "Employee with Fname and Lname couldn't find"))
                ) { employeeReq =>
                  employeeDao.updateNumber(employee.employeeId, employeeReq.employeeId.toString)
                  soapVerificationService.queryEmployeeUpdate(technicalId = employee.employeeId,
                    changeStateId = employeeReq.changeStateId,
                    employeeId = employeeReq.employeeId.toString,
                    phone = employee.phone,
                    cell = employee.cell,
                    email = employee.email)
                    .map {
                      case Left(updateReq) => AppResult(s"Synchronized hired employeeId=${employee.employeeId}, SAPEmployeeId=${employeeReq.employeeId} changeState=$updateReq", JsObject.empty)
                      case Right(errors) => ResultConverter.convertErrorsToAppResult(employee.employeeNumber, errors)
                    }
                }
            case Right(error) =>
              Future.successful(ResultConverter.convertErrorsToAppResult(employee.employeeNumber, Seq(error.getMessage)))
          }
      case Right(errors) =>
        Future.successful(ResultConverter.convertErrorsToAppResult(employee.employeeNumber, errors))
    }
  }

  override def updateEmployee(employee: EmployeeHiring, changeStateId: String): Future[AppResult] = {
    soapVerificationService.queryEmployeeUpdate(
      technicalId = employee.employeeId,
      changeStateId = changeStateId,
      employeeId = employee.employeeNumber,
      phone = employee.phone,
      cell = employee.cell,
      email = employee.email)
      .map {
        case Left(updateReq) =>
          AppResult(s"Synchronized hired employeeId=${employee.employeeId}, SAPEmployeeId=${employee.employeeId} changeState=$updateReq", JsObject.empty)
        case Right(errors) =>
          ResultConverter.convertErrorsToAppResult(employee.employeeNumber, errors)
      }
  }

  override def terminateEmployee(employee: EmployeeTermination): Future[AppResult] = {
    soapVerificationService.queryEmployeeTermninate(
      employee.terminationDate,
      employee.employeeNumber,
      employee.personnelEventTypeCode.toString,
      employee.personnelEventReasonCode.toString)
      .map {
        case Left(result) =>
          AppResult(s"Employee id=${employee.employeeId} number=${employee.employeeNumber} successfully terminated changeState=$result", JsObject.empty)
        case Right(errors) =>
          ResultConverter.convertErrorsToAppResult(employee.employeeNumber, errors)
      }
  }

  override def transferEmployee(employee: EmployeeHiring): Future[AppResult] = {
    soapVerificationService.queryEmployeeTransfer(
      transferDate = employee.transferDate.get,
      employeeId = employee.employeeNumber,
      agreedWorkingHoursRate = employee.agreedWorkingHoursRate,
      baseMeasureUnitCode = employee.baseMeasureUnitCode.toString,
      organisationalCentreId = employee.organisationalCentreId,
      jobId = employee.jobId)
      .map {
        case Left(result) =>
          AppResult(s"Employee id=${employee.employeeId} number=${employee.employeeNumber} successfully terminated changeState=$result", JsObject.empty)
        case Right(errors) =>
          ResultConverter.convertErrorsToAppResult(employee.employeeNumber, errors)
      }
  }

  override def rehireEmployee(employee: EmployeeHiring): Future[AppResult] = {
    soapVerificationService.queryEmployeeReHire(
      reHireDate = employee.hireDate,
      employeeId = employee.employeeNumber,
      countryCode = employee.countryCode,
      typeCode = employee.typeCode.toString,
      administrativeCategoryCode = employee.administrativeCategoryCode.toString,
      agreedWorkingHoursRate = employee.agreedWorkingHoursRate,
      baseMeasureUnitCode = employee.baseMeasureUnitCode.toString,
      organisationalCentreId = employee.organisationalCentreId,
      jobId = employee.jobId)
      .map {
        case Left(result) =>
          AppResult(s"Employee id=${employee.employeeId} number=${employee.employeeNumber} successfully terminated changeState=$result", JsObject.empty)
        case Right(errors) =>
          ResultConverter.convertErrorsToAppResult(employee.employeeNumber, errors)
      }
  }
}