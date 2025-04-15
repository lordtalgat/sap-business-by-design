package kz.talgat.services

import java.time.LocalDate

import com.criterionhcm.helpers.DateFormatHelper.convertLocalDateToString

import scala.xml.Elem

object SoapVerificationXmlRequests {

  def generateXmlCompanyFinancialsProcess(): Elem = {
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:glob="http://sap.com/xi/SAPGlobal20/Global">
      <soapenv:Header/>
      <soapenv:Body>
        <glob:CompanyFinancialsProcessControlByElementsQuery>
          <CompanyFinancialsProcessControlSelectionByElements>
          </CompanyFinancialsProcessControlSelectionByElements>
        </glob:CompanyFinancialsProcessControlByElementsQuery>
      </soapenv:Body>
    </soapenv:Envelope>
  }

  def generateXmlOrganisationalCenterIn(): Elem = {
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:glob="http://sap.com/xi/SAPGlobal20/Global">
      <soapenv:Header/>
      <soapenv:Body>
        <glob:OrganisationalCentreByElementsQuery_sync>
          <OrganisationalCentreSelectionByElements>
          </OrganisationalCentreSelectionByElements>
        </glob:OrganisationalCentreByElementsQuery_sync>
      </soapenv:Body>
    </soapenv:Envelope>
  }

  def generateEmployeeById(employeeId: String = "0"): Elem = {
    val finalEmployeeId = employeeId match {
      case "0" => "*"
      case _ => employeeId
    }

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:glob="http://sap.com/xi/SAPGlobal20/Global">
      <soapenv:Header/>
      <soapenv:Body>
        <glob:EmployeeBasicDataByIdentificationQuery_sync>
          <EmployeeBasicDataSelectionByIdentification>
            <SelectionByEmployeeID>
              <InclusionExclusionCode>I</InclusionExclusionCode>
              <IntervalBoundaryTypeCode>1</IntervalBoundaryTypeCode>
              <LowerBoundaryEmployeeID>{finalEmployeeId}</LowerBoundaryEmployeeID>
            </SelectionByEmployeeID>
          </EmployeeBasicDataSelectionByIdentification>
        </glob:EmployeeBasicDataByIdentificationQuery_sync>
      </soapenv:Body>
    </soapenv:Envelope>
  }

  def generateEmployeeByFNameLName(fName: String, lName: String): Elem = {
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:glob="http://sap.com/xi/SAPGlobal20/Global">
      <soapenv:Header/>
      <soapenv:Body>
        <glob:EmployeeDataByIdentificationQuery_sync>
          <EmployeeDataSelectionByIdentification>
            <SelectionBySearchText>
              {fName + " " + lName}
            </SelectionBySearchText>
          </EmployeeDataSelectionByIdentification>
          <PROCESSING_CONDITIONS>
            <QueryHitsMaximumNumberValue>10</QueryHitsMaximumNumberValue>
            <QueryHitsUnlimitedIndicator>false</QueryHitsUnlimitedIndicator>
          </PROCESSING_CONDITIONS>
        </glob:EmployeeDataByIdentificationQuery_sync>
      </soapenv:Body>
    </soapenv:Envelope>
  }

  def generateEmployeeHiring(employeeId: Int,
                             hireDate: LocalDate,
                             givenName: String,
                             familyName: String,
                             countryCode: String,
                             typeCode: String,
                             administrativeCategoryCode: String,
                             agreedWorkingHoursRate: Double,
                             baseMeasureUnitCode: String,
                             organisationalCentreId: String,
                             jobId: String): Elem = {
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:glob="http://sap.com/xi/SAPGlobal20/Global">
      <soapenv:Header/>
      <soapenv:Body>
        <glob:PersonnelHiringBundleMaintainRequest_sync>
          <BasicMessageHeader>
          </BasicMessageHeader>
          <PersonnelHiring actionCode="01">
            <ObjectNodeSenderTechnicalID>{employeeId.toString}</ObjectNodeSenderTechnicalID>
            <!-- Format YYYY-MM-DD -->
            <HireDate>{convertLocalDateToString(hireDate, "employee -> hireDate")}</HireDate>
            <Employee>
              <GivenName>{givenName}</GivenName>
              <FamilyName>{familyName}</FamilyName>
            </Employee>
            <Employment>
              <CountryCode>{countryCode}</CountryCode>
            </Employment>
            <WorkAgreement>
              <!-- Work Agreement Type Codes - 1 = Permanent  1	Executive 2 Retiree 3 Temporary 4 Trainee 5 Working student 6 -->
              <TypeCode>{typeCode}</TypeCode>
              <!-- Work Agreement Administrative Category Code - 2 = Salaried Employee 2	Hourly 3 Manager 4 -->
              <AdministrativeCategoryCode>{administrativeCategoryCode}</AdministrativeCategoryCode>
              <AgreedWorkingHoursRate>
                <DecimalValue>{agreedWorkingHoursRate.toString}</DecimalValue>
                <BaseMeasureUnitCode>{baseMeasureUnitCode}</BaseMeasureUnitCode>
              </AgreedWorkingHoursRate>
              <!-- Set to valid Organizational Center IDs -->
              <OrganisationalCentreID>{organisationalCentreId}</OrganisationalCentreID>
              <!-- 10_CLERK_1Set to valid Job IDs (J016) -->
              <JobID>{jobId}</JobID>
            </WorkAgreement>
          </PersonnelHiring>
        </glob:PersonnelHiringBundleMaintainRequest_sync>
      </soapenv:Body>
    </soapenv:Envelope>
  }

