package kz.talgat.helpers

object PersonnelEventReasonCode extends Enumeration {
  val BetterCareerOpportunities = Value("1")
  val DeathOfEmployee = Value("2")
  val DisturbedWorkingRelationship = Value("3")
  val HealthReasons = Value("4")
  val ManagementStyle = Value("5")
  val PersonalOrFamilyCircumstances = Value("6")
  val DecisionToStudy = Value("7")
  val Salary = Value("8")
  val Workload = Value("9")
  val Other = Value("10")

  def valueOfString(value: String):Value ={
    value match {
      case "B"|"W" => HealthReasons
      case "M"|"Q"|"S" => PersonalOrFamilyCircumstances
      case _ => DisturbedWorkingRelationship
    }
  }
}
