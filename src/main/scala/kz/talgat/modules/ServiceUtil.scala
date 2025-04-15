package kz.talgat.modules

import com.criterionhcm.apps.exceptions.AppException
import com.criterionhcm.services.{EmployeeSyncService, EmployeeSyncServiceImpl, SoapVerificationServiceImpl, SyncService, SyncServiceImpl}
import play.api.libs.ws.WSClient


object ServiceUtil {
  private var services: Services = _

  def init(wsClient: WSClient,
           daos: Daos,
           ctxUtil: ContextUtil.type): Unit = {
    if (this.services == null) {
      this.services = new Services(wsClient, daos, ctxUtil)
    } else {
      throw new AppException("Service can't be initialized twice")
    }
  }

  lazy val getServices: Services = services
}

class Services(wsClient: WSClient,
               daos: Daos,
               ctxUtil: ContextUtil.type) {
  import daos._

  lazy val soapVerificationService: SoapVerificationServiceImpl = new SoapVerificationServiceImpl(wsClient)
  lazy val employeeSyncService: EmployeeSyncService = new EmployeeSyncServiceImpl(soapVerificationService, employeeDao)
  lazy val syncService: SyncService = new SyncServiceImpl()
}