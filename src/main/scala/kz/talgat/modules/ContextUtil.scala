package kz.talgat.modules

import com.criterionhcm.apps.exceptions.AppException
import com.criterionhcm.apps.models.PluginContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.criterionhcm.play.util.DateTimeFormat

import scala.util.Try

object ContextUtil {
  private var ctx: PluginContext = _

  lazy val EndpointUrl: String = {
    Option(ctx.settings.get("EndpointUrl"))
      .filter(_.nonEmpty)
      .getOrElse(throw new AppException("Endpoint for service is not set"))
  }

  lazy val EmployerId: Int = {
    Option(ctx.settings.get("employerId"))
      .filter(_.nonEmpty)
      .map(_.toInt)
      .getOrElse(throw new AppException("EmployerId for service is not set"))
  }

  lazy val BasicLogin: String = {
    Option(ctx.settings.get("BasicLogin"))
      .filter(_.nonEmpty)
      .getOrElse(throw new AppException("Basic Login for service is not set"))
  }

  lazy val BasicPassword: String = {
    Option(ctx.settings.get("BasicPassword"))
      .filter(_.nonEmpty)
      .getOrElse(throw new AppException("Basic Password for service is not set"))
  }

  lazy val LoginODATA: String = {
    Option(ctx.settings.get("LoginODATA"))
      .filter(_.nonEmpty)
      .getOrElse(throw new AppException("Login ODATA for service is not set"))
  }

  lazy val PasswordODATA: String = {
    Option(ctx.settings.get("PasswordODATA"))
      .filter(_.nonEmpty)
      .getOrElse(throw new AppException("Password ODATA for service is not set"))
  }

  lazy val ProjectSyncDate: LocalDate = {
    Option(ctx.settings.get("ProjectSyncDate"))
      .filter(_.nonEmpty)
      .map(DateTimeFormat.parseLocalDate)
      .getOrElse(throw new AppException("Project Sync Date is not set"))
  }

  def init(context: PluginContext): Unit = {
    if (this.ctx == null) {
      this.ctx = context
    } else {
      throw new AppException("Plugin context can't be initialize twice")
    }
  }
}