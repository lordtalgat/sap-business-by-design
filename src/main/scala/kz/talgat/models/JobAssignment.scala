package kz.talgat.models

import com.criterionhcm.helpers.{BaseMeasureUnitCode, WorkAgreementTypeCodes, AdministrativeCategoryCode}


case class JobAssignment(jobUUID: String,
                         jobID: String,
                         jobName: String,
                         validPeriod: ValidPeriod,
                         rateDecimalValue: Option[Double],
                         baseMeasureUnitCode: Option[BaseMeasureUnitCode.Value],
                         workAgreementTypeCodes: Option[WorkAgreementTypeCodes.Value],
                         administrativeCategoryCode: Option[AdministrativeCategoryCode.Value],
                         objectNodeSenderTechnicalId: Option[Int])

object JobAssignment {
  def apply(jobUUID: String,
            jobID: String,
            jobName: String,
            validPeriod: ValidPeriod): JobAssignment = {
    JobAssignment(jobUUID = jobUUID,
      jobID = jobID,
      jobName = jobName,
      validPeriod = validPeriod,
      rateDecimalValue = None,
      baseMeasureUnitCode = None,
      workAgreementTypeCodes = None,
      administrativeCategoryCode = None,
      objectNodeSenderTechnicalId = None
    )
  }
}
