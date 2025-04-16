package kz.talgat.models

import java.time.ZonedDateTime

case class EmployeeWorkLocation(id: Int,
                                employeeId: Int,
                                employerWorkLocationId: Int,
                                expirationDate: Option[ZonedDateTime],
                                isPrimary: Boolean,
                                isActive: Boolean)
