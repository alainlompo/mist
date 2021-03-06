package io.hydrosphere.mist.worker.runners

import io.hydrosphere.mist.Messages.JobMessages.JobParams
import io.hydrosphere.mist.jobs.resolvers.JobResolver
import io.hydrosphere.mist.worker.NamedContext
import io.hydrosphere.mist.worker.runners.python.PythonRunner
import io.hydrosphere.mist.worker.runners.scala.ScalaRunner

object MistJobRunner extends JobRunner {

  override def run(params: JobParams, context: NamedContext): Either[String, Map[String, Any]] = {
    val filePath = params.filePath
    val file = JobResolver.fromPath(filePath).resolve()
    if (!file.exists()) {
      Left(s"Can not found file locally: $file")
    } else {
      val specificRunner =  selectRunner(filePath)
      specificRunner.run(params, context)
    }
  }

  def selectRunner(filePath: String): JobRunner = {
    if (filePath.endsWith(".py"))
      new PythonRunner
    else if (filePath.endsWith(".jar"))
      new ScalaRunner
    else
      throw new IllegalArgumentException(s"Can not select runner for $filePath")
  }
}
