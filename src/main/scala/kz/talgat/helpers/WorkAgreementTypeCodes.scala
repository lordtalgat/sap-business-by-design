package kz.talgat.helpers

object WorkAgreementTypeCodes extends Enumeration {
  val Permanent = Value("1")
  val Executive = Value("2")
  val Retiree = Value("3")
  val Temporary = Value("4")
  val Trainee = Value("5")
  val WorkingStudent = Value("6")

  def valueOfString(value: String): Value = {
    value match { // "FT"|"RFT"|"RT"|"TEM"|"INT"|"PT"|"RPT"|"CONS"
      case "TEM" => Temporary
      case "INT" => Trainee
      case "CONS" => Retiree
      case _ => Permanent
    }
  }
}
