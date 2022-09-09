package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.schema.annotation.{KoodistoUri, Tooltip}
import fi.oph.scalaschema.annotation.Description

@Description("Opintoihin liittyvien ulkomaanjaksojen tiedot. Ulkomaanjakson tiedot sisältävät alku- ja loppupäivämäärät, tiedon siitä, missä maassa jakso on suoritettu, sekä kuvauksen jakson sisällöstä.")
@Tooltip("Opintoihin liittyvien ulkomaanjaksojen tiedot. Ulkomaanjakson tiedot sisältävät alku- ja loppupäivämäärät, tiedon siitä, missä maassa jakso on suoritettu, sekä kuvauksen jakson sisällöstä. Voit lisätä useita ulkomaanjaksoja 'lisää uusi'-painikkeesta.")
case class Ulkomaanjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @Description("Missä maassa jakso on suoritettu")
  @Tooltip("Tieto siitä, missä maassa jakso on suoritettu.")
  @KoodistoUri("maatjavaltiot2")
  maa: Koodistokoodiviite,
  @Description("Kuvaus ulkomaanjakson sisällöstä. Esimerkiksi tieto siitä, opiskeleeko oppija ulkomaisessa oppilaitoksessa vai onko työharjoittelussa tai työssäoppimisessa.")
  @Tooltip("Kuvaus ulkomaanjakson sisällöstä. Esimerkiksi tieto siitä, opiskeleeko oppija ulkomaisessa oppilaitoksessa vai onko työharjoittelussa tai työssäoppimisessa.")
  kuvaus: LocalizedString
) extends Jakso
