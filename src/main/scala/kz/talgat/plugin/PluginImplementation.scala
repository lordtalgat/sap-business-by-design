package kz.talgat.plugin

import cats.effect.{ContextShift, IO}
import cats.implicits._
import java.util.concurrent.{ExecutorService, Executors, Future}

import com.criterionhcm.apps.dao.DAO
import com.criterionhcm.apps.exceptions.AppException
import com.criterionhcm.apps.models.{ActionEvent, AppEmployeeSyncResult, AppEvent, AppResult, ButtonActionEvent, HttpRequestEvent, Plugin, PluginContext}
import com.criterionhcm.modules.{ContextUtil, DaoUtil, ServiceUtil}
import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.scalalogging.StrictLogging
import com.criterionhcm.concurrent.AppExecutionContext.ec
import com.criterionhcm.events._
import com.criterionhcm.models.{EmployeeHiring, SyncResult, SyncStatus}
import com.criterionhcm.modules.ContextUtil.ProjectSyncDate
import com.criterionhcm.result.{ResultConverter, SapEmployeeSyncResult}
import com.criterionhcm.result.ResultConverter.convertEmployeeSyncResults
import com.criterionhcm.util.ImplicitUtil.FutureImplicitUtil
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSClient
import com.criterionhcm.util.Constants.{CustomFields, CustomTables}

