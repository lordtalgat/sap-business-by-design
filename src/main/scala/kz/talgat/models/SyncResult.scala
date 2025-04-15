package kz.talgat.models

import cats.syntax.option._
import com.criterionhcm.apps.models.ApiResult

case class SyncResult(isSuccessful: Boolean,
                      message: Option[String])
  extends ApiResult

object SyncResult {

  def successful: SyncResult = SyncResult(isSuccessful = true, none)

  def successful(message: String): SyncResult = SyncResult(isSuccessful = true, message.some)

  def failed(error: String) = SyncResult(isSuccessful = false, error.some)

  def failed(errors: Seq[String]) = SyncResult(isSuccessful = false, errors.mkString("; ").some)
}
