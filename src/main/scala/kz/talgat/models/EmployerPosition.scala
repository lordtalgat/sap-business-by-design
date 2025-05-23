package kz.talgat.models

case class EmployerPosition(id: Int,
                            employerId: Int,
                            isActive: Boolean,
                            code: String,
                            title: String,
                            jobId: Option[Int],
                            workersCompensationCd: Option[Int],
                            employerWorkLocationId: Int,
                            costCenterCd: Option[Int],
                            isSalary: Boolean,
                            salaryGradeId: Option[Int],
                            payRate: Double,
                            rateUnitCd: Int,
                            positionTypeCd: Int,
                            fullTimeEquivalency: Double,
                            averageWeeks: Option[Double],
                            averageHours: Option[Double],
                            averageDays: Option[Double],
                            workPeriodId: Option[Int],
                            isOfficer: Option[Boolean],
                            officerCodeCd: Option[Int],
                            isManager: Option[Boolean],
                            isHighSalary: Option[Boolean],
                            isSeasonal: Option[Boolean],
                            departmentCd: Int,
                            securityClearanceCd: Option[Int],
                            travelRequirementsCd: Option[Int],
                            dressCd: Option[Int],
                            workFromHomeCd: Option[Int],
                            workAuthorizationCd: Option[Int],
                            categoryCd: Option[Int],
                            description: Option[String],
                            eeocCd: Option[Int],
                            isExempt: Boolean,
                            minSalaryGradeId: Option[Int],
                            maxSalaryGradeId: Option[Int],
                            experienceCd: Option[Int],
                            educationCd: Option[Int],
                            statusCd: Int,
                            workflowLogId: Option[Int],
                            org1PositionId: Option[Int],
                            org2PositionId: Option[Int],
                            org3PositionId: Option[Int],
                            org4PositionId: Option[Int],
                            signatureId: Option[Int],
                            divisionCd: Option[Int],
                            sectionCd: Option[Int])
