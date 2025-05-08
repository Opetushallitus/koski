package fi.oph.koski.schema

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoUri, OksaUri, RedundantData, SensitiveData, Tooltip}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate

@Description("Oppivelvollisen tukijaksot")
case class Tukijakso(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: Option[LocalDate],
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate]
) extends MahdollisestiAlkupäivällinenJakso
