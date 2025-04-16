package kz.talgat.models

case class Assignment(id: Int,
                      employeeId: Int,
                      isPrimary: Boolean,
                      isTerminated: Boolean,
                      wf1EmployeeId: Option[Int],
                      wf2EmployeeId: Option[Int],
                      statusCd: Int,
                      workflowLogId: Option[Int],
                      signatureId: Option[Int])
