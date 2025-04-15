package kz.talgat.result

import cats.syntax.option._
import com.criterionhcm.apps.models.{AppEmployeeSyncResult, AppResult, AppResultMultipleDetails, AppResultSingleDetails}
import com.criterionhcm.apps.result.EmployeeResultColumns
import com.fasterxml.jackson.databind.JsonNode
import play.api.libs.json.JsObject

object ResultConverter {
  implicit def toObjectNode(obj: JsObject): JsonNode = {
    play.libs.Json.parse(obj.toString())
  }

  def convertEmployeeSyncResult(syncResult: AppEmployeeSyncResult): AppResult = {
    val status = if (syncResult.isSuccessful) "succeeds" else "failed"
    val resultObj = AppResultSingleDetails(
      isSuccessful = syncResult.isSuccessful,
      columns = EmployeeResultColumns.ResultColumns,
      data = Seq(syncResult.toJson)
    )

    AppResult(s"Employee '${syncResult.employeeNumber}' sync $status.", resultObj.toJson)
  }

  def convertEmployeeSyncResults(syncResults: Seq[AppEmployeeSyncResult]): AppResult = {
    if (syncResults.size == 1) {
      convertEmployeeSyncResult(syncResults.head)
    } else {
      val successfulSyncNumber = syncResults.count(_.isSuccessful)
      val failedSyncNumber = syncResults.filter(!_.isSuccessful).map(_.employeeId).distinct.size
      val message = s"Success: $successfulSyncNumber employees. Failed: $failedSyncNumber employees."
      val resultObj = AppResultMultipleDetails(
        successfulCount = successfulSyncNumber,
        failedCount = failedSyncNumber,
        columns = EmployeeResultColumns.ResultColumns,
        data = syncResults.map(_.toJson)
      )

      AppResult(message, resultObj.toJson)
    }
  }

  def convertSapEmployeeSyncResult(syncResult: SapEmployeeSyncResult): AppResult = {
    val status = if (syncResult.isSuccessful) "succeeds" else "failed"
    val resultObj = AppResultSingleDetails(
      isSuccessful = syncResult.isSuccessful,
      columns = EmployeeResultColumns.ResultColumns,
      data = Seq(syncResult.toJson)
    )

    AppResult(s"Employee '${syncResult.employeeNumber}' sync $status.", resultObj.toJson)
  }

  def convertSapEmployeeSyncResults(syncResults: Seq[SapEmployeeSyncResult]): AppResult = {
    if (syncResults.size == 1) {
      convertSapEmployeeSyncResult(syncResults.head)
    } else {
      val successfulSyncNumber = syncResults.count(_.isSuccessful)
      val failedSyncNumber = syncResults.filter(!_.isSuccessful).map(_.employeeNumber).distinct.size
      val message = s"Success: $successfulSyncNumber employees. Failed: $failedSyncNumber employees."
      val resultObj = AppResultMultipleDetails(
        successfulCount = successfulSyncNumber,
        failedCount = failedSyncNumber,
        columns = EmployeeResultColumns.ResultColumns,
        data = syncResults.map(_.toJson)
      )

      AppResult(message, resultObj.toJson)
    }
  }

  def convertErrorsToSapEmployeeSyncResult(employeeNumber: String, errors: Seq[String]): Seq[SapEmployeeSyncResult] = {
    errors.zipWithIndex.map { case (error, index) => SapEmployeeSyncResult(s"${employeeNumber}_$index", employeeNumber, false, error.some)}
  }

  def convertErrorsToAppResult(employeeNumber: String, errors: Seq[String]): AppResult = {
    val appErrors = errors.zipWithIndex.map { case (error, index) => SapEmployeeSyncResult(index, employeeNumber, false, error.some)}
    convertSapEmployeeSyncResults(appErrors)
  }
}