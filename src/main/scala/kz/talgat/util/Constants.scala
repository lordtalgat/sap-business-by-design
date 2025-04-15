package kz.talgat.util

object Constants {

  object CustomFields {

    // Employer Fields
    val SAP_COMPANY_ID_CODE = "SAP_COMPANY_ID"
    val SAP_COMPANY_ID_LABEL = "SAP Company Id"
    val SAP_COMPANY_UUID_CODE = "SAP_COMPANY_UUID"
    val SAP_COMPANY_UUID_LABEL = "SAP Company UUID"
    val SAP_CHANGE_STATE_ID_CODE = "SAP_CHANGE_STATE_ID"
    val SAP_CHANGE_STATE_ID_LABEL = "SAP Change State Id"

    // Position Fields
    val SAP_WORK_AGREEMENT_TYPE_CODE = "SAP_WORK_AGREEMENT_TYPE"
    val SAP_WORK_AGREEMENT_TYPE_LABEL = "SAP Work Agreement Type"
    val SAP_ADMINISTRATIVE_CATEGORY_CODE = "SAP_ADMINISTRATIVE_CATEGORY"
    val SAP_ADMINISTRATIVE_CATEGORY_LABEL = "SAP Administrative Category"
    val SAP_BASE_MEASURE_UNIT_CODE_CODE = "SAP_BASE_MEASURE_UNIT_CODE"
    val SAP_BASE_MEASURE_UNIT_CODE_LABEL = "SAP Base Measure Unit Code"

    final val SAP_NONE = "NONE"
    final val PROJECT_LIFE_CYCLE_STATUS_CODE = 5
    final val SAP_DEFAULT_TEXT = ""

    // TODO: need review all code usage
    val AllCodes: Seq[String] = Seq(
      SAP_COMPANY_ID_CODE,
      SAP_COMPANY_UUID_CODE,
      SAP_CHANGE_STATE_ID_CODE
    )

    val AllFields: Seq[PredefinedCustomField] = Seq(
      PredefinedCustomField(SAP_COMPANY_ID_CODE, SAP_COMPANY_ID_LABEL),
      PredefinedCustomField(SAP_COMPANY_UUID_CODE, SAP_COMPANY_UUID_LABEL),
      PredefinedCustomField(SAP_CHANGE_STATE_ID_CODE, SAP_CHANGE_STATE_ID_LABEL)
    )

    val AllPositionCodes: Seq[String] = Seq(
      SAP_WORK_AGREEMENT_TYPE_CODE,
      SAP_ADMINISTRATIVE_CATEGORY_CODE,
      SAP_BASE_MEASURE_UNIT_CODE_CODE
    )

    val AllPositionFields: Seq[PredefinedCustomField] = Seq(
      PredefinedCustomField(SAP_WORK_AGREEMENT_TYPE_CODE, SAP_WORK_AGREEMENT_TYPE_LABEL, Option(CustomTables.SAP_WORK_AGREEMENT_TYPES_NAME)),
      PredefinedCustomField(SAP_ADMINISTRATIVE_CATEGORY_CODE, SAP_ADMINISTRATIVE_CATEGORY_LABEL, Option(CustomTables.SAP_ADMINISTRATIVE_CATEGORIES_NAME)),
      PredefinedCustomField(SAP_BASE_MEASURE_UNIT_CODE_CODE, SAP_BASE_MEASURE_UNIT_CODE_LABEL, Option(CustomTables.SAP_BASE_MEASURE_UNIT_CODES_NAME))
    )
  }

  object CustomTables {
    // Custom tables on Position level
    val SAP_WORK_AGREEMENT_TYPES_NAME = "SAP_WORK_AGREEMENT_TYPES"
    val SAP_WORK_AGREEMENT_TYPES_DESCRIPTION = "SAP Work Agreement Types"
    val SAP_ADMINISTRATIVE_CATEGORIES_NAME = "SAP_ADMINISTRATIVE_CATEGORIES"
    val SAP_ADMINISTRATIVE_CATEGORIES_DESCRIPTION = "SAP Administrative Categories"
    val SAP_BASE_MEASURE_UNIT_CODES_NAME = "SAP_BASE_MEASURE_UNIT_CODES"
    val SAP_BASE_MEASURE_UNIT_CODES_DESCRIPTION = "SAP Base Measure Unit Codes"


    val WorkAgreementValues: Seq[PredefinedCodeTableDetailValue] = Seq(
      PredefinedCodeTableDetailValue("1", "Permanent", isDefault = true),
      PredefinedCodeTableDetailValue("2", "Temporary", isDefault = false),
      PredefinedCodeTableDetailValue("3", "Executive", isDefault = false),
      PredefinedCodeTableDetailValue("4", "Trainee", isDefault = false),
      PredefinedCodeTableDetailValue("5", "Working student", isDefault = false),
      PredefinedCodeTableDetailValue("6", "Retiree", isDefault = false)
    )

    val AdministrativeCategoriesValues: Seq[PredefinedCodeTableDetailValue] = Seq(
      PredefinedCodeTableDetailValue("1", "Hourly", isDefault = true),
      PredefinedCodeTableDetailValue("2", "Salaried Employee", isDefault = false),
      PredefinedCodeTableDetailValue("3", "Manager", isDefault = false)
    )

    val BaseMeasureUnitCodesValues: Seq[PredefinedCodeTableDetailValue] = Seq(
      PredefinedCodeTableDetailValue("WEE", "Week", isDefault = true),
      PredefinedCodeTableDetailValue("DAY", "Day", isDefault = false),
      PredefinedCodeTableDetailValue("MON", "Month", isDefault = false)
    )

    val AllTableNames: Seq[String] = Seq(SAP_WORK_AGREEMENT_TYPES_NAME, SAP_ADMINISTRATIVE_CATEGORIES_NAME, SAP_BASE_MEASURE_UNIT_CODES_NAME)
    val AllTables: Seq[PredefinedCustomTable] = Seq(
      PredefinedCustomTable(SAP_WORK_AGREEMENT_TYPES_NAME, SAP_WORK_AGREEMENT_TYPES_DESCRIPTION, WorkAgreementValues),
      PredefinedCustomTable(SAP_ADMINISTRATIVE_CATEGORIES_NAME, SAP_ADMINISTRATIVE_CATEGORIES_DESCRIPTION, AdministrativeCategoriesValues),
      PredefinedCustomTable(SAP_BASE_MEASURE_UNIT_CODES_NAME, SAP_BASE_MEASURE_UNIT_CODES_DESCRIPTION, BaseMeasureUnitCodesValues)
    )
  }
}

case class PredefinedCustomField(code: String, label: String, codeTableName: Option[String] = None)

case class PredefinedCustomTable(name: String, description: String, values: Seq[PredefinedCodeTableDetailValue])

case class PredefinedCodeTableDetailValue(code: String, description: String, isDefault: Boolean)