package kz.talgat.models

import java.time.LocalDateTime

case class Project(id: String, // -> Project
                   title: String,
                   updated: Option[LocalDateTime],
                   category: Option[Category],
                   properties: Properties,
                   tasks: List[Task])

