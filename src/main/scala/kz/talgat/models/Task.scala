package kz.talgat.models

import java.time.LocalDateTime

case class Task(objectId: String,
                parentObjectId: String,
                location_KUT: Option[String],
                location_KUTText: Option[String],
                taskName: String,
                languageCode: String,
                languageCodeText: String,
                creationDateTime: Option[LocalDateTime],
                lastChangeDateTime: Option[LocalDateTime])