  def generateEmployeeUpdate(technicalId: Int,
                             employeeId: String,
                             changeStateId: String,
                             phone: Option[String],
                             cell: Option[String],
                             email: String): Elem = {
    val phoneElm = phone.fold(<Telephone/>) { phone =>
      <Telephone actionCode="04">
        <TelephoneFormattedNumberDescription>{phone}</TelephoneFormattedNumberDescription>
      </Telephone>
    }
    val cellElm = cell.fold(<Telephone/>) { cell =>
      <Telephone actionCode="04">
        <TelephoneFormattedNumberDescription>{cell}</TelephoneFormattedNumberDescription>
        <MobilePhoneNumberIndicator>true</MobilePhoneNumberIndicator>
      </Telephone>
    }

    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:glob="http://sap.com/xi/SAPGlobal20/Global">
      <soapenv:Header/>
      <soapenv:Body>
        <glob:EmployeeDataBundleMaintainRequest_sync>
          <BasicMessageHeader/>
          <EmployeeData workplaceAddressInformationListCompleteTransmissionIndicator="true" actionCode="04">
            <ObjectNodeSenderTechnicalID>{technicalId}</ObjectNodeSenderTechnicalID>
            <!-- ChangeStateID value from Query Response OR Personnel Hiring Response -->
            <ChangeStateID>{changeStateId}</ChangeStateID>
            <Identification actionCode="04">
              <!-- HCM001 = Employee Type -->
              <PartyIdentifierTypeCode>HCM001</PartyIdentifierTypeCode>
              <!-- EmployeeID value from Query Response -->
              <EmployeeID>{employeeId}</EmployeeID>
            </Identification>
            <WorkplaceAddressInformation actionCode="04">
              <Address actionCode="04">
                <!-- Main Work Phone -->
                {phoneElm}
                <!-- Main Work Cell Phone -->
                {cellElm}
                <!-- Work Email -->
                <Email actionCode="04">
                  <URI>{email}</URI>
                </Email>
              </Address>
            </WorkplaceAddressInformation>
          </EmployeeData>
        </glob:EmployeeDataBundleMaintainRequest_sync>
      </soapenv:Body>
    </soapenv:Envelope>
  }

  def generateEmployeeTerminate(livingDate: LocalDate,
                                EmployeeId: String,
                                personnelEventTypeCode: String,
                                personnelEventReasonCode: String): Elem = {
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:glob="http://sap.com/xi/SAPGlobal20/Global">
      <soapenv:Header/>
      <soapenv:Body>
        <glob:PersonnelLeavingBundleMaintainRequest_sync>
          <BasicMessageHeader/>
          <!-- 1 or more repetitions – Use personnelleaving node multiple times in
              one call in event of seasonal layoffs where multiple employees are
              terminated -->
          <PersonnelLeaving actionCode="01">
            <LeavingDate>{convertLocalDateToString(livingDate,"EmployeeTerminate -> livingDate")}</LeavingDate>
            <!-- Use Employee ID or Employee UUID -->
            <EmployeeID>{EmployeeId}</EmployeeID>
            <!--3 For Dismissal, 4 For Resignation-->
            <PersonnelEventTypeCode>{personnelEventTypeCode}</PersonnelEventTypeCode>
            <!--PersonnelEventTypeCode 3 - Dismissal Type Reasons:
            1   Missing qualifications
            2   Reorganization
            3   End of limitation
            4   Gross misconduct
            5   Lacking ability
            6   Redundancy
            7   Statutory reason
        -->
            <!--PersonnelEventTypeCode 4 - Resignation Type Reasons:
            1   Better career opportunities
            2   Death of employee
            3   Disturbed working relationship
            4   Health reasons
            5   Management style
            6   Personal or family circumstances
            7   Decision to study
            8   Salary
            9   Workload
            10  Other
        -->
            <PersonnelEventReasonCode>{personnelEventReasonCode}</PersonnelEventReasonCode>
            <WithoutNoticeIndicator>false</WithoutNoticeIndicator>
          </PersonnelLeaving>
        </glob:PersonnelLeavingBundleMaintainRequest_sync>
      </soapenv:Body>
    </soapenv:Envelope>
  }

