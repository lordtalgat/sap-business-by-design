package kz.talgat.models

case class SalaryGrade(id: Int,
                       employerId: Int,
                       salaryGroupCd: Int,
                       salaryGradeCd: Int,
                       salaryStepCd: Option[Int],
                       sequence: Option[Int],
                       maxRate: Option[Double],
                       minRate: Double,
                       alternateRate: Option[Double])
