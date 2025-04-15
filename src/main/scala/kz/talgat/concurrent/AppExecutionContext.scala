package kz.talgat.concurrent

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object AppExecutionContext {

  implicit lazy val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(3))
}
