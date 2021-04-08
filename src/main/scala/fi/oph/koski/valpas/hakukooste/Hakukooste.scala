package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.schema.annotation.{EnumValues, KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.schema.{BlankableLocalizedString, Koodistokoodiviite}
import fi.oph.koski.valpas.repository.{ValpasHakutilanneLaajatTiedot, ValpasHakutoive, ValpasHenkilö}
import java.time.LocalDateTime


case class Hakukooste(
  oppijaOid: ValpasHenkilö.Oid,
  hakuOid: ValpasHakutilanneLaajatTiedot.HakuOid,
  aktiivinenHaku: Option[Boolean],
  hakemusOid: ValpasHakutilanneLaajatTiedot.HakemusOid,
  hakemusUrl: String,

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
  postinumero: String,
  postitoimipaikka: Option[String],
  matkapuhelin: String,
  huoltajanNimi: Option[String],
  huoltajanPuhelinnumero: Option[String],
  huoltajanSähkoposti: Option[String],
  hakutoiveet: Seq[Hakutoive]
)

case class Hakutoive(
  hakukohdeOid: String,
  hakukohdeNimi: BlankableLocalizedString,
  organisaatioNimi: BlankableLocalizedString,
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
  val values: Set[String] = Set(
    "HYVAKSYTTY",
    "HARKINNANVARAISESTI_HYVAKSYTTY",
    "VARASIJALTA_HYVAKSYTTY",
    "VARALLA",
    "KESKEN",
    "PERUUTETTU",
    "PERUNUT",
    "HYLATTY",
    "PERUUNTUNUT"
  )
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
