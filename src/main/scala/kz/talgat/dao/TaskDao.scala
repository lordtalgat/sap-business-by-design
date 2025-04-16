package kz.talgat.dao

import kz.talgat.daos.DAO
import kz.talgat.models.Task
import kz.talgat.companions.{EmployerTask => EmployerTaskC, Project => ProjectC, ProjectTask => ProjectTaskC}
import scalikejdbc.{sqls, _}

trait TaskDao {
  def getTaskList(projectId: Int): List[Task]

  def insert(task: Task, employerId: EmployerId, projectId: ProjectId): Int

  def insert(tasks: List[Task], employerId: EmployerId, projectId: ProjectId): List[Int]

  def getListByCode(codes: Seq[(String, String)]): List[String]

  def generateCode(id: String, name: String): String

  def getTaskCodesByEmployer(employerId: EmployerId): Map[String, List[String]]
}


class TaskDaoImpl(protected val dao: DAO)
  extends TaskDao
    with DaoImpl {

  override def getTaskList(projectId: ProjectId): List[Task] = localTxQuery { implicit session =>
    val eet = EmployerTaskC.defaultAlias
    val pt = ProjectTaskC.defaultAlias
    withSQL {
      select(
        eet.code,
        eet.name,
        eet.description
      )
        .from(EmployerTaskC as eet)
        .innerJoin(ProjectTaskC as pt).on(eet.id, pt.taskId)
        .where(
          sqls.eq(pt.projectId, projectId)
        )
    }.map { e =>
      val objectId = e.string(1)
      val taskName = e.string(2)
      val parentObjectId = e.string(3).replace("ParentObjectID=", "")

      Task(
        objectId = objectId,
        parentObjectId = parentObjectId,
        location_KUT = None,
        location_KUTText = None,
        taskName = taskName,
        languageCode = "EN",
        languageCodeText = "English",
        creationDateTime = None,
        lastChangeDateTime = None
      )
    }.list().apply()
  }

  override def insert(task: Task, employerId: EmployerId, projectId: ProjectId): Int = localTxQuery { implicit session =>
    val c = EmployerTaskC.column
    val pt = ProjectTaskC.column

    val taskId = withSQL {
      insertInto(EmployerTaskC)
        .namedValues(
          c.employerId -> employerId,
          c.code -> generateCode(task.objectId, task.taskName),
          c.name -> task.taskName,
          c.description -> s"ParentObjectID=${task.parentObjectId}")
    }.updateAndReturnGeneratedKey().apply().toInt

    withSQL {
      insertInto(ProjectTaskC)
        .namedValues(
          pt.taskId -> taskId,
          pt.projectId -> projectId
        )
    }.update().apply()

    taskId
  }

  override def insert(tasks: List[Task], employerId: EmployerId, projectId: ProjectId): List[Int] = localTxQuery { implicit session =>
    val c = EmployerTaskC.column
    val ptColumn = ProjectTaskC.column
    val batchParams: Seq[Seq[Any]] = tasks.map { task =>

      val taskId = withSQL {
        insertInto(EmployerTaskC)
          .namedValues(
            c.employerId -> employerId,
            c.code -> generateCode(task.objectId, task.taskName),
            c.name -> task.taskName,
            c.description -> s"ParentObjectID=${task.parentObjectId}",
            c.customValue1 -> task.objectId
          )
      }.updateAndReturnGeneratedKey().apply().toInt


      Seq(taskId, projectId)
    }

    withSQL {
      QueryDSL.insert
        .into(ProjectTaskC)
        .namedValues(
          ptColumn.taskId -> sqls.?,
          ptColumn.projectId -> sqls.?
        )
    }.batch(batchParams: _*).apply()
  }

  override def getListByCode(codes: Seq[(String, String)]): List[String] = localTxQuery { implicit session =>
    val generateCodes = codes.map(el => generateCode(el._1, el._2))
    val tsk = EmployerTaskC.defaultAlias
    withSQL {
      select
        .from(EmployerTaskC as tsk)
        .where(
          sqls.in(tsk.code, generateCodes)
        )
    }.map(EmployerTaskC(_).customValue1.getOrElse("")).toList().apply()
  }

  override def generateCode(id: String, name: String): String = {
    val codeSum = id.toCharArray.foldLeft(0) { case (acc, char) =>
      acc + char.toInt
    }
    val nameSum = name.trim.toCharArray.foldLeft(0) { case (acc, char) =>
      acc + char.toInt
    }
    val total = name.trim.toLowerCase.replace(" ", "-")

    if (total.length > 90) {
      total.substring(0, 90) + "-" + codeSum + "-" + nameSum
    } else {
      total + "-" + codeSum + "-" + nameSum
    }
  }

  override def getTaskCodesByEmployer(employerId: EmployerId): Map[String, List[String]] = localTxQuery { implicit session =>
    val eet = EmployerTaskC.defaultAlias
    val pro = ProjectC.defaultAlias
    val pt = ProjectTaskC.defaultAlias
    val sql = withSQL {
      select(
        pro.code,
        eet.code
      )
        .from(EmployerTaskC as eet)
        .innerJoin(ProjectTaskC as pt).on(eet.id, pt.taskId)
        .innerJoin(ProjectC as pro).on(pt.projectId, pro.id)
        .where(
          sqls.eq(pro.employerId, employerId)
        )
    }.map { e =>
      val projectCode = e.string(1)
      val taskCode = e.string(2)
      (projectCode, taskCode)

    }.list().apply()
    sql.groupBy(_._1).map(e => e._1 -> e._2.map(_._2))
  }
}
