package fi.oph.koski.valpas.repository

import fi.oph.koski.henkilo.Yhteystiedot
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.valpas.hakukooste.Hakukooste

case class ValpasYhteystiedot(
  alkuperä: ValpasYhteystietojenAlkuperä,
  yhteystietoryhmänNimi: LocalizedString,
  henkilönimi: Option[String] = None,
  sähköposti: Option[String] = None,
  puhelinnumero: Option[String] = None,
  matkapuhelinnumero: Option[String] = None,
  lähiosoite: Option[String] = None,
  kunta: Option[String] = None,
  postinumero: Option[String] = None,
  maa: Option[String] = None,
)

trait ValpasYhteystietojenAlkuperä

case class ValpasYhteystietoHakemukselta (
  hakuOid: String,
  hakemusOid: String
) extends ValpasYhteystietojenAlkuperä

case class ValpasYhteystietoOppijanumerorekisteristä (
  @KoodistoUri("yhteystietojenalkupera")
  alkuperä: Koodistokoodiviite,
  @KoodistoUri("yhteystietotyypit")
  tyyppi: Koodistokoodiviite,
) extends ValpasYhteystietojenAlkuperä

object ValpasYhteystiedot {
  def oppijanIlmoittamatYhteystiedot(hakukooste: Hakukooste, nimi: LocalizedString): ValpasYhteystiedot = ValpasYhteystiedot(
    alkuperä = ValpasYhteystietoHakemukselta(hakukooste.hakemusOid, hakukooste.hakemusOid),
    yhteystietoryhmänNimi = nimi,
    matkapuhelinnumero = Some(hakukooste.matkapuhelin),
    lähiosoite = Some(hakukooste.lahiosoite),
    postinumero = Some(hakukooste.postinumero),
    kunta = hakukooste.postitoimipaikka,
    sähköposti = Some(hakukooste.email),
  )

  def oppijanIlmoittamatHuoltajanYhteystiedot(hakukooste: Hakukooste, nimi: LocalizedString): Option[ValpasYhteystiedot] =
    if (hakukooste.huoltajanNimi.nonEmpty || hakukooste.huoltajanPuhelinnumero.nonEmpty || hakukooste.huoltajanSähkoposti.nonEmpty) {
      Some(ValpasYhteystiedot(
        alkuperä = ValpasYhteystietoHakemukselta(hakukooste.hakemusOid, hakukooste.hakemusOid),
        yhteystietoryhmänNimi = nimi,
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
    kunta = yhteystiedot.kunta.orElse(yhteystiedot.kaupunki),
    postinumero = yhteystiedot.postinumero,
    maa = yhteystiedot.maa,
  )
}
