package fi.oph.koski.config

import com.typesafe.config.Config

case class Features(config: Config) {
  def valpas: Boolean = config.getBoolean("features.valpas")
}
