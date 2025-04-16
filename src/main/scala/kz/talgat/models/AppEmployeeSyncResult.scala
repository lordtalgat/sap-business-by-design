package kz.talgat.models

case class AppEmployeeSyncResult(employeeId: Int, employeeNumber: String, isSuccessful: Boolean, message: Option[String])

object AppEmployeeSyncResult extends Serializable {
  def apply(employeeId: Int, employeeNumber: String, apiResult: ApiResult): AppEmployeeSyncResult =
    AppEmployeeSyncResult(employeeId, employeeNumber, apiResult.isSuccessful, apiResult.message)

  def apply(json: JsValue): AppEmployeeSyncResult =

}
