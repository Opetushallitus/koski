package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.Description

@Description("Ulkomaanjakson tiedot sisältävät alku- ja loppupäivämäärät, sekä tiedon siitä, missä maassa jakso on suoritettu")
case class Ulkomaanjakso(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: LocalDate,
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate],
  @KoodistoUri("maatjavaltiot2")
  maa: Koodistokoodiviite
) extends Jakso


