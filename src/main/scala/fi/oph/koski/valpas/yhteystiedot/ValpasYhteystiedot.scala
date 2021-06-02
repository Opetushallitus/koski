package fi.oph.koski.valpas.yhteystiedot

import java.time.LocalDateTime

import fi.oph.koski.henkilo.Yhteystiedot
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.schema.{BlankableLocalizedString, Koodistokoodiviite, LocalizedString}
import fi.oph.koski.valpas.hakukooste.Hakukooste
import fi.oph.scalaschema.annotation.SyntheticProperty

case class ValpasYhteystiedot(
  alkuperä: ValpasYhteystietojenAlkuperä,
  yhteystietoryhmänNimi: LocalizedString,
  henkilönimi: Option[String] = None,
  sähköposti: Option[String] = None,
  puhelinnumero: Option[String] = None,
  matkapuhelinnumero: Option[String] = None,
  lähiosoite: Option[String] = None,
  postitoimipaikka: Option[String] = None,
  postinumero: Option[String] = None,
  // Käytetään tässä yleisenä tyyppinä LocalizedString:iä, koska maatieto tulee DVV:ltä merkkijonona ja
  // hakukoostepalvelusta koodistokoodiarvona, jossa koodistona joko maatjavaltiot1 tai maatjavaltiot2
  maa: Option[LocalizedString] = None
)

trait ValpasYhteystietojenAlkuperä

case class ValpasYhteystietoHakemukselta (
  hakuNimi: BlankableLocalizedString,
  haunAlkamispaivämäärä: LocalDateTime,
  hakemuksenMuokkauksenAikaleima: Option[LocalDateTime],
  hakuOid: String,
  hakemusOid: String
) extends ValpasYhteystietojenAlkuperä

object ValpasYhteystietoHakemukselta {
  def apply(hakukooste: Hakukooste): ValpasYhteystietoHakemukselta = ValpasYhteystietoHakemukselta(
    hakuNimi = hakukooste.hakuNimi,
    haunAlkamispaivämäärä = hakukooste.haunAlkamispaivamaara,
    hakemuksenMuokkauksenAikaleima = hakukooste.hakemuksenMuokkauksenAikaleima,
    hakuOid = hakukooste.hakuOid,
    hakemusOid = hakukooste.hakemusOid
  )
}

case class ValpasYhteystietoOppijanumerorekisteristä (
  @KoodistoUri("yhteystietojenalkupera")
  alkuperä: Koodistokoodiviite,
  @KoodistoUri("yhteystietotyypit")
  tyyppi: Koodistokoodiviite,
) extends ValpasYhteystietojenAlkuperä

object ValpasYhteystiedot {
  def oppijanIlmoittamatYhteystiedot(hakukooste: Hakukooste, yhteystietoryhmänNimi: LocalizedString): ValpasYhteystiedot = ValpasYhteystiedot(
    alkuperä = ValpasYhteystietoHakemukselta(hakukooste),
    yhteystietoryhmänNimi = yhteystietoryhmänNimi,
    matkapuhelinnumero = Some(hakukooste.matkapuhelin),
    lähiosoite = Some(hakukooste.lahiosoite),
    postinumero = Some(hakukooste.postinumero),
    postitoimipaikka = hakukooste.postitoimipaikka,
    maa = hakukooste.maa match {
      case Some(maa) if !onSuomi(maa) => maa.nimi // Suomi on oletus, ei haluta näyttää
      case _ => None
    },
    sähköposti = Some(hakukooste.email),
  )

  def oppijanIlmoittamatHuoltajanYhteystiedot(hakukooste: Hakukooste, yhteystietoryhmänNimi: LocalizedString): Option[ValpasYhteystiedot] =
    if (hakukooste.huoltajanNimi.nonEmpty || hakukooste.huoltajanPuhelinnumero.nonEmpty || hakukooste.huoltajanSähkoposti.nonEmpty) {
      Some(ValpasYhteystiedot(
        alkuperä = ValpasYhteystietoHakemukselta(hakukooste),
        yhteystietoryhmänNimi = yhteystietoryhmänNimi,
        henkilönimi = hakukooste.huoltajanNimi,
        matkapuhelinnumero = hakukooste.huoltajanPuhelinnumero,
        sähköposti = hakukooste.huoltajanSähkoposti,
      ))
    } else {
      None
    }

  def virallinenYhteystieto(yhteystiedot: Yhteystiedot, nimi: LocalizedString): ValpasYhteystiedot = ValpasYhteystiedot(
    alkuperä = ValpasYhteystietoOppijanumerorekisteristä(yhteystiedot.alkuperä, yhteystiedot.tyyppi),
    yhteystietoryhmänNimi = nimi,
    sähköposti = yhteystiedot.sähköposti,
    puhelinnumero = yhteystiedot.puhelinnumero,
    matkapuhelinnumero = yhteystiedot.matkapuhelinnumero,
    lähiosoite = yhteystiedot.katuosoite,
    postitoimipaikka = yhteystiedot.kunta.orElse(yhteystiedot.kaupunki),
    postinumero = yhteystiedot.postinumero,
    maa = yhteystiedot.maa.map(LocalizedString.finnish), // Oletetaan DVV:ltä tulevan maan nimen olevan aina suomeksi
  )

  private def onSuomi(koodi: Koodistokoodiviite) =
    Seq(
      Koodistokoodiviite("FIN", "maatjavaltiot1"),
      Koodistokoodiviite("246", "maatjavaltiot2")
    ).contains(koodi)
}
