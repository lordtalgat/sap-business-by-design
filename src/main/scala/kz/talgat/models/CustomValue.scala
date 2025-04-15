package kz.talgat.models

case class CustomValue(id: Int,
                       customFieldId: Int,
                       entityId: Int,
                       value: Option[String])
