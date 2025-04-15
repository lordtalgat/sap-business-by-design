package kz.talgat.helpers

object PersonnelEventTypeCode extends Enumeration{
  val MissingQualifications = Value("1")
  val Reorganization = Value("2")
  val EndOfLimitation = Value("3")
  val GrossMisconduct = Value("4")
  val LackingAbility = Value("5")
  val Redundancy = Value("6")
  val StatutoryReason = Value("7")

  def valueOfString(value: String):Value ={
    value match {
      case "B"|"W" => MissingQualifications
      case "M"|"Q"|"S" => Reorganization
      case _ => EndOfLimitation
    }
  }
}
