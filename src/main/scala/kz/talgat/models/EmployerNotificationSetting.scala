package kz.talgat.models

case class EmployerNotificationSetting(id: Int,
                                       employerId: Int,
                                       eventTypeCd: Int,
                                       isActive: Boolean,
                                       isEmail: Boolean,
                                       isSMS: Boolean,
                                       isESS: Boolean)
