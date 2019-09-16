package fi.oph.koski.util

import scala.util.Properties

trait EnvVariables {
  def requiredEnv(name: String, explanation: String = ""): String = optEnv(name).getOrElse(throw new IllegalStateException(s"Environment property $name missing. $explanation"))
  def env(name: String, defaultValue: String): String = optEnv(name).getOrElse(defaultValue)
  def optEnv(name: String): Option[String] = Properties.envOrNone(name).orElse(Properties.propOrNone(name))
}
