package kz.talgat.models

case class Company(changeStateId: String, // -> Employer
                   companyUUID: String,
                   companyId: Int,
                   companyName: String,
                   systemAdministrativeData: SystemAdministrativeData)

