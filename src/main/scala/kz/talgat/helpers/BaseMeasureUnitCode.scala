package kz.talgat.helpers

object BaseMeasureUnitCode extends Enumeration {
  val Day = Value("DAY")
  val Month = Value("MON")
  val Week = Value("WEE")

  def valueOfString(value: String):Value ={
    value match {
      case "B"|"W" => Week
      case "M"|"Q"|"S" => Month
      case _ => Day
    }
  }

}
