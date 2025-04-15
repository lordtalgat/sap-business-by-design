package kz.talgat.errors

sealed trait EmployeeSyncError {
  def causes: Option[Throwable]

  def getStackTrace: String = {
    causes.map(ex => s"SAP EmployeeSyncError ${ex.getStackTrace.mkString("\n")}").getOrElse("")
  }
}

case class EmployeeIdsSelectionError(error: String, cause: Option[Throwable] = None)
  extends EmployeeSyncError {
  val causes = cause
}

case class EmployeeMoreThanOneError(error: String, cause: Option[Throwable] = None)
  extends EmployeeSyncError {
  val causes = cause
}
