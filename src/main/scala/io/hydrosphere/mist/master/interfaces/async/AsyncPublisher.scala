package io.hydrosphere.mist.master.interfaces.async

import akka.actor.Actor
import io.hydrosphere.mist.jobs.JobResult
import io.hydrosphere.mist.utils.Logger
import io.hydrosphere.mist.utils.json.JobConfigurationJsonSerialization
import spray.json.pimpAny

trait AsyncPublisher extends Actor with Logger with JobConfigurationJsonSerialization {

  override def receive: Receive = {
    case jobResult: JobResult =>
      logger.info(jobResult.toString)
      logger.info(jobResult.toJson.compactPrint)
      val jsonString = jobResult.toJson.compactPrint
      send(jsonString)
    case string: String =>
      send(string)
  }

  def send(message: String): Unit
}
