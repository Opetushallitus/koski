package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.Description

import java.time.LocalDate

@Description("Oppivelvollisen tukijaksot")
case class Tukijakso(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: Option[LocalDate],
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate]
) extends MahdollisestiAlkupäivällinenJakso
