package fi.oph.koski.config

import com.typesafe.config.Config

case class Features(config: Config) {
  def shibboleth: Boolean = config.getBoolean("features.shibboleth")
  def tiedonsiirtomail: Boolean = config.getBoolean("features.tiedonsiirtomail")
  def luovutuspalvelu: Boolean = config.getBoolean("features.luovutuspalvelu")
}
