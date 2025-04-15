package kz.talgat.models

case class SyncStatus(id: Int,
                      success: Boolean,
                      error: Option[String])