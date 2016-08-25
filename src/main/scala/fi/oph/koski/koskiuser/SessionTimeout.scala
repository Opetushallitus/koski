package fi.oph.koski.koskiuser

import com.typesafe.config.Config

case class SessionTimeout(config: Config) {
  val minutes: Int = config.getInt("sessionTimeoutMinutes")
  def seconds = minutes * 60
  val milliseconds = seconds * 1000
}
