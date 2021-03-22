package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.schema.annotation.{EnumValues, KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.schema.{BlankableLocalizedString, Koodistokoodiviite}
import fi.oph.koski.valpas.repository.{ValpasHakutilanne, ValpasHakutoive, ValpasHenkilö, ValpasOppilaitos}
import fi.oph.scalaschema.annotation.SyntheticProperty

import java.time.LocalDateTime


case class Hakukooste(
  oppijaOid: ValpasHenkilö.Oid,
  hakuOid: ValpasHakutilanne.HakuOid,
  hakemusOid: ValpasHakutilanne.HakemusOid,

  @KoodistoUri("hakutapa")
  // TODO: Koodiston lataus koskeen
  @KoodistoKoodiarvo("01") // Yhteishaku
  @KoodistoKoodiarvo("02") // Erillishaku
  @KoodistoKoodiarvo("03") // Jatkuva haku
  @KoodistoKoodiarvo("04") // Joustava haku
  hakutapa: Koodistokoodiviite,

  @KoodistoUri("hakutyyppi")
  // TODO: Koodiston lataus koskeen
  @KoodistoKoodiarvo("01") // Varsinainen haku
  @KoodistoKoodiarvo("02") // täydennyshaku
  @KoodistoKoodiarvo("03") // lisähaku
  hakutyyppi: Koodistokoodiviite,

  haunAlkamispaivamaara: LocalDateTime,
  hakuNimi: BlankableLocalizedString,
  email: String,
  lahiosoite: String,
  matkapuhelin: String,
  huoltajanNimi: Option[String],
  huoltajanPuhelinnumero: Option[String],
  huoltajanSähkoposti: Option[String],
  hakutoiveet: Seq[Hakutoive]
)

case class Hakutoive(
  hakukohdeOid: ValpasOppilaitos.Oid,
  hakukohdeNimi: BlankableLocalizedString,
  hakukohdeOrganisaatio: String,
  koulutusNimi: BlankableLocalizedString,
  koulutusOid: Option[ValpasHakutoive.KoulutusOid],
  hakutoivenumero: Int,
  pisteet: Option[BigDecimal],
  alinValintaPistemaara: Option[BigDecimal],
  @EnumValues(Valintatila.values)
  valintatila: Option[String],
  @EnumValues(Vastaanottotieto.values)
  vastaanottotieto: Option[String],
  @EnumValues(Ilmoittautumistila.values)
  ilmoittautumistila: Option[String],
  harkinnanvaraisuus: Option[String], // TODO: Arvot?
  hakukohdeKoulutuskoodi: String // TODO: Arvot?
) {
  @SyntheticProperty
  def isAktiivinen: Boolean = valintatila.exists(v => Valintatila.isAktiivinen(v))
}

object Vastaanottotieto {
  val values = Set(
    "KESKEN",
    "VASTAANOTTANUT_SITOVASTI",
    "EI_VASTAANOTETTU_MAARA_AIKANA",
    "PERUNUT", "PERUUTETTU",
    "OTTANUT_VASTAAN_TOISEN_PAIKAN",
    "EHDOLLISESTI_VASTAANOTTANUT"
  )
}

object Valintatila {
  private val aktiivisetTilat = Set(
    "HYVAKSYTTY",
    "HARKINNANVARAISESTI_HYVAKSYTTY",
    "VARASIJALTA_HYVAKSYTTY",
    "VARALLA",
    "KESKEN"
  )

  private val eiAktiivisetTilat = Set(
    "PERUUTETTU",
    "PERUNUT",
    "HYLATTY",
    "PERUUNTUNUT"
  )

  val values: Set[String] = aktiivisetTilat ++ eiAktiivisetTilat

  def isAktiivinen(tila: String): Boolean = aktiivisetTilat.contains(tila)
}

object Ilmoittautumistila {
  val values = Set(
    "EI_TEHTY",
    "LASNA_KOKO_LUKUVUOSI",
    "POISSA_KOKO_LUKUVUOSI",
    "EI_ILMOITTAUTUNUT",
    "LASNA_SYKSY",
    "POISSA_SYKSY",
    "LASNA",
    "POISSA"
  )
}