@Singleton
class PluginImplementation @Inject()(ctx: PluginContext,
                                     dao: DAO,
                                     wsClient: WSClient)
  extends Plugin with StrictLogging {

  import DaoUtil.getDaos._

  private val pool: ExecutorService = Executors.newFixedThreadPool(2)

  private def future(block: => AppResult): Future[AppResult] = {
    pool.submit(() => block)
  }

  implicit def toObjectNode(obj: JsObject): JsonNode = {
    play.libs.Json.parse(obj.toString())
  }

  override def init(): Unit = {
    ContextUtil.init(ctx)
    DaoUtil.init(dao)
    ServiceUtil.init(wsClient, DaoUtil.getDaos, ContextUtil)
  }

  override def stop(): Unit = {
    // no code
  }

  override def receiveAppEvent(appEvent: AppEvent): Future[AppResult] = {
    appEvent match {
      case event: EmployeeHireEvent =>
        syncEmployeeHire(event.employeeId)

      case event: EmployeeRehireEvent =>
        syncEmployeeRehire(event.employeeId)

      case event: EmployeeTransferEvent =>
        syncEmployeeTransfer(event.employeeId)

      case event: EmployeeDataChangedEvent =>
        syncEmployeeDataChanged(event.employeeId)

      case event: EmployeeDataChangedEvent =>
        syncEmployeeDataChanged(event.employeeId)

      case event: EmployeeAssignmentChangedEvent =>
        syncEmployeeDataChanged(event.employeeId)

      case event: PersonDataChangedEvent =>
        syncPersonDataChanged(event.personId)

      case event: EmployeeTerminatedEvent =>
        processTerminationEvent(event.employeeId)

      case event: EmployeeTerminationQueuedEvent =>
        processTerminationEvent(event.employeeId)

      case _ =>
        throw new AppException("Unsupported operation is triggered")
    }
  }

  override def receiveActionEvent(actionEvent: ActionEvent): Future[AppResult] = {
    actionEvent match {
      case ButtonActionEvent("Init Data Base", _) =>
        createAllCodeDataTablesForApp()
      case ButtonActionEvent("Sync Employers", _) =>
        syncEmployers()
      case ButtonActionEvent("Sync Employer Departments", _) =>
        syncEmployerDepartments()
      case ButtonActionEvent("Sync Projects and Tasks", _) =>
        syncProjectsAndTasks()
      case ButtonActionEvent("Sync Employees", _) =>
        sync2Employees()
    }
  }

  override def receiveHTTPRequestEvent(httpRequestEvent: HttpRequestEvent): Future[AppResult] = {
    throw new AppException(s"Unsupported operation is triggered: ${httpRequestEvent.getClass.getSimpleName}")
  }

  private def syncEmployeeHire(employeeId: Int): Future[AppResult] = {
    val employeeHiringOpt = employeeDao.find(employeeId, ContextUtil.EmployerId)

    employeeHiringOpt.fold {
      future(AppResult(s"Nothing to process.", JsObject.empty)) // TODO: review
    } { employeeHiring =>
      ServiceUtil.getServices.employeeSyncService.createEmployee(employeeHiring).asJava
    }
  }

  private def syncEmployeeRehire(employeeId: Int): Future[AppResult] = {
    val employeeHiringOpt = employeeDao.find(employeeId, ContextUtil.EmployerId)

    employeeHiringOpt.fold {
      scala.concurrent.Future {
        AppResult(s"Nothing to process.", JsObject.empty) // TODO: review
      }.asJava
    } { employeeHiring =>
      ServiceUtil.getServices.employeeSyncService.rehireEmployee(employeeHiring).asJava
    }
  }

  private def syncEmployeeTransfer(employeeId: Int): Future[AppResult] = {
    val employeeTransferOpt = employeeDao.find(employeeId, ContextUtil.EmployerId)

    employeeTransferOpt.fold {
      scala.concurrent.Future {
        AppResult(s"Nothing to process.", JsObject.empty) // TODO: review
      }.asJava
    } { employeeTransfer =>
      ServiceUtil.getServices.employeeSyncService.transferEmployee(employeeTransfer).asJava
    }
  }

  private def syncPersonDataChanged(personId: Int): Future[AppResult] = {
    val employeeOpt = employeeDao.findByPersonId(personId, ContextUtil.EmployerId)
    employeeOpt.fold {
      scala.concurrent.Future {
        AppResult(s"Nothing to process. Person($personId) doesn't match app settings", JsObject.empty)
      }.asJava
    } { employee =>
      syncEmployee(employee)
    }
  }

  private def syncEmployeeDataChanged(employeeId: Int): Future[AppResult] = {
    val employeeOpt = employeeDao.find(employeeId, ContextUtil.EmployerId)

    employeeOpt.fold {
      scala.concurrent.Future {
        AppResult(s"Nothing to process.", JsObject.empty) // TODO: review
      }.asJava
    } { employee =>
      syncEmployee(employee)
    }
  }

  private def syncEmployee(employee: EmployeeHiring): Future[AppResult] = {
    ServiceUtil.getServices.soapVerificationService.queryEmployeeById(employee.employeeNumber)
      .flatMap {
        case Left(sapEmployeeOpt) =>
          // create new employee if doesn't exists
          sapEmployeeOpt.fold {
            ServiceUtil.getServices.employeeSyncService.createEmployee(employee)
          } { sapEmployee =>
            // update contact data if employee exists
            ServiceUtil.getServices.employeeSyncService.updateEmployee(employee, sapEmployee.changeStateId)
          }
        case Right(err) => throw new AppException(err.getMessage)
      }.asJava
  }

  private def processTerminationEvent(employeeId: Int): Future[AppResult] = {
    val employeeTerminationInfo = employeeDao.getTerminated(employeeId)
    ServiceUtil.getServices.employeeSyncService.terminateEmployee(employeeTerminationInfo).asJava
  }

  private def syncEmployers(): Future[AppResult] = {
    logger.info("SAP. Sync Employers started.")
    ServiceUtil.getServices.soapVerificationService.queryCompanyFinancialsProcessList()
      .map {
        case Left(result) =>
          val (successList, errorList) = ServiceUtil.getServices.syncService.syncEmployers(result).partition(_.success)
          AppResult(s"Successfully synced ${successList.length} employers. Errors: ${errorList.size}", JsObject.empty)
        case Right(err) => throw new AppException(err.getMessage, err, Json.obj("err" -> err.toString))
      }
      .asJava
  }

  private def syncEmployerDepartments(): Future[AppResult] = {
    logger.info("SAP. Sync Employers Departments started.")
    ServiceUtil.getServices.soapVerificationService.queryOrganisationalCenterInList()
      .map {
        case Left(result) =>
          val newCreatedDepartments = ServiceUtil.getServices.syncService.syncDepartments(result).filter(_.id > 0)
          AppResult(s"Successfully synced ${newCreatedDepartments.length} new departments.", JsObject.empty)
        case Right(err) => err match {
          case ex: AppException => throw ex
          case _ => throw new AppException(err.getMessage, err, JsObject.empty)
        }
      }
      .asJava
  }

  private def syncProjectsAndTasks(): Future[AppResult] = {
    logger.info("SAP. Sync Projects and Tasks started.")
    val lists = ServiceUtil.getServices.soapVerificationService.queryProjectCollectionList(ProjectSyncDate.atStartOfDay()).map { listResult =>
      listResult.map {
        case Left(result) =>
          ServiceUtil.getServices.syncService.syncProjects(result._1)
        case Right(err) =>
          List(SyncStatus(0, success = false, err.getMessage.some))
      }
    }
    implicit val contextShift: ContextShift[IO] = IO.contextShift(ec)
    val catsIO = lists.map { app =>
      IO.fromFuture(IO(app))
    }.sequence

    val result = catsIO.map { lists =>
      lists.map { list =>
        list.foldLeft(0, 0, 0, 0, 0) { case ((iProject, iTask, uProject, uTask, err), row) =>
          row match {
            case r if r.id > 0 && r.success => (iProject + 1, iTask + row.error.getOrElse("0").toInt, uProject, uTask, err)
            case r if r.id == 0 && r.success => (iProject, iTask, uProject + 1, uTask + row.error.getOrElse("0").toInt, err)
            case r if r.id == -1 && r.success => (iProject, iTask, uProject, uTask, err)
            case _ => (iProject, iTask, uProject, uTask, err + 1)
          }
        }
      }
    }.unsafeRunSync()

    val (iProject, iTask, uProject, uTask, err) = result.foldLeft(0, 0, 0, 0, 0) { case (a, b) =>
      (a._1 + b._1, a._2 + b._2, a._3 + b._3, a._4 + b._4, a._5 + b._5)
    }

    scala.concurrent.Future {
      val inserted = if (iProject > 0 || iTask > 0) s"Inserted (Projects, Tasks): ($iProject,$iTask)" else ""
      val updated = if (uProject > 0 || uTask > 0) s"Ipdated (Projects, Tasks): ($uProject, $uTask)" else ""
      val errors = if (err > 0) s"Errors: $err" else ""
      val message = if (inserted.nonEmpty || updated.nonEmpty || errors.nonEmpty) {
        Seq(inserted, updated, errors).filter(_.nonEmpty).mkString(", ")
      } else {
        ""
      }

      logger.info("SAP. Sync Projects and Tasks finished.")
      AppResult(s"Sync Projects completed. $message", JsObject.empty)
    }.asJava
  }

  private def syncEmployees(): Future[AppResult] = {
    ServiceUtil.getServices.soapVerificationService.queryEmployeeList()
      .map {
        case Left(result) =>
          val list = ServiceUtil.getServices.syncService.syncEmployees(result)
          AppResult(s"Successfully processed ${list.length} data", JsObject.empty)
        case Right(err) => err match {
          case ex: AppException => throw ex
          case _ => throw new AppException(err.getMessage, err, Json.obj("err" -> err.toString))
        }
      }
      .asJava
  }

  private def sync2Employees(): Future[AppResult] = {
    val employeeIds = employeeDao.getEmployeeIds(Option(ContextUtil.EmployerId))
    val resultList = employeeIds.map { case(employeeId, employeeNumber) =>
      val _employeeChange = try {
        employeeDao.find(employeeId, ContextUtil.EmployerId)
      } catch {
        case _: Exception => none
      }
      _employeeChange.fold {
        scala.concurrent.Future {
          Seq(createAppEmployeeSyncResult(employeeId, employeeNumber, false , "Employee countains issues in profile, coudn't be sync with SAP".some))
        }
      } { employeeChange =>
        ServiceUtil.getServices.soapVerificationService.queryEmployeeById(employeeChange.employeeNumber).flatMap {
          case Left(resultOpt) =>
            resultOpt.fold {
              ServiceUtil.getServices.soapVerificationService.queryEmployeeByFNameLName(employeeChange.givenName, employeeChange.familyName)
                .flatMap {
                  case Left(employeeReqs) =>
                    employeeReqs match {
                      case employeeReq :: Nil =>
                        employeeDao.updateNumber(employeeId, employeeReq.employeeId.toString)
                        ServiceUtil.getServices.soapVerificationService.queryEmployeeUpdate(
                          technicalId = employeeId,
                          changeStateId = employeeReq.changeStateId,
                          employeeId = employeeReq.employeeId.toString,
                          phone = employeeChange.phone,
                          cell = employeeChange.cell,
                          email = employeeChange.email
                        ).map {
                          case Left(updateReq) =>
                            Seq(createAppEmployeeSyncResult(employeeId, employeeReq.employeeId.toString, true, none))
                          case Right(errors) =>
                            ResultConverter.convertErrorsToSapEmployeeSyncResult(employeeNumber, errors)
                        }

                      case _ => scala.concurrent.Future {
                        Seq(createAppEmployeeSyncResult(employeeId, employeeNumber, false, "More then One FName LName Employee in SAP".some))
                      }
                    }
                  case Right(error) => scala.concurrent.Future {
                    Seq(createAppEmployeeSyncResult(employeeId, employeeNumber, false, error.getMessage.some))
                  }
                }
            } { _ =>
              scala.concurrent.Future {
                Seq(createAppEmployeeSyncResult(employeeId, employeeNumber, true, none))
              }
            }

          case Right(error) => scala.concurrent.Future {
            Seq(createAppEmployeeSyncResult(employeeId, employeeNumber, false, error.getMessage.some))
          }
        }
      }
    }

    implicit val contextShift: ContextShift[IO] = IO.contextShift(ec)


    val catsIO = resultList.map { app =>
      IO.fromFuture(IO(app))
    }.sequence.unsafeRunSync().flatten

    scala.concurrent.Future {
      ResultConverter.convertSapEmployeeSyncResults(catsIO)
    }.asJava
  }

  private def createAllCodeDataTablesForApp(): Future[AppResult] = {
    scala.concurrent.Future {
      logger.info("SAP. Database initialize started.")

      val codeTableEntityType = codeTableDao.getByName("ENTITY_TYPE")
      val codeTableDataType = codeTableDao.getByName("DATA_TYPE")
      val employerEntity = codeTableDetailDao.findByCode("EMPLOYER", codeTableEntityType.id).get
      val positionEntity = codeTableDetailDao.findByCode("POSITION", codeTableEntityType.id).get
      val textDataType = codeTableDetailDao.findByCode("TEXT", codeTableDataType.id).get
      val dropdownDataType = codeTableDetailDao.findByCode("DROPDOWN", codeTableDataType.id).get

      // create Employer custom fields
      val existingEmployerCodes = customFieldDao.listExistingCustomFields(CustomFields.AllCodes)
      CustomFields.AllFields.zipWithIndex.foreach { case (fieldInfo, index) =>
        if (!existingEmployerCodes.contains(fieldInfo.code)) {
          customFieldDao.createCustomField(fieldInfo.code, fieldInfo.label, employerEntity.id, textDataType.id, index + 1)
        }
      }

      // create custom code tables
      var existingCodeTables = codeTableDao.listByNames(CustomTables.AllTableNames)
      CustomTables.AllTables.foreach { codeTableInfo =>
        if (!existingCodeTables.exists(_._1 == codeTableInfo.name)) {
          val codeTableId = codeTableDao.insert(codeTableInfo.name, codeTableInfo.description)
          codeTableDetailDao.insert(codeTableId, codeTableInfo.values)
        }
      }

      // Need to have actual info
      existingCodeTables = codeTableDao.listByNames(CustomTables.AllTableNames)

      // create position custom fields
      val existingPositionCodes = customFieldDao.listExistingCustomFields(CustomFields.AllPositionCodes)
      CustomFields.AllPositionFields.zipWithIndex.foreach { case (fieldInfo, index) =>
        if (!existingPositionCodes.contains(fieldInfo.code)) {
          val codeTableIdOpt = fieldInfo.codeTableName.flatMap(name => existingCodeTables.find(_._1 == name).map(_._2))
          customFieldDao.createCustomField(fieldInfo.code, fieldInfo.label, positionEntity.id, dropdownDataType.id, index + 1, codeTableIdOpt)
        }
      }

      logger.info("SAP. Database initialize finished.")
      AppResult(s"Successfully processed init Database data", JsObject.empty)
    }.asJava
  }

  private def createAppEmployeeSyncResult(employeeId: Int, employeeNumber: String, isSuccessful: Boolean, message: Option[String]): SapEmployeeSyncResult = {
    SapEmployeeSyncResult(
      id = employeeNumber,
      employeeNumber = employeeNumber,
      isSuccessful = isSuccessful,
      message = message
    )
  }
}