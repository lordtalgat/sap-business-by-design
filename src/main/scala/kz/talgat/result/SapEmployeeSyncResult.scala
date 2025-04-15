package kz.talgat.result

import com.criterionhcm.apps.models.AppDetailBase
import com.criterionhcm.play.util.JsonParametersParser._
import play.api.libs.json.{JsObject, JsValue, Json}

case class SapEmployeeSyncResult(id: String,
                                 employeeNumber: String,
                                 isSuccessful: Boolean,
                                 message: Option[String])
  extends AppDetailBase {

  override def toJson: JsObject = {
    super.toJson ++
      Json.obj(
        "employeeNumber" -> employeeNumber,
        "message" -> message
      )
  }
}

object SapEmployeeSyncResult {

  def apply(id: Int, employeeNumber: String, isSuccessful: Boolean, message: Option[String]): SapEmployeeSyncResult = {
    new SapEmployeeSyncResult(id.toString, employeeNumber, isSuccessful, message)
  }

  def apply(json: JsValue): SapEmployeeSyncResult = {
    SapEmployeeSyncResult(
      parseString("id")(json),
      parseString("employeeNumber")(json),
      parseBoolean("isSuccessful")(json),
      parseOptionalString("message")(json)
    )
  }
}