package kz.talgat.helpers

import java.time.{LocalDate, LocalDateTime, ZoneId}
import java.time.format.{DateTimeFormatter}
import scala.util.Try

object DateFormatHelper {
  val defaultDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE
  val defaultDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))
  val longDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  def convertToLocalDate(dateStr: String, fieldName: String): LocalDate = {
    Try {
      LocalDate.parse(dateStr, defaultDateFormatter)
    }.getOrElse {
      throw new RuntimeException(s"Invalid date time value '$dateStr' for '$fieldName' node. Date time format should be '$defaultDateFormatter'")
    }
  }

  def convertToLocalDateTime(dateStr: String, fieldName: String): LocalDateTime = {
    Try {
      LocalDateTime.parse(dateStr, defaultDateTimeFormatter)
    }.getOrElse {
      throw new RuntimeException(s"Invalid date time value '$dateStr' for '$fieldName' node. Date time format should be '$defaultDateTimeFormatter'")
    }
  }

  def convertLocalDateTimeToString(date: LocalDateTime, filter:String): String = {
    Try {
      date.format(DateTimeFormatter.ISO_DATE_TIME)
    }.getOrElse {
      throw new RuntimeException(s"Invalid date time value 'LocalDateTime'. Filter was '$filter'")
    }
  }

  def convertLocalDateToString(date: LocalDate, filter:String): String = {
    Try {
      date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrElse {
      throw new RuntimeException(s"Invalid date time value 'LocalDateTime'. Filter was '$filter'")
    }
  }
}
