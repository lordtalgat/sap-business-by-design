package kz.talgat.models

case class EmployerEssWidgets(id: Int,
                              employerId: Int,
                              widgets: Int)

object EmployerEssWidgets extends Serializable {
  val allWidgets: Int = 1

  val onboardingWidgetPosition: Int = 2
}
