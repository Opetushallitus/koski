package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.valpas.hakukooste.Ilmoittautumistila.Ilmoittautumistila
import fi.oph.koski.valpas.hakukooste.Valintatila.Valintatila
import fi.oph.koski.valpas.hakukooste.Vastaanottotieto.Vastaanottotieto

case class Hakukooste(
  oppijaOid: String,
  hakuOid: String,
  hakemusOid: String,
  @KoodistoUri("hakutapa") // Yhteishaku / Erillishaku / Jatkuva haku / Joustava haku
  hakutapa: Koodistokoodiviite,
  @KoodistoUri("hakutyyppi") // Varsinainen haku / täydennyshaku / lisähaku
  hakutyyppi: Koodistokoodiviite,
  muokattu: String,
  hakuNimi: LocalizedString,
  email: String,
  osoite: String,
  matkapuhelin: String,
  huoltajanNimi: String,
  huoltajanPuhelinnumero: String,
  huoltajanSahkoposti: String,
  hakutoiveet: Seq[Hakutoive]
)

case class Hakutoive(
  hakukohdeOid: String,
  hakukohdeNimi: LocalizedString,
  hakutoivenumero: Int,
  koulutusNimi: LocalizedString,
  hakukohdeOrganisaatio: String,
  pisteet: Float,
  koulutusOid: String,
  valintatila: String, // TODO: Toteutetaan enumeraationa jotenkin scala-scheman tukemalla tavalla
  vastaanottotieto: String, // TODO: Toteutetaan enumeraationa jotenkin scala-scheman tukemalla tavalla
  ilmoittautumistila: String, // TODO: Toteutetaan enumeraationa jotenkin scala-scheman tukemalla tavalla
  harkinnanvaraisuus: String, // TODO: Arvot?
  hakukohdeKoulutuskoodi: String // TODO: Arvot?
)

object Vastaanottotieto extends Enumeration {
  type Vastaanottotieto = Value
  val KESKEN, VASTAANOTTANUT_SITOVASTI, EI_VASTAANOTETTU_MAARA_AIKANA, PERUNUT,PERUUTETTU, OTTANUT_VASTAAN_TOISEN_PAIKAN, EHDOLLISESTI_VASTAANOTTANUT = Value
}

object Valintatila extends Enumeration {
  type Valintatila = Value
  val HYVAKSYTTY, HARKINNANVARAISESTI_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY, VARALLA,PERUUTETTU, PERUNUT, HYLATTY, PERUUNTUNUT, KESKEN = Value

  def isAktiivinen(tila: String): Boolean = tila match {
    case "HYVAKSYTTY" => true
    case "HARKINNANVARAISESTI_HYVAKSYTTY" => true
    case "VARASIJALTA_HYVAKSYTTY" => true
    case "VARALLA" => true
    case "PERUUTETTU" => false
    case "PERUNUT" => false
    case "HYLATTY" => false
    case "PERUUNTUNUT" => false
    case "KESKEN" => true
    case _ => false
  }
}

object Ilmoittautumistila extends Enumeration {
  type Ilmoittautumistila = Value
  val EI_TEHTY, LASNA_KOKO_LUKUVUOSI, POISSA_KOKO_LUKUVUOSI, EI_ILMOITTAUTUNUT, LASNA_SYKSY, POISSA_SYKSY, LASNA, POISSA = Value
}
