package fi.oph.koski.config

import com.typesafe.config.Config

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class ConfigForFrontend(
  rajapäivät: Rajapäivät,
)

object ConfigForFrontend {
  def apply(config: Config): ConfigForFrontend = ConfigForFrontend(
    rajapäivät = Rajapäivät(config),
  )
}

case class Rajapäivät(
  ibLaajuusOpintopisteinäAlkaen: String,
)

object Rajapäivät {
  def apply(config: Config): Rajapäivät = Rajapäivät(
    ibLaajuusOpintopisteinäAlkaen = dateString(config, "validaatiot.ibLaajuudetOpintopisteinäAlkaen"),
  )

  // Tarkastaa että päivämäärä on validi ja varmistaa sen formaatin frontin ymmärtämässä muodossa
  def dateString(config: Config, key: String): String =
    LocalDate.parse(config.getString(key)).format(DateTimeFormatter.ISO_LOCAL_DATE)
}
