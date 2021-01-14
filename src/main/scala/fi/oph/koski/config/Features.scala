package fi.oph.koski.config

import com.typesafe.config.Config

case class Features(config: Config) {
  def shibboleth: Boolean = config.getBoolean("features.shibboleth")
  def valpas: Boolean = config.getBoolean("features.valpas")
}
