package kz.talgat.models

import kz.talgat.helpers.{AdministrativeCategoryCode, BaseMeasureUnitCode, WorkAgreementTypeCodes}

import java.time.LocalDate

case class EmployeeHiring(employeeId: Int,
                          employeeNumber: String,
                          hireDate: LocalDate,
                          transferDate: Option[LocalDate],
                          givenName: String,
                          familyName: String,
                          countryCode: String,
                          typeCode: WorkAgreementTypeCodes.Value,
                          administrativeCategoryCode: AdministrativeCategoryCode.Value,
                          agreedWorkingHoursRate: Double,
                          baseMeasureUnitCode: BaseMeasureUnitCode.Value,
                          organisationalCentreId: String,
                          jobId: String,
                          email: String,
                          phone: Option[String],
                          cell: Option[String])

