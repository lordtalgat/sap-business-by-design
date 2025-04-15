package kz.talgat.helpers

object AdministrativeCategoryCode extends Enumeration {
  val SalariedEmployee = Value("2")
  val Hourly = Value("1")
  val Manager = Value("3")

  def valueOfBoolean(value: Boolean): Value = {
    value match {
      case true => SalariedEmployee
      case _ => Hourly
    }
  }
}
