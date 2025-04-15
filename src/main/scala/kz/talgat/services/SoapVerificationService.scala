package kz.talgat.services

import java.net.URLEncoder
import java.time.{LocalDate, LocalDateTime}

import com.criterionhcm.apps.exceptions.AppException
import com.criterionhcm.concurrent.AppExecutionContext.ec
import com.criterionhcm.helpers.DateFormatHelper.{convertLocalDateTimeToString, convertToLocalDate, convertToLocalDateTime}
import com.criterionhcm.models._
import com.criterionhcm.modules.ContextUtil
import com.typesafe.scalalogging.StrictLogging
import javax.inject.Singleton
import play.api.libs.ws.{WSAuthScheme, WSClient}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.Try
import scala.xml.{NodeSeq, XML}

trait SoapVerificationService {
  def queryCompanyFinancialsProcessList(): Future[Either[List[Company], Exception]]

  def queryOrganisationalCenterInList(): Future[Either[List[CostCenter], Exception]]

  def queryProjectCollectionList(lastChangeDateTime: LocalDateTime): List[Future[Either[(List[Project], Int), Exception]]]

  def queryProjectCollectionFilteredList(top: Int,
                                         projectLifeCycleStatusCode: Int,
                                         lastChangeDateTime: LocalDateTime): Future[Either[(List[Project], Int), Exception]]

  def queryEmployeeList(): Future[Either[List[Employee], Exception]]

  def queryEmployeeById(employeeId: String): Future[Either[Option[Employee], Exception]]

  def queryEmployeeByFNameLName(fName: String, lName: String): Future[Either[List[Employee], Exception]]

  def queryEmployeeHiring(employeeId: Int,
                          hireDate: LocalDate,
                          givenName: String,
                          familyName: String,
                          countryCode: String,
                          typeCode: String,
                          administrativeCategoryCode: String,
                          agreedWorkingHoursRate: Double,
                          baseMeasureUnitCode: String,
                          organisationalCentreID: String,
                          jobId: String): Future[Either[PersonelHiring, Seq[String]]]

  def queryEmployeeUpdate(technicalId: Int,
                          changeStateId: String,
                          employeeId: String,
                          phone: Option[String],
                          cell: Option[String],
                          email: String): Future[Either[String, Seq[String]]]

  def queryEmployeeTermninate(livingDate: LocalDate,
                              employeeId: String,
                              personnelEventTypeCode: String,
                              personnelEventReasonCode: String): Future[Either[String, Seq[String]]]

  def queryEmployeeReHire(reHireDate: LocalDate,
                          employeeId: String,
                          countryCode: String,
                          typeCode: String,
                          administrativeCategoryCode: String,
                          agreedWorkingHoursRate: Double,
                          baseMeasureUnitCode: String,
                          organisationalCentreId: String,
                          jobId: String): Future[Either[String, Seq[String]]]

  def queryEmployeeTransfer(transferDate: LocalDate,
                            employeeId: String,
                            agreedWorkingHoursRate: Double,
                            baseMeasureUnitCode: String,
                            organisationalCentreId: String,
                            jobId: String): Future[Either[String, Seq[String]]]
}

