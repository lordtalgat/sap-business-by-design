package kz.talgat.dao

import kz.talgat.daos.DAO
import kz.talgat.models.{Project, Properties}
import kz.talgat.companions.{Project => ProjectC}
import kz.talgat.modules.DaoUtil
import kz.talgat.util.Constants.CustomFields.PROJECT_LIFE_CYCLE_STATUS_CODE
import scalikejdbc.{sqls, _}

import java.time.LocalDateTime


trait ProjectDao {
  def getProjectList(employerId: EmployerId): List[Project]

  def insert(project: Project, employerId: EmployerId): Int

  def get(code: String): Option[Project]

  def getListByCode(codes: Seq[String]): List[String]

  def findIdByObjectId(objectId: String): Option[Int]

  def getProjectCodeList(employerId: EmployerId): List[String]
}

class ProjectDaoImpl(protected val dao: DAO)
  extends ProjectDao
    with DaoImpl {

  import DaoUtil.getDaos._

  override def getProjectList(employerId: EmployerId): List[Project] = localTxQuery { implicit session =>
    val pro = ProjectC.defaultAlias
    withSQL {
      select(
        pro.id,
        pro.code,
        pro.name,
        pro.description,
        pro.customValue1,
        pro.customValue2,
        pro.customValue3,
        pro.customValue4
      )
        .from(ProjectC as pro)
        .where(
          sqls.eq(pro.employerId, employerId)
        )
    }.map { e =>
      val projectId = e.int(1)
      val objectID = e.string(2)
      val projectName = e.string(3)
      val projectLifeCycleStatusCodeText = e.string(4)
      val location_KUT = e.stringOpt(5)
      val location_KUTText = e.stringOpt(6)
      val updated = e.localDateTimeOpt(7)
      val projectID = e.string(8)

      Project(
        id = objectID,
        title = projectName,
        updated = updated,
        category = None,
        properties = Properties(
          objectID = objectID,
          projectID = projectID,
          location_KUT = location_KUT,
          location_KUTText = location_KUTText,
          projectLifeCycleStatusCode = 5,
          projectLifeCycleStatusCodeText = projectLifeCycleStatusCodeText,
          creationDateTime = None,
          lastChangeDateTime = None,
          projectName = projectName,
          languageCode = "EN",
          languageCodeText = "English"),
        tasks = taskDao.getTaskList(projectId)
      )
    }.list().apply()
  }

  override def insert(project: Project, employerId: EmployerId): Int = localTxQuery { implicit session =>
    val c = ProjectC.column
    ProjectC.createWithNamedValues(
      c.employerId -> employerId,
      c.code -> project.properties.objectID,
      c.name -> project.properties.projectName,
      c.description -> project.properties.projectLifeCycleStatusCodeText,
      c.customValue1 -> project.properties.location_KUT,
      c.customValue2 -> project.properties.location_KUTText,
      c.customValue3 -> project.updated,
      c.customValue4 -> project.properties.projectID,
      c.isValuable -> false
    ).toInt
  }

  override def get(code: String): Option[Project] = localTxQuery { implicit session =>
    val pro = ProjectC.defaultAlias
    val sql = withSQL {
      select(
        pro.id,
        pro.code,
        pro.name,
        pro.description,
        pro.customValue1,
        pro.customValue2,
        pro.customValue3,
        pro.customValue4
      )
        .from(ProjectC as pro)
        .where(
          sqls.eq(pro.code, code)
        )
    }.collection

    createProject(sql)
  }

  override def findIdByObjectId(objectId: String): Option[Int] = localTxQuery { implicit session =>
    val pro = ProjectC.defaultAlias
    withSQL {
      select(
        pro.id
      )
        .from(ProjectC as pro)
        .where(
          sqls.eq(pro.code, objectId)
        )
    }.map { e =>
      e.int(1)
    }.list().apply().headOption
  }

  private def createProject(sql: SQLToCollection[Nothing, NoExtractor]): Option[Project] = localTxQuery { implicit session =>
    sql.map { e =>
      val projectId = e.int(1)
      val objectID = e.string(2)
      val projectName = e.string(3)
      val projectLifeCycleStatusCodeText = e.string(4)
      val location_KUT = e.stringOpt(5)
      val location_KUTText = e.stringOpt(6)
      val updated = e.localDateTimeOpt(7)
      val projectID = e.string(8)

      Project(
        id = objectID,
        title = projectName,
        updated = updated,
        category = None,
        properties = Properties(
          objectID = objectID,
          projectID = projectID,
          location_KUT = location_KUT,
          location_KUTText = location_KUTText,
          projectLifeCycleStatusCode = PROJECT_LIFE_CYCLE_STATUS_CODE,
          projectLifeCycleStatusCodeText = projectLifeCycleStatusCodeText,
          creationDateTime = None,
          lastChangeDateTime = None,
          projectName = projectName,
          languageCode = "EN",
          languageCodeText = "English"),
        tasks = taskDao.getTaskList(projectId)
      )
    }.list().apply().headOption
  }

  override def getListByCode(codes: Seq[String]): List[String] = localTxQuery { implicit session =>
    val pro = ProjectC.defaultAlias
    withSQL {
      select
        .from(ProjectC as pro)
        .where(
          sqls.in(pro.code, codes)
        )
    }.map(ProjectC(_).code).toList().apply()
  }

  override def getProjectCodeList(employerId: EmployerId): List[String] = localTxQuery { implicit session =>
    val pro = ProjectC.defaultAlias
    withSQL {
      select(
        pro.code
      )
        .from(ProjectC as pro)
        .where(
          sqls.eq(pro.employerId, employerId)
        )
    }.map { e =>
      e.string(1)
    }.list().apply()
  }
}
