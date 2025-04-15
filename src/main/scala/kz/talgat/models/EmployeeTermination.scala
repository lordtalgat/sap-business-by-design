package kz.talgat.models

import com.criterionhcm.helpers.{PersonnelEventReasonCode, PersonnelEventTypeCode}

import java.time.LocalDate

case class EmployeeTermination(employeeId: Int, //in Criterion
                               employeeNumber: String, //employeeId in SAP
                               terminationDate: LocalDate, //livingDate in SAP
                               personnelEventTypeCode: PersonnelEventTypeCode.Value,
                               personnelEventReasonCode: PersonnelEventReasonCode.Value)