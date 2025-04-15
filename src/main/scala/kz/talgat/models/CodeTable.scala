package kz.talgat.models

case class CodeTable(id: Int,
                     name: String,
                     description: String,
                     isSystem: Boolean,
                     isCustom: Boolean,
                     attribute1Caption: Option[String],
                     attribute2Caption: Option[String],
                     attribute3Caption: Option[String],
                     attribute4Caption: Option[String],
                     attribute5Caption: Option[String])
