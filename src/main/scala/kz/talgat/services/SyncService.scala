package kz.talgat.services

import com.criterionhcm.db.models.{CodeTable, CodeTableDetail}
import com.criterionhcm.models.{Company, CostCenter, Employee, Project, SyncStatus}
import com.criterionhcm.modules.{ContextUtil, DaoUtil}
import com.criterionhcm.play.constants.CodeTableConstants
import com.criterionhcm.util.Constants.CustomFields
import com.typesafe.scalalogging.StrictLogging

trait SyncService {
  def syncEmployers(employerList: List[Company]): List[SyncStatus]

  def syncDepartments(departmentList: List[CostCenter]): List[SyncStatus]

  def syncProjects(projectList: List[Project]): List[SyncStatus]

  def syncEmployees(employeeList: List[Employee]): List[SyncStatus]

}

class SyncServiceImpl()
  extends SyncService
    with StrictLogging {

  import DaoUtil.getDaos._

  override def syncEmployers(employerList: List[Company]): List[SyncStatus] = {
    val existedEmployers = employerDao.listAll()
    val (toUpdate, toCreate) = employerList.partition(e => existedEmployers.map(_.legalName.replace(".", "")).contains(e.companyName.replace(".", "")))
    logger.info(s"SAP. ${toCreate.size} employers to create.")
    val resultCreated = toCreate.flatMap { emp =>
      val employerId = employerDao.create(emp.companyName)
      customValueEmployerDao.setCustomValues(employerId, emp)
      Some(SyncStatus(employerId, true, None))
    }

    logger.info(s"SAP. ${toUpdate.size} employers to update.")
    val resultUpdated = toUpdate.flatMap { emp =>
      existedEmployers.find(_.legalName.replace(".", "") == emp.companyName.replace(".", "")).flatMap { employer =>
        customValueEmployerDao.updateCustomValues(employer.id, emp)
        Some(SyncStatus(0, true, None))
      }
    }

    val total = resultCreated ++ resultUpdated

    logger.info("SAP. Sync Employers finished.")
    if (total.isEmpty) {
      List(SyncStatus(-1, true, None))
    } else {
      total
    }
  }

  override def syncDepartments(departmentList: List[CostCenter]): List[SyncStatus] = {
    val codeTableDepartment = codeTableDao.getByName(CodeTableConstants.CT_DEPARTMENT)
    val listExistingCode = codeTableDetailDao.listByCodeTableId(codeTableDepartment.id).map(_.code)
    val seqFilteredDepartments = departmentList.filterNot(e => listExistingCode.contains(e.id))
    val seqOfDepartments = generateDepart(seqFilteredDepartments, codeTableDepartment)
    val resList = codeTableDetailDao.insert(seqOfDepartments)

    logger.info("SAP. Sync Employer Departments finished.")
    if (resList.isEmpty) {
      List(SyncStatus(0, true, None))
    } else {
      resList.map(x => SyncStatus(x.toInt, true, None)).toList
    }
  }

  override def syncProjects(projectList: List[Project]): List[SyncStatus] = {
    val projectCodes = projectDao.getProjectCodeList(ContextUtil.EmployerId)
    val taskCodesMap = taskDao.getTaskCodesByEmployer(ContextUtil.EmployerId)

    val resultUpdate = projectList.flatMap { project =>
      taskCodesMap.get(project.properties.objectID).map { taskCodes =>
        val tasks = project.tasks.filterNot(x => taskCodes.contains(taskDao.generateCode(x.objectId, x.taskName)))
        if (tasks.nonEmpty) {
          projectDao.findIdByObjectId(project.properties.objectID).flatMap { id =>
            taskDao.insert(tasks, ContextUtil.EmployerId, id)
            Some(SyncStatus(0, true, Some(s"${tasks.size}")))
          }
        } else {
          None
        }
      }
    }.flatten

    val resultInsert = projectList.filterNot(x => projectCodes.contains(x.properties.objectID)).map { project =>
      val projectId = projectDao.insert(project, ContextUtil.EmployerId)
      val taskCodes = taskDao.getListByCode(project.tasks.map { el =>
        (el.objectId, el.taskName)
      })
      val tasks = project.tasks.filterNot(x => taskCodes.contains(x.objectId))
      if (tasks.nonEmpty) {
        taskDao.insert(tasks, ContextUtil.EmployerId, projectId)
      }
      SyncStatus(projectId, true, Some(s"${tasks.size}"))
    }

    val total = resultInsert ++ resultUpdate

    if (total.isEmpty) {
      List(SyncStatus(-1, true, None))
    } else {
      total
    }
  }

  override def syncEmployees(employeeList: List[Employee]): List[SyncStatus] = {
    val gender = codeTableDetailDao.listByCodeTableCode(CodeTableConstants.CT_GENDER)
    val codeTableWorkFlow = codeTableDetailDao.getByCode(CodeTableConstants.WORKFLOW_STATE_APPROVED, CodeTableConstants.CT_WORKFLOW_STATE)
    val mapCountry = codeTableDetailDao.listByCodeTableCode(CodeTableConstants.CT_COUNTRY).map(elem => elem.attribute1.getOrElse(elem.code) -> elem.id).toMap
    val employeeNumbers = employeeDao.getEmployeeNumbers(ContextUtil.EmployerId)
    val addressLocationCd = codeTableDetailDao.getByCode(CustomFields.SAP_NONE, CodeTableConstants.CT_ADDRESS_LOCATION).id

    def getGenderCode(code: Int): Int = {
      code match {
        case 0 => gender.find(_.code.equals("M")).map(_.id).head
        case _ => gender.find(_.code.equals("F")).map(_.id).head
      }
    }

    val employeeListFiltered = employeeList.filterNot(x => employeeNumbers.contains(x.employeeId.toString))

    employeeListFiltered.map { emp =>
      val employeeId = employeeDao.insert((emp, getGenderCode(emp.genderCode), codeTableWorkFlow.id), employerId = ContextUtil.EmployerId, mapCountry)(addressLocationCd)
      SyncStatus(employeeId.toInt, true, None)

    }

  }

  private def generateDepart(departmentList: List[CostCenter], codeTableDepartment: CodeTable): Seq[CodeTableDetail] = {
    val existingDepartments = codeTableDao.getListByNames(codeTableDepartment.id, departmentList.map(_.id))
    departmentList.filterNot(dep => existingDepartments.contains(dep.id)).map { dep =>
      CodeTableDetail(id = 0,
        codeTableId = codeTableDepartment.id,
        code = dep.id,
        description = dep.id + " Need manual setting",
        attribute1 = Some(dep.validPeriod.startDate.toString),
        attribute2 = Some(dep.validPeriod.endDate.toString),
        attribute3 = None,
        attribute4 = None,
        attribute5 = None,
        isDefault = false,
        isActive = true)
    }
  }
}