  def generateEmployeeReHire(reHireDate: LocalDate,
                             employeeId: String,
                             countryCode: String,
                             typeCode: String,
                             administrativeCategoryCode: String,
                             agreedWorkingHoursRate: Double,
                             baseMeasureUnitCode: String,
                             organisationalCentreId: String,
                             jobId: String): Elem = {
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:glob="http://sap.com/xi/SAPGlobal20/Global">
      <soapenv:Header/>
      <soapenv:Body>
        <glob:PersonnelRehireBundleMaintainRequest_sync>
          <BasicMessageHeader/>
          <!--1 or more repetitions:-->
          <PersonnelRehire actionCode="01">
            <!--The Date Of Rehire-->
            <RehireDate>{convertLocalDateToString(reHireDate, "EmployeeReHire -> reHireDate")}</RehireDate>
            <!--Should Be The SAME Employee ID As The Original Hiring Employee ID-->
            <Employee>
              <ID>{employeeId}</ID>
            </Employee>
            <!--Match To The Employee's Country-->
            <Employment>
              <CountryCode>{countryCode}</CountryCode>
            </Employment>
            <!-- Reference Original PersonnelHiringIn Request For Information In This-->
            <WorkAgreement>
              <TypeCode>{typeCode}</TypeCode>
              <AdministrativeCategoryCode>{administrativeCategoryCode}</AdministrativeCategoryCode>
              <AgreedWorkingHoursRate>
                <DecimalValue>{agreedWorkingHoursRate.toString}</DecimalValue>
                <BaseMeasureUnitCode>{baseMeasureUnitCode}</BaseMeasureUnitCode>
              </AgreedWorkingHoursRate>
              <OrganisationalCentreID>{organisationalCentreId}</OrganisationalCentreID>
              <JobID>{jobId}</JobID>
            </WorkAgreement>
          </PersonnelRehire>
        </glob:PersonnelRehireBundleMaintainRequest_sync>
      </soapenv:Body>
    </soapenv:Envelope>
  }

  def generateEmployeeTransfer(transferDate: LocalDate,
                               employeeId: String,
                               agreedWorkingHoursRate: Double,
                               baseMeasureUnitCode: String,
                               organisationalCentreId: String,
                               jobId: String): Elem = {
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:glob="http://sap.com/xi/SAPGlobal20/Global">
      <soapenv:Header/>
      <soapenv:Body>
        <glob:PersonnelTransferBundleMaintainRequest_sync>
          <BasicMessageHeader/>
          <!--1 or more repetitions:-->
          <PersonnelTransfer actionCode="01">
            <!--Date The Transfer Will Occur In The System-->
            <TransferDate>{convertLocalDateToString(transferDate, "EmployeeTransfer -> transferDate")}
            </TransferDate>
            <!--Mandatory To Identify The Employee-->
            <!-- Use Employee ID or Employee UUID -->
            <EmployeeID>{employeeId}</EmployeeID>
            <!--Should Include As The Prior Value Even If It Does Not Change-->
            <AgreedWorkingHoursRate>
              <DecimalValue>{agreedWorkingHoursRate.toString}</DecimalValue>
              <BaseMeasureUnitCode>{baseMeasureUnitCode}</BaseMeasureUnitCode>
            </AgreedWorkingHoursRate>
            <!--The ByDesign Org. Unit ID, Should Specify Even If Only Job Will Change-->
            <OrganisationalCentreID>{organisationalCentreId}</OrganisationalCentreID>
            <!--The ByDesign Job ID, Should Specify Even If Only Org. Unit Will Change-->
            <JobID>{jobId}</JobID>
          </PersonnelTransfer>
        </glob:PersonnelTransferBundleMaintainRequest_sync>
      </soapenv:Body>
    </soapenv:Envelope>
  }
}