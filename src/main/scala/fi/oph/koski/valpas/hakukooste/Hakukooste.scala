package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.schema.annotation.{EnumValues, KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.schema.{BlankableLocalizedString, Koodistokoodiviite}
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHakutilanneLaajatTiedot, ValpasHakutoive, ValpasHenkilö}
import fi.oph.scalaschema.annotation.SyntheticProperty

import java.time.LocalDateTime


case class Hakukooste(
  oppijaOid: ValpasHenkilö.Oid,
  hakuOid: ValpasHakutilanneLaajatTiedot.HakuOid,
  aktiivinenHaku: Option[Boolean],
  hakemusOid: ValpasHakutilanneLaajatTiedot.HakemusOid,
  hakemusUrl: String,
  @KoodistoUri("hakutapa")
  hakutapa: Koodistokoodiviite,
  @KoodistoUri("hakutyyppi")
  hakutyyppi: Koodistokoodiviite,
  haunAlkamispaivamaara: LocalDateTime,
  hakuNimi: BlankableLocalizedString,
  email: String,
  lahiosoite: String,
  postinumero: String,
  postitoimipaikka: Option[String],
  @KoodistoUri("maatjavaltiot1")
  @KoodistoUri("maatjavaltiot2")
  maa: Option[Koodistokoodiviite],
  matkapuhelin: String,
  huoltajanNimi: Option[String],
  huoltajanPuhelinnumero: Option[String],
  huoltajanSähkoposti: Option[String],
  hakutoiveet: Seq[Hakutoive],
  hakemuksenMuokkauksenAikaleima: Option[LocalDateTime]
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
  alinHyvaksyttyPistemaara: Option[BigDecimal],
  @EnumValues(Valintatila.values)
  valintatila: Option[String],
  varasijanumero: Option[Int],
  @EnumValues(Vastaanottotieto.values)
  vastaanottotieto: Option[String],
  @EnumValues(Ilmoittautumistila.values)
  ilmoittautumistila: Option[String],
  @EnumValues(Harkinnanvaraisuus.values)
  harkinnanvaraisuus: Option[String],
  @KoodistoUri("koulutus")
  hakukohdeKoulutuskoodi: Koodistokoodiviite
) {
  @SyntheticProperty
  def onHakenutHarkinnanvaraisesti = harkinnanvaraisuus.isDefined
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

  def valpasKoodiviiteOption(vastaanotto: Option[String]): Option[Koodistokoodiviite] =
    (vastaanotto match {
      case Some("VASTAANOTTANUT_SITOVASTI") => Some("vastaanotettu")
      case Some("EHDOLLISESTI_VASTAANOTTANUT") => Some("ehdollinen")
      case _ => None
    }).map(Koodistokoodiviite(_, "valpasvastaanottotieto"))
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

  def valpasKoodiviiteOption(valintatila: Option[String]): Option[Koodistokoodiviite] =
    (valintatila match {
      case Some("HYVAKSYTTY") | Some("HARKINNANVARAISESTI_HYVAKSYTTY") | Some("VARASIJALTA_HYVAKSYTTY") => Some("hyvaksytty")
      case Some("HYLATTY") => Some("hylatty")
      case Some("VARALLA") => Some("varasijalla")
      case Some("PERUUNTUNUT") => Some("peruuntunut")
      case Some("PERUUTETTU") => Some("peruutettu")
      case Some("PERUTTU") => Some("peruttu")
      case Some("KESKEN") => Some("kesken")
      case _ => None
    }).map(Koodistokoodiviite(_, "valpashaunvalintatila"))
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

object Harkinnanvaraisuus {
  val values = Set(
    "oppimisvaikudet", // Typo lähdekoodissa, https://github.com/Opetushallitus/haku/blob/master/hakemus-api/src/main/java/fi/vm/sade/haku/virkailija/lomakkeenhallinta/hakulomakepohja/phase/hakutoiveet/HakutoiveetPhase.java#L212-L221
    "sosiaalisetsyyt",
    "todistustenvertailuvaikeudet",
    "todistustenpuuttuminen",
    "riittamatonkielitaito"
  )

  def isHarkinnanvarainen(value: String) = values.contains(value)
}
