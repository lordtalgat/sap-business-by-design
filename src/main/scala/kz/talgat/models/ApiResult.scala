package kz.talgat.models

trait ApiResult {
  val isSuccessful: Boolean

  val message: Option[String]
}
