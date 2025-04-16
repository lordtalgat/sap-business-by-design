package kz.talgat.models

case class EmployerTask(id: Int,
                        employerId: Int,
                        code: String,
                        name: String,
                        description: Option[String],
                        customValue1: Option[String],
                        customValue2: Option[String],
                        customValue3: Option[String],
                        customValue4: Option[String],
                        isActive: Boolean)
