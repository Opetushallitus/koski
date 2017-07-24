package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.Description

@Description("Ulkomaanjakson tiedot sisältävät alku- ja loppupäivämäärät, sekä tiedon siitä, missä maassa jakso on suoritettu")
case class Ulkomaanjakso(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: LocalDate,
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate],
  @Description("Missä maassa jakso on suoritettu")
  @KoodistoUri("maatjavaltiot2")
  maa: Koodistokoodiviite,
  @Description("Kuvaus ulkomaanjakson sisällöstä. Esimerkiksi tieto siitä, että opiskeleeko oppija ulkomaisessa oppilaitoksessa vai onko työharjoittelussa tai työssäoppimisessa.")
  kuvaus: LocalizedString
) extends Jakso


