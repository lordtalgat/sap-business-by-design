package kz.talgat.modules

import com.criterionhcm.apps.dao.DAO
import com.criterionhcm.apps.exceptions.AppException
import com.criterionhcm.dao._

object DaoUtil {
  private var daos: Daos = _

  def init(dao: DAO): Unit = {
    if (this.daos == null) {
      this.daos = new Daos(dao)
    } else {
      throw new AppException("Daos can't be initialized twice")
    }
  }

  lazy val getDaos: Daos = daos
}

class Daos(dao: DAO) {
  val employeeDao: EmployeeDao = new EmployeeDaoImpl(dao)
  val codeTableDao: CodeTableDao = new CodeTableDaoImpl(dao)
  val codeTableDetailDao: CodeTableDetailDao = new CodeTableDetailDaoImpl(dao)
  val customFieldDao: CustomFieldDao = new CustomFieldDaoImpl(dao)
  val customValueEmployerDao: CustomValueEmployerDao = new CustomValueEmployerDaoImpl(dao)
  val employerDao: EmployerDao = new EmployerDaoImpl(dao)
  val projectDao: ProjectDao = new ProjectDaoImpl(dao)
  val taskDao: TaskDao = new TaskDaoImpl(dao)
}