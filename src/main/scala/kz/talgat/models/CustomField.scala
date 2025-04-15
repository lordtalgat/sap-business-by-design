package kz.talgat.models

case class CustomField(id: Int,
                       entityTypeCd: Int,
                       code: Option[String],
                       label: String,
                       dataTypeCd: Int,
                       codeTableId: Option[Int],
                       maximumSize: Int,
                       sequenceNumber: Int,
                       fieldFormatTypeId: Option[Int],
                       isHidden: Boolean,
                       showInEss: Boolean,
                       isRequired: Boolean,
                       formula: Option[String])
