package fi.oph.koski.schema

import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{DefaultValue, Description}

@Description("Tiedot aiemmin hankitun osaamisen tunnustamisesta.")
@OksaUri("tmpOKSAID629", "osaamisen tunnustaminen")
case class OsaamisenTunnustaminen(
  @Description("Aiemman, korvaavan suorituksen tiedot")
  @FlattenInUI
  osaaminen: Option[Suoritus],
  selite: LocalizedString,
  @Description("Käytetään, mikäli tunnustettu osaaminen kuuluu rahoitukseen piiriin (esimerkiksi kaksoistutkintolaisilla ammatilliseen tutkintoon tunnustetut lukio-opinnot tai toiselta oppilaitokselta ostetut yksittäiset tutkinnon osat).")
  @Tooltip("Tunnustettu osaaminen kuuluu rahoitukseen piiriin (esimerkiksi kaksoistutkintolaisilla ammatilliseen tutkintoon tunnustetut lukio-opinnot tai toiselta oppilaitokselta ostetut yksittäiset tutkinnon osat).")
  @DefaultValue(false)
  rahoituksenPiirissä: Boolean = false
) extends SelitettyOsaamisenTunnustaminen

trait SelitettyOsaamisenTunnustaminen {
  @Description("Osaamisen tunnustamisen kautta saatavan tutkinnon osan suorituksen selite.")
  @Tooltip("Kuvaus siitä, miten aikaisemmin hankittu osaaminen on tunnustettu.")
  @OksaUri("tmpOKSAID629", "osaamisen tunnustaminen")
  @Representative
  @MultiLineString(5)
  def selite: LocalizedString
}

trait MahdollisestiTunnustettu {
  def tunnustettu: Option[SelitettyOsaamisenTunnustaminen]

  def tunnustettuRahoituksenPiirissä: Boolean = tunnustettu.exists {
    case o: OsaamisenTunnustaminen => o.rahoituksenPiirissä
    case _ => false
  }
}
