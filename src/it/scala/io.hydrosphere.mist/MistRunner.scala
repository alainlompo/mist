package io.hydrosphere.mist

import org.scalatest._
import scala.sys.process._

trait MistRunner {

  private def getProperty(name: String): String = sys.props.get(name) match {
    case Some(v) => v
    case None => throw new RuntimeException(s"Property $name is not set")
  }


  val sparkHome = getProperty("sparkHome")
  val jar = getProperty("mistJar")
  val sparkVersion = getProperty("sparkVersion")

  def runMist(configPath: String): Process = {

    val reallyConfigPath = getClass.getClassLoader.getResource(configPath).getPath
    val args = Seq(
      "./bin/mist", "start", "master",
      "--jar", jar,
      "--config", reallyConfigPath)

    val env = sys.env.toSeq :+ ("SPARK_HOME" -> sparkHome)
    val ps = Process(args, None, env: _*).run(new ProcessLogger {
      override def buffer[T](f: => T): T = f

      override def out(s: => String): Unit = ()

      override def err(s: => String): Unit = ()
    })
    Thread.sleep(5000)
    ps
  }

  def killMist(): Unit ={
    Process("./bin/mist stop", None, "SPARK_HOME" -> sparkHome).run(false).exitValue()
  }
}

object MistRunner extends MistRunner

trait MistItTest extends BeforeAndAfterAll with MistRunner { self: Suite =>

  val configPath: String
  private var ps: Process = null

  override def beforeAll {
    ps = runMist(configPath)
  }

  override def afterAll: Unit = {
    // call kill over bash - destroy works strangely
    ps.destroy()
    killMist()
    Thread.sleep(5000)
  }

  def isSpark2: Boolean = sparkVersion.startsWith("2.")
  def isSpark1: Boolean = !isSpark2

  def runOnlyIf(f: => Boolean, descr: String)(body: => Unit) = {
    if (f) body
    else cancel(descr)
  }

  def runOnlyOnSpark2(body: => Unit): Unit =
    runOnlyIf(isSpark2, "SKIP TEST - ONLY FOR SPARK2")(body)

  def runOnlyOnSpark1(body: => Unit): Unit =
    runOnlyIf(isSpark1, "SKIP TEST - ONLY FOR SPARK1")(body)
}

