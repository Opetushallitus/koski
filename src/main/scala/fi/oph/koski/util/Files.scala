package fi.oph.koski.util

import fi.oph.koski.log.Logging

import scala.io.{BufferedSource, Source}
import scala.reflect.io.File

object Files extends Logging {
  def exists(filename: String) = asSource(filename).isDefined
  def asByteArray(filename: String): Option[Array[Byte]] = asSource(filename).map(_.takeWhile(_ != -1).map(_.toByte).toArray)
  def asString(filename: String): Option[String] = asSource(filename).map(_.mkString)
  def asSource(filename: String) = {
    loadFile(filename)
  }

  private def loadFile(filename: String): Option[BufferedSource] = {
    File(filename).exists match {
      case true => Some(Source.fromFile(filename))
      case false => None
    }
  }

}

trait FileOps {
  def exists(filename: String) = asSource(filename).isDefined
  def asByteArray(filename: String): Option[Array[Byte]] = asSource(filename).map(_.takeWhile(_ != -1).map(_.toByte).toArray)
  def asString(filename: String): Option[String] = asSource(filename).map(_.mkString)
  def asSource(filename: String): Option[Source]
}