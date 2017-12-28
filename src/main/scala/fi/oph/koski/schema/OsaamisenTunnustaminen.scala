package fi.oph.koski.schema

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{DefaultValue, Description}

@Description("Tiedot aiemmin hankitun osaamisen tunnustamisesta.")
@Tooltip("Tiedot aiemmin hankitun osaamisen tunnustamisesta.")
@OksaUri("tmpOKSAID629", "osaamisen tunnustaminen")
case class OsaamisenTunnustaminen(
  @Description("Aiemman, korvaavan suorituksen tiedot")
  @FlattenInUI
  osaaminen: Option[Suoritus],
  @Description("Osaamisen tunnustamisen kautta saatavan tutkinnon osan suorituksen selite.")
  @Tooltip("Kuvaus siitä, miten aikaisemmin hankittu osaaminen on tunnustettu.")
  @OksaUri("tmpOKSAID629", "osaamisen tunnustaminen")
  @Representative
  @MultiLineString(5)
  selite: LocalizedString,
  @Description("Mikäli oppija on kaksoistutkintoa suorittava, jonka rahoituksen saaja on ammatillinen oppilaitos, tulee lukio-opinnoista tunnustetut suoritukset merkitä kentällä rahoituksenPiirissä: Kyllä.")
  @Tooltip("Valitse valintaruutu, mikäli oppija on kaksoistutkintoa suorittava, ja rahoituksen saaja on ammatillinen oppilaitos, joka osaamisen tunnustaa.")
  @DefaultValue(false)
  rahoituksenPiirissä: Boolean = false
)