@Singleton
class SoapVerificationServiceImpl(wsClient: WSClient)
  extends SoapVerificationService
    with StrictLogging {

  private lazy val DEFAULT_TIMEOUT = 60.seconds

  override def queryCompanyFinancialsProcessList(): Future[Either[List[Company], Exception]] = {
    logger.info("Request: queryCompanyFinancialsProcess")

    val future = wsClient.url(ContextUtil.EndpointUrl + "/sap/bc/srt/scs/sap/querycompanyfinancialsprocess1")
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.BasicLogin, ContextUtil.BasicPassword, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .post(SoapVerificationXmlRequests.generateXmlCompanyFinancialsProcess)

    future.map { resp =>
      errorValidation(resp.status)
      val xml = XML.loadString(resp.body)
      val xmlChild = xml \\ "Body"
      responseCompanyFinancialsProcess(xmlChild)
    }.recover {
      case ex: Exception =>
        Right(ex)
    }
  }

  private def responseCompanyFinancialsProcess(responseXml: NodeSeq): Either[List[Company], Exception] = {
    if (responseXml.toString().contains("<soap-env:Fault>")) {
      val (code, description) = parseError(responseXml)
      Right(new AppException(s"Code: $code. $description"))
    } else {
      Left {
        val list: NodeSeq = responseXml \\ "CompanyFinancialsProcessControlByElementsResponse" \ "CompanyFinancialsProcessControl"
        list.map { company =>
          Company(
            changeStateId = (company \ "ChangeStateID").text.trim,
            companyUUID = (company \ "CompanyUUID").text,
            companyId = (company \ "CompanyID").text.toInt,
            companyName = (company \ "CompanyName").text,
            systemAdministrativeData = SystemAdministrativeData(
              creationDateTime = (company \ "SystemAdministrativeData" \ "CreationDateTime").text,
              creationIdentityUUID = (company \ "SystemAdministrativeData" \ "CreationIdentityUUID").text,
              lastChangeDateTime = (company \ "SystemAdministrativeData" \ "LastChangeDateTime").text,
              lastChangeIdentityUUID = (company \ "SystemAdministrativeData" \ "LastChangeIdentityUUID").text
            )
          )
        }.toList
      }
    }
  }

  private def parseError(xml: NodeSeq): (String, String) = {
    val code = (xml \\ "Fault" \ "faultcode").text
    val description = (xml \\ "Fault" \ "faultstring").text
    val detail = (xml \\ "Fault" \ "detail").text

    (code, description + "\n [" + detail + "]")

  }

  private def parseErrorFromGet(xml: NodeSeq): (String, String) = {
    val code = (xml \\ "error" \ "code").text
    val description = (xml \\ "error" \ "message").text

    (code, description)

  }

  override def queryOrganisationalCenterInList(): Future[Either[List[CostCenter], Exception]] = {
    logger.info("Request: queryOrganisationalCentreIn")

    val future = wsClient.url(ContextUtil.EndpointUrl + "/sap/bc/srt/scs/sap/queryorganisationalcentrein")
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.BasicLogin, ContextUtil.BasicPassword, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .post(SoapVerificationXmlRequests.generateXmlOrganisationalCenterIn)

    future.map { resp =>
      errorValidation(resp.status)
      val xml = XML.loadString(resp.body)
      //  LOG.warn(s"Response: queryOrganisationalCentreIn\n $xml")
      val xmlChild = xml \\ "Body"
      responseOrganisationalCenterIn(xmlChild)
    }.recover {
      case ex: Exception =>
        Right(ex)
    }
  }

  private def responseOrganisationalCenterIn(xml: NodeSeq): Either[List[CostCenter], Exception] = {
    if ((xml \\ "Fault").nonEmpty) {
      val (code, description) = parseError(xml)
      Right(new AppException(s"Code: $code. $description"))
    } else {
      Left {
        val list: NodeSeq = xml \\ "OrganisationalCentreByElementsResponse_sync" \ "OrganisationalCentre"
        list.map { center =>
          CostCenter(
            id = (center \ "ID").text,
            validPeriod = ValidPeriod(
              startDate = convertToLocalDate((center \ "ValidityPeriod" \ "StartDate").text, "StartDate"),
              endDate = convertToLocalDate((center \ "ValidityPeriod" \ "EndDate").text, "EndDate")
            )
          )
        }.toList
      }
    }
  }

  override def queryProjectCollectionList(lastChangeDateTime: LocalDateTime): List[Future[Either[(List[Project], Int), Exception]]] = {
    logger.info("Request: queryOrganisationalCentreIn")
    val top = 100
    val url = ContextUtil.EndpointUrl +
      "/sap/byd/odata/cust/v1/cr_projects/ProjectCollection" +
      generateFiltersForProjectCollectionInLine(top = top, skip = 0, projectLifeCycleStatusCode = 5, lastChangeDateTime = lastChangeDateTime)
    val future = wsClient.url(url)
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.LoginODATA, ContextUtil.PasswordODATA, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .get()

    val count: Int = Await.result(future.map { resp =>
      errorValidation(resp.status)
      Try {
        (XML.loadString(resp.body) \\ "feed" \ "count").text.toInt
      }.getOrElse(0)
    }, DEFAULT_TIMEOUT)

    if (top >= count) {
      List(future.map { resp =>
        logger.info(s"Request: queryOrganisationalCentreIn count=$count")
        errorValidation(resp.status)
        responseProjectCollection(resp.body, count)
      }.recover {
        case ex: Exception =>
          Right(ex)
      })
    } else {
      val urls = for (skip <- 0 to count by top) yield {
        ContextUtil.EndpointUrl +
          "/sap/byd/odata/cust/v1/cr_projects/ProjectCollection" +
          generateFiltersForProjectCollectionInLine(top = top, skip = skip, projectLifeCycleStatusCode = 5, lastChangeDateTime = lastChangeDateTime)
      }

      val futures = urls.map { url =>
        wsClient.url(url)
          .addHttpHeaders("Content-Type" -> "text/xml")
          .withAuth(ContextUtil.LoginODATA, ContextUtil.PasswordODATA, WSAuthScheme.BASIC)
          .withRequestTimeout(DEFAULT_TIMEOUT)
          .get()
      }.toList

      logger.info(s"Request: queryOrganisationalCentreIn count=${urls.size}")
      futures.map(future => future.map { response =>
        errorValidation(response.status)
        responseProjectCollection(response.body, count)
      }.recover {
        case ex: Exception =>
          Right(ex)
      })
    }
  }

  override def queryProjectCollectionFilteredList(top: Int,
                                                  projectLifeCycleStatusCode: Int,
                                                  lastChangeDateTime: LocalDateTime): Future[Either[(List[Project], Int), Exception]] = {
    logger.info("Request: queryOrganisationalCentreIn")
    val url = ContextUtil.EndpointUrl +
      "/sap/byd/odata/cust/v1/cr_projects/ProjectCollection" +
      generateFiltersForProjectCollection(top = top,
        projectLifeCycleStatusCode = projectLifeCycleStatusCode,
        lastChangeDateTime = lastChangeDateTime)

    val future = wsClient.url(url)
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.LoginODATA, ContextUtil.PasswordODATA, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .get()

    future.map { resp =>
      errorValidation(resp.status)
      val xml = XML.loadString(resp.body)
      logger.info(s"Response: queryProjectCollection\n $xml")
      responseProjectCollection(resp.body, 0)
    }.recover {
      case ex: Exception =>
        Right(ex)
    }

  }

  private def filter(projectLifeCycleStatusCode: Int,
                     lastChangeDateTime: LocalDateTime): String = {
    val value = s"(ProjectLifeCycleStatusCode ne '$projectLifeCycleStatusCode') and (LastChangeDateTime ge datetimeoffset'${convertLocalDateTimeToString(lastChangeDateTime, "filter for projects")}Z')"
    URLEncoder.encode(value, "UTF-8").replace("+", "%20")
  }

  private def generateFiltersForProjectCollection(top: Int,
                                                                    projectLifeCycleStatusCode: Int,
                                                                    lastChangeDateTime: LocalDateTime): String = {
    "?$inlinecount=allpages&$skip=0&$top=" + top.toString + "$filter=" + filter(projectLifeCycleStatusCode, lastChangeDateTime)
  }

  private def generateFiltersForProjectCollectionInLine(top: Int,
                                                                          skip: Int,
                                                                          projectLifeCycleStatusCode: Int,
                                                                          lastChangeDateTime: LocalDateTime): String = {
    val d = URLEncoder.encode("$", "UTF-8")
    "?" + d + "inlinecount=allpages&" + d + "skip=" + skip + "&" + d +
      "top=" + top + "&" + d + "filter=" + filter(projectLifeCycleStatusCode, lastChangeDateTime) + "&" + d + "expand=ProjectTask"
  }


  private def responseProjectCollection(xml: String, count: Int) = {
    if (xml.contains("<error")) {
      val (code, description) = parseError(XML.loadString(xml))
      Right(new AppException(s"Code: $code. $description"))
    } else {
      Left {
        val list: NodeSeq = XML.loadString(xml).child.filter(p => p.label == "entry")
        (list.map { project =>
          Project(
            id = (project \ "id").text,
            title = (project \ "title").text,
            updated = Some(convertToLocalDateTime((project \ "updated").text, "ProjectCollection -> updated")),
            category = Some(Category(
              term = (project \ "category" \ "@term").text,
              scheme = (project \ "category" \ "@scheme").text
            )),
            properties = Properties(
              objectID = (project \ "content" \ "properties" \ "ObjectID").text,
              projectID = (project \ "content" \ "properties" \ "ProjectID").text,
              location_KUT = Some((project \ "content" \ "properties" \ "Location_KUT").text),
              location_KUTText = Some((project \ "content" \ "properties" \ "Location_KUTText").text),
              projectLifeCycleStatusCode = (project \ "content" \ "properties" \ "ProjectLifeCycleStatusCode").text.toInt,
              projectLifeCycleStatusCodeText = (project \ "content" \ "properties" \ "ProjectLifeCycleStatusCodeText").text,
              creationDateTime = Some(convertToLocalDateTime((project \ "content" \ "properties" \ "CreationDateTime").text,
                "ProjectCollection -> CreationDateTime")),
              lastChangeDateTime = Some(convertToLocalDateTime((project \ "content" \ "properties" \ "LastChangeDateTime").text,
                "ProjectCollection -> lastChangeDateTime")),
              projectName = (project \ "content" \ "properties" \ "ProjectName").text,
              languageCode = (project \ "content" \ "properties" \ "languageCode").text,
              languageCodeText = (project \ "content" \ "properties" \ "languageCodeText").text
            ),
            tasks = {
              val listTask: NodeSeq = project \\ "inline" \ "feed" \ "entry" \ "content" \ "properties"
              listTask.map { task =>
                Task(
                  objectId = (task \ "ObjectID").text,
                  parentObjectId = (task \ "ParentObjectID").text,
                  location_KUT = Some((task \ "TaskLocation_KUT").text),
                  location_KUTText = Some((task \ "TaskLocation_KUTTex").text),
                  taskName = (task \ "TaskName").text,
                  languageCode = (task \ "languageCode").text,
                  languageCodeText = (task \ "languageCodeText").text,
                  creationDateTime = Some(convertToLocalDateTime((task \ "CreationDateTime").text, "task->properties->CreationDateTime")),
                  lastChangeDateTime = Some(convertToLocalDateTime((task \ "LastChangeDateTime").text, "task->properties->LastChangeDateTime"))
                )
              }
            }.toList
          )
        }.toList, count)
      }
    }
  }

  override def queryEmployeeList(): Future[Either[List[Employee], Exception]] = {
    logger.info("Request: queryEmployeeById")

    val future = wsClient.url(ContextUtil.EndpointUrl + "/sap/bc/srt/scs/sap/queryemployeein")
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.BasicLogin, ContextUtil.BasicPassword, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .post(SoapVerificationXmlRequests.generateEmployeeById())

    future.map { resp =>
      errorValidation(resp.status)
      val xml = XML.loadString(resp.body)
      //  LOG.warn(s"Response: queryEmployeeById\n $xml")
      val xmlChild = xml \\ "Body"
      responseEmployeeList(xmlChild)
    }.recover {
      case ex: Exception =>
        Right(ex)
    }
  }

  private def responseEmployeeList(xml: NodeSeq): Either[List[Employee], Exception] = {
    if ((xml \\ "Fault").nonEmpty) {
      val (code, description) = parseError(xml)
      Right(new AppException(s"Code: $code. $description"))
    } else {
      Left {
        val list: NodeSeq = xml \\ "EmployeeBasicDataByIdentificationResponse_sync" \ "BasicData"
        list.map { emp =>
          Employee(
            uuid = (emp \ "UUID").text,
            changeStateId = (emp \ "ChangeStateID").text.trim,
            employeeId = (emp \ "EmployeeID").text.toInt,
            biographicValidPeriod = ValidPeriod(
              startDate = convertToLocalDate((emp \ "BiographicalData" \ "ValidityPeriod" \ "StartDate").text,
                "Employee -> BiographicalData -> ValidityPeriod ->StartDate"),
              endDate = convertToLocalDate((emp \ "BiographicalData" \ "ValidityPeriod" \ "StartDate").text,
                "Employee -> BiographicalData -> ValidityPeriod ->StartDate")
            ),
            givenName = (emp \ "BiographicalData" \ "GivenName").text,
            familyName = (emp \ "BiographicalData" \ "FamilyName").text,
            genderCode = (emp \ "BiographicalData" \ "GenderCode").text.toInt,
            workplaceAddressInformation = getWorkplaceAddressInformation(emp),
            jobAssignment = getJobAssignmentList(emp),
            costCenter = None,
            hireDate = None
          )
        }.toList
      }
    }
  }

  override def queryEmployeeById(employeeId: String): Future[Either[Option[Employee], Exception]] = {
    logger.info("Request: queryEmployeeById")

    val future = wsClient.url(ContextUtil.EndpointUrl + "/sap/bc/srt/scs/sap/queryemployeein")
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.BasicLogin, ContextUtil.BasicPassword, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .post(SoapVerificationXmlRequests.generateEmployeeById(employeeId))

    future.map { resp =>
      errorValidation(resp.status)
      val xml = scala.xml.XML.loadString(resp.body)
      val xmlChild = xml \\ "Body"
      logger.info(s"Response: queryEmployeeById\n ${xmlChild.toString()}")
      responseEmployeeById(xmlChild)
    }.recover {
      case ex: Exception =>
        Right(ex)
    }
  }

  private def responseEmployeeById(xml: NodeSeq): Either[Option[Employee], Exception] = {
    if ((xml \\ "Fault").nonEmpty) {
      val (code, description) = parseError(xml)
      Right(new AppException(s"Code: $code. $description"))
    } else {
      Left {
        if ((xml \\ "BasicData").nonEmpty) {
          Some(Employee(
            uuid = (xml \\ "BasicData" \ "UUID").text,
            changeStateId = (xml \\ "BasicData" \ "ChangeStateID").text,
            employeeId = (xml \\ "BasicData" \ "EmployeeID").text.toInt,
            biographicValidPeriod = ValidPeriod(
              startDate = convertToLocalDate((xml \\ "BasicData" \ "BiographicalData" \ "ValidityPeriod" \ "StartDate").text,
                "Employee -> BiographicalData -> ValidityPeriod ->StartDate"),
              endDate = convertToLocalDate((xml \\ "BasicData" \ "BiographicalData" \ "ValidityPeriod" \ "StartDate").text,
                "Employee -> BiographicalData -> ValidityPeriod ->StartDate")
            ),
            givenName = (xml \\ "BasicData" \ "BiographicalData" \ "GivenName").text,
            familyName = (xml \\ "BasicData" \ "BiographicalData" \ "FamilyName").text,
            genderCode = (xml \\ "BasicData" \ "BiographicalData" \ "GenderCode").text.toInt,
            workplaceAddressInformation = getWorkplaceAddressInformation(xml),
            jobAssignment = getJobAssignmentList(xml),
            costCenter = None,
            hireDate = None
          ))
        } else {
          None
        }
      }
    }
  }

  private def responseEmployeeByName(xml: NodeSeq): Either[List[Employee], Exception] = {
    if ((xml \\ "Fault").nonEmpty) {
      val (code, description) = parseError(xml)
      Right(new AppException(s"Code: $code. $description"))
    } else {
      Left {
        val list: NodeSeq = xml \\ "EmployeeDataByIdentificationResponse_sync" \ "EmployeeData"
        list.map { emp =>
          Employee(
            uuid = (emp \ "UUID").text,
            changeStateId = (emp \ "ChangeStateID").text.trim,
            employeeId = (emp \ "EmployeeID").text.toInt,
            biographicValidPeriod = ValidPeriod(
              startDate = convertToLocalDate((emp \ "BiographicalData" \ "ValidityPeriod" \ "StartDate").text,
                "Employee -> BiographicalData -> ValidityPeriod ->StartDate"),
              endDate = convertToLocalDate((emp \ "BiographicalData" \ "ValidityPeriod" \ "StartDate").text,
                "Employee -> BiographicalData -> ValidityPeriod ->StartDate")
            ),
            givenName = (emp \ "BiographicalData" \ "GivenName").text,
            familyName = (emp \ "BiographicalData" \ "FamilyName").text,
            genderCode = (emp \ "BiographicalData" \ "GenderCode").text.toInt,
            workplaceAddressInformation = getWorkplaceAddressInformation(xml),
            jobAssignment = getJobAssignmentList(xml),
            costCenter = None,
            hireDate = None
          )
        }.toList
      }
    }
  }

  private def getWorkplaceAddressInformation(xml: NodeSeq): Option[WorkplaceAddressInformation] = {
    if ((xml \\ "WorkplaceAddressInformation").nonEmpty) {
      val node = xml \\ "BasicData" \ "WorkplaceAddressInformation"
      Some(WorkplaceAddressInformation(
        countryCode = (node \\ "OrganisationalPostalAddress" \ "CountryCode").text,
        regionCode = (node \\ "OrganisationalPostalAddress" \ "RegionCode").text,
        timeZoneCode = (node \\ "OrganisationalPostalAddress" \ "TimeZoneCode").text,
        emailURI = (node \\ "EmailURI").text,
        phone = (node \\ "Phone").text,
        formattedAddressDescription = (node \\ "FormattedAddress" \ "FormattedAddressDescription").text,
        formattedPostalAddressDescription = (node \\ "FormattedAddress" \ "FormattedPostalAddressDescription").text,
        firstLineDescription = (node \\ "FormattedAddress" \ "FormattedAddress" \ "FirstLineDescription").text,
        secondLineDescription = (node \\ "FormattedAddress" \ "FormattedAddress" \ "SecondLineDescription").text,
        thirdLineDescription = (node \\ "FormattedAddress" \ "FormattedAddress" \ "ThirdLineDescription").text,
        fourthLineDescription = (node \\ "FormattedAddress" \ "FormattedAddress" \ "FourthLineDescription").text,
        postalFirstLineDescription = (node \\ "FormattedAddress" \ "FormattedPostalAddress" \ "FirstLineDescription").text,
        postalSecondLineDescription = (node \\ "FormattedAddress" \ "FormattedPostalAddress" \ "SecondLineDescription").text
      )
      )
    } else {
      None
    }
  }

  private def getJobAssignmentList(xml: NodeSeq): Option[List[JobAssignment]] = {
    if ((xml \\ "JobAssignment").nonEmpty) {
      val list: NodeSeq = xml \\ "BasicData" \ "JobAssignment"
      Some(list.map { job =>
        JobAssignment(
          jobUUID = (job \ "JobUUID").text,
          jobID = (job \ "JobID").text,
          jobName = (job \ "JobName").text,
          validPeriod = ValidPeriod(
            startDate = convertToLocalDate((job \ "ValidityPeriod" \ "StartDate").text, "JobAssignmentList -> StartDate"),
            endDate = convertToLocalDate((job \ "ValidityPeriod" \ "EndDate").text, "JobAssignmentList -> EndDate")
          )
        )
      }.toList)
    } else {
      None
    }
  }

  override def queryEmployeeByFNameLName(fName: String, lName: String): Future[Either[List[Employee], Exception]] = {
    logger.info("Request: queryEmployeeByFNameLName")
    val elem = SoapVerificationXmlRequests.generateEmployeeByFNameLName(fName, lName)
    logger.info(s"\n elem")

    val future = wsClient.url(ContextUtil.EndpointUrl + "/sap/bc/srt/scs/sap/queryemployeein")
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.BasicLogin, ContextUtil.BasicPassword, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .post(elem)

    future.map { resp =>
      errorValidation(resp.status)
      val xml = XML.loadString(resp.body)
      logger.info(s"Response: queryEmployeeByFNameLName\n $xml")
      val xmlChild = xml \\ "Body"
      responseEmployeeByName(xmlChild)
    }.recover {
      case ex: Exception =>
        Right(ex)
    }
  }

  override def queryEmployeeHiring(employeeId: Int,
                                   hireDate: LocalDate,
                                   givenName: String,
                                   familyName: String,
                                   countryCode: String,
                                   typeCode: String,
                                   administrativeCategoryCode: String,
                                   agreedWorkingHoursRate: Double,
                                   baseMeasureUnitCode: String,
                                   organisationalCentreId: String,
                                   jobId: String): Future[Either[PersonelHiring, Seq[String]]] = {
    logger.info("Request: queryEmployeeHiring")
    val elem = SoapVerificationXmlRequests.generateEmployeeHiring(employeeId, hireDate, givenName, familyName, countryCode, typeCode, administrativeCategoryCode, agreedWorkingHoursRate,
      baseMeasureUnitCode, organisationalCentreId, jobId)
    logger.info(s"\n ${elem}")

    val future = wsClient.url(ContextUtil.EndpointUrl + "/sap/bc/srt/scs/sap/managepersonnelhiringin")
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.BasicLogin, ContextUtil.BasicPassword, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .post(elem)

    future.map { resp =>
      errorValidation(resp.status)
      val xml = XML.loadString(resp.body)
      logger.info(s"Response: queryEmployeeHiring\n $xml")
      val xmlChild = xml \\ "Body"
      responseEmployeeHiring(xmlChild, employeeId)
    }.recover {
      case ex: Exception =>
        Right(Seq(ex.toString))
    }
  }

  private def responseEmployeeHiring(xml: NodeSeq, employeeId: Int): Either[PersonelHiring, Seq[String]] = {
    if ((xml \\ "Fault").nonEmpty) {
      val (code, description) = parseError(xml)
      Right(Seq(s"Code: $code. $description"))
    } else {
        val uuid = (xml \\ "PersonnelHiring" \ "UUID").text
        if (uuid.nonEmpty) {
          Left {
            PersonelHiring(
              uuid = uuid,
              employeeId = 0,
              changeStateId = (xml \\ "PersonnelHiring" \ "ChangeStateID").text.trim,
              referenceObjectNodeSenderTechnicalId = (xml \\ "PersonnelHiring" \ "ReferenceObjectNodeSenderTechnicalID").text.toInt
            )
          }
        } else {
            val errors = Try {
              (xml \\ "Log" \ "Item" \ "Note").map(_.text.trim).distinct
            }.getOrElse {
              logger.warn(s"Error: $xml")
              Seq("please view log on SAP or lof of APP")
            }

            Right(errors)
        }
    }
  }

  private def responseEmployeeUpdate(xml: NodeSeq): Either[String, Seq[String]] = {
    if ((xml \\ "Fault").nonEmpty) {
      val (code, description) = parseError(xml)
      Right(Seq(s"Code: $code. $description"))
    } else {
      val changeStateID = (xml \\ "EmployeeData" \ "ChangeStateID").text.trim
      if (changeStateID.nonEmpty) {
        Left(changeStateID)
      } else {
        val errors = Try {
          (xml \\ "Log" \ "Item" \ "Note").map(_.text.trim).distinct
        }.getOrElse {
          logger.warn(s"Error: $xml")
          Seq("please view log on SAP or lof of APP")
        }

        Right(errors)
      }
    }
  }

  override def queryEmployeeUpdate(technicalId: Int, changeStateId: String, employeeId: String, phone: Option[String], cell: Option[String], email: String): Future[Either[String, Seq[String]]] = {
    logger.info("Request: queryEmployeeUpdate")
    val elem = SoapVerificationXmlRequests.generateEmployeeUpdate(technicalId, employeeId, changeStateId, phone, cell, email)
    logger.info(s"\n $elem")

    val future = wsClient.url(ContextUtil.EndpointUrl + "/sap/bc/srt/scs/sap/manageemployeein2")
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.BasicLogin, ContextUtil.BasicPassword, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .post(elem)

    future.map { resp =>
      errorValidation(resp.status)
      val xml = XML.loadString(resp.body)
      logger.info(s"Response: queryEmployeeUpdate\n $xml")
      val xmlChild = xml \\ "Body"
      responseEmployeeUpdate(xmlChild)
    }.recover {
      case ex: Exception =>
        Right(Seq(ex.getMessage))
    }
  }

  private def responseEmployeeTermninate(xml: NodeSeq): Either[String, Seq[String]] = {
    if ((xml \\ "Fault").nonEmpty) {
      val (code, description) = parseError(xml)
      Right(Seq(s"Code: $code. $description"))
    } else {
      val changeStateID = (xml \\ "PersonnelLeaving" \ "ChangeStateID").text.trim
      if (changeStateID.nonEmpty) {
        Left(changeStateID)
      } else {
        val errors = Try {
          (xml \\ "Log" \ "Item" \ "Note").map(_.text.trim).distinct
        }.getOrElse {
          logger.warn(s"Error: $xml")
          Seq("please view log on SAP or lof of APP")
        }

        Right(errors)
      }
    }
  }

  override def queryEmployeeTermninate(livingDate: LocalDate,
                                       employeeId: String,
                                       personnelEventTypeCode: String,
                                       personnelEventReasonCode: String): Future[Either[String, Seq[String]]] = {
    logger.info("Request: queryEmployeeTermninate")
    val elem = SoapVerificationXmlRequests.generateEmployeeTerminate(livingDate, employeeId, personnelEventTypeCode, personnelEventReasonCode)
    logger.info(s"\n $elem")

    val future = wsClient.url(ContextUtil.EndpointUrl + "/sap/bc/srt/scs/sap/managepersonnelleavingin")
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.BasicLogin, ContextUtil.BasicPassword, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .post(elem)

    future.map { resp =>
      errorValidation(resp.status)
      val xml = XML.loadString(resp.body)
      logger.info(s"Response: queryEmployeeTermninate\n $xml")
      val xmlChild = xml \\ "Body"
      responseEmployeeTermninate(xmlChild)
    }.recover {
      case ex: Exception =>
        Right(Seq(ex.getMessage))
    }
  }

  private def responseEmployeeTransfer(xml: NodeSeq): Either[String, Seq[String]] = {
    if ((xml \\ "Fault").nonEmpty) {
      val (code, description) = parseError(xml)
      Right(Seq(s"Code: $code. $description"))
    } else {
      val changeStateID = (xml \\ "PersonnelTransfer" \ "ChangeStateID").text.trim
      if (changeStateID.nonEmpty) {
        Left(changeStateID)
      } else {
        val errors = Try {
          (xml \\ "Log" \ "Item" \ "Note").map(_.text.trim).distinct
        }.getOrElse {
          logger.warn(s"Error: $xml")
          Seq("please view log on SAP or lof of APP")
        }

        Right(errors)
      }
    }
  }

  override def queryEmployeeTransfer(transferDate: LocalDate,
                                     employeeId: String,
                                     agreedWorkingHoursRate: Double,
                                     baseMeasureUnitCode: String,
                                     organisationalCentreId: String,
                                     jobId: String): Future[Either[String, Seq[String]]] = {
    logger.info("Request: queryEmployeeTransfer")
    val elem = SoapVerificationXmlRequests.generateEmployeeTransfer(transferDate, employeeId, agreedWorkingHoursRate, baseMeasureUnitCode, organisationalCentreId, jobId)
    logger.info(s"\n $elem")

    val future = wsClient.url(ContextUtil.EndpointUrl + "/sap/bc/srt/scs/sap/managepersonneltransferin")
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.BasicLogin, ContextUtil.BasicPassword, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .post(elem)

    future.map { resp =>
      errorValidation(resp.status)
      val xml = XML.loadString(resp.body)
      logger.info(s"Response: queryEmployeeTransfer\n $xml")
      val xmlChild = xml \\ "Body"
      responseEmployeeTransfer(xmlChild)
    }.recover {
      case ex: Exception =>
        Right(Seq(ex.getMessage))
    }
  }

  private def responseEmployeeReHire(xml: NodeSeq): Either[String, Seq[String]] = {
    if ((xml \\ "Fault").nonEmpty) {
      val (code, description) = parseError(xml)
      Right(Seq(s"Code: $code. $description"))
    } else {
      val changeStateID = (xml \\ "PersonnelRehire" \ "ChangeStateID").text.trim
      if (changeStateID.nonEmpty) {
        Left(changeStateID)
      } else {
        val errors = Try {
          (xml \\ "Log" \ "Item" \ "Note").map(_.text.trim).distinct
        }.getOrElse {
          logger.warn(s"Error: $xml")
          Seq("please view log on SAP or lof of APP")
        }

        Right(errors)
      }
    }
  }

  override def queryEmployeeReHire(reHireDate: LocalDate,
                                   employeeId: String,
                                   countryCode: String,
                                   typeCode: String,
                                   administrativeCategoryCode: String,
                                   agreedWorkingHoursRate: Double,
                                   baseMeasureUnitCode: String,
                                   organisationalCentreId: String,
                                   jobId: String): Future[Either[String, Seq[String]]] = {
    logger.info("Request: queryEmployeeReHire")
    val elem = SoapVerificationXmlRequests.generateEmployeeReHire(reHireDate, employeeId, countryCode, typeCode,
      administrativeCategoryCode, agreedWorkingHoursRate, baseMeasureUnitCode, organisationalCentreId, jobId)
    logger.info(s"\n $elem")

    val future = wsClient.url(ContextUtil.EndpointUrl + "/sap/bc/srt/scs/sap/managepersonnelrehirein")
      .addHttpHeaders("Content-Type" -> "text/xml")
      .withAuth(ContextUtil.BasicLogin, ContextUtil.BasicPassword, WSAuthScheme.BASIC)
      .withRequestTimeout(DEFAULT_TIMEOUT)
      .post(elem)

    future.map { resp =>
      val xml = XML.loadString(resp.body)
      logger.info(s"Response: queryEmployeeReHire\n $xml")
      val xmlChild = xml \\ "Body"
      responseEmployeeReHire(xmlChild)
    }.recover {
      case ex: Exception =>
        Right(Seq(ex.getMessage))
    }
  }

  private def errorValidation(status: Int): Unit = {
    status match {
      case 400 => throw new AppException("Bad request please contact administrator 400")
      case 401 => throw new AppException("Login or Password is incorrect 401")
      case 403 => throw new AppException("Not Authorized: Check Authorization Restriction for the User 403")
      case 404 => throw new AppException("Url Not Found 404")
      case 500 => throw new AppException("Internal Server Error 500")
      case _ =>
    }
  }
}