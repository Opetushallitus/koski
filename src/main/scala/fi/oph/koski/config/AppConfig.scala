package fi.oph.koski.config

import com.typesafe.config.Config

object AppConfig {
  def virkailijaOpintopolkuUrl(config: Config, path: String*): Option[String] =
    Option(config.getString("opintopolku.virkailija.url"))
      .flatMap(ignoreMock)
      .map(_ + path.mkString(""))

  def environmentName(config: Config): Option[String] =
    Option(config.getString("opintopolku.environment")).flatMap(ignoreMock)

  def oppijaOpintopolkuUrl(config: Config): String =
    config.getString("opintopolku.oppija.url")

  def ophService(config: Config, service: String): Option[String] =
    virkailijaOpintopolkuUrl(config)

  private def ignoreMock(url: String): Option[String] =
    if (url == "mock") None else Some(url)
}

object OphServiceUrls {
  def koodisto(config: Config): Option[String] = AppConfig.ophService(config, "koodisto")
  def organisaatiot(config: Config): Option[String] = AppConfig.ophService(config, "organisaatio-service")
  def oppijanumerorekisteri(config: Config): Option[String] = AppConfig.ophService(config, "oppijanumerorekisteri-service")
}
