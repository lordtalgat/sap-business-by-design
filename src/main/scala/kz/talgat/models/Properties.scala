package kz.talgat.models

import java.time.LocalDateTime


case class Properties(objectID: String,
                      projectID: String,
                      location_KUT: Option[String],
                      location_KUTText: Option[String],
                      projectLifeCycleStatusCode: Int,
                      projectLifeCycleStatusCodeText: String,
                      creationDateTime: Option[LocalDateTime],
                      lastChangeDateTime: Option[LocalDateTime],
                      projectName: String,
                      languageCode: String,
                      languageCodeText: String)
