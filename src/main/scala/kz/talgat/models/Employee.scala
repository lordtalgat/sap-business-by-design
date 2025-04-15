package kz.talgat.models

import java.time.LocalDate

case class Employee(uuid: String,
                    changeStateId: String,
                    employeeId: Int,
                    biographicValidPeriod: ValidPeriod,
                    givenName: String,
                    familyName: String,
                    genderCode: Int,
                    workplaceAddressInformation: Option[WorkplaceAddressInformation],
                    jobAssignment: Option[List[JobAssignment]],
                    costCenter: Option[CostCenter],
                    hireDate: Option[LocalDate])
