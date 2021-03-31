package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, OnlyWhen, Title}

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, MultiLineString, OksaUri, Representative, Tooltip}

case class VapaanSivistystyönLukutaitokoulutuksenSuoritus(
  toimipiste: OrganisaatioWithOid,
  @KoodistoKoodiarvo("vstlukutaitokoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: VapaanSivistystyönLukutaitokoulutus,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla],
  @Description("Koulutuksen opetuskieli")
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Title("Suorituskokonaisuudet")
  override val osasuoritukset: Option[List[VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None
) extends VapaanSivistystyönPäätasonSuoritus with Laajuudellinen

@Description("Vapaan sivistystyön lukutaitokoulutuksen tunnistetiedot")
case class VapaanSivistystyönLukutaitokoulutus(
 @KoodistoKoodiarvo("999911")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("999911", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String] = None,
 koulutustyyppi: Option[Koodistokoodiviite] = None,
 laajuus: Option[LaajuusOpintoviikoissa] = None
) extends DiaarinumerollinenKoulutus with Tutkinto

trait VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus extends Suoritus with Vahvistukseton

@Title("Lukutaitokoulutuksen vuorovaikutustilanteissa toimimisen kokonaisuuden suoritus")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo","vstlukutaitokoulutuksenvuorovaikutustilannekokonaisuudensuoritus")
case class VapaanSivistystyönLukutaitokoulutuksenVuorovaikutustilanteissaToimimisenSuoritus(
  @Title("Lukutaitokoulutuksen vuorovaikutustilanteissa toimimisen osuus")
  koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus,
  @KoodistoKoodiarvo("vstlukutaitokoulutuksenvuorovaikutustilannekokonaisuudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutuksenvuorovaikutustilannekokonaisuudensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[VapaanSivistystyönLukutaitokoulutuksenArviointi]] = None
) extends VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus

@Title("Lukutaitokoulutuksen tekstien lukemisen ja tulkitsemisen kokonaisuuden suoritus")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo","vstlukutaitokoulutuksentekstienlukemisenkokonaisuudensuoritus")
case class VapaanSivistystyönLukutaitokoulutuksenTekstienLukeminenJaTulkitseminenSuoritus(
  @Title("Lukutaitokoulutuksen tekstien lukemisen ja tulkitsemisen osuus")
  koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus,
  @KoodistoKoodiarvo("vstlukutaitokoulutuksentekstienlukemisenkokonaisuudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutuksentekstienlukemisenkokonaisuudensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[VapaanSivistystyönLukutaitokoulutuksenArviointi]] = None
) extends VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus

@Title("Lukutaitokoulutuksen tekstien kirjoittamisen ja tuottamisen kokonaisuuden suoritus")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo","vstlukutaitokoulutuksentekstienkirjoittamisenkokonaisuudensuoritus")
case class VapaanSivistystyönLukutaitokoulutuksenTekstienKirjoittaminenJaTuottaminenToimimisenSuoritus(
  @Title("Lukutaitokoulutuksen tekstien kirjoittamisen ja tuottamisen osuus")
  koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus,
  @KoodistoKoodiarvo("vstlukutaitokoulutuksentekstienkirjoittamisenkokonaisuudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutuksentekstienkirjoittamisenkokonaisuudensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[VapaanSivistystyönLukutaitokoulutuksenArviointi]] = None
) extends VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus

@Title("Lukutaitokoulutuksen numeeristen taitojen kokonaisuuden suoritus")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo","vstlukutaitokoulutuksennumeeristentaitojenkokonaisuudensuoritus")
case class VapaanSivistystyönLukutaitokoulutuksenNumeeristenTaitojenSuoritus(
  @Title("Lukutaitokoulutuksen numeeristen taitojen osuus")
  koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus,
  @KoodistoKoodiarvo("vstlukutaitokoulutuksennumeeristentaitojenkokonaisuudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutuksennumeeristentaitojenkokonaisuudensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[VapaanSivistystyönLukutaitokoulutuksenArviointi]] = None
) extends VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus

trait VapaanSivistystyönLukutaitokoulutuksenOsasuoritustenKoulutusmoduuli extends KoulutusmoduuliValinnainenLaajuus with KoodistostaLöytyväKoulutusmoduuli {
  def laajuus: Option[LaajuusOpintoviikoissa]
}

@Title("Lukutaitokoulutuksen kokonaisuus")
case class VapaanSivistystyönLukutaidonKokonaisuus(
  @KoodistoUri(koodistoUri = "vstlukutaitokoulutuksenkokonaisuus")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintoviikoissa] = None
) extends VapaanSivistystyönLukutaitokoulutuksenOsasuoritustenKoulutusmoduuli with KoodistostaLöytyväKoulutusmoduuli with LaajuuttaEiValidoida

@Title("Arviointi")
case class VapaanSivistystyönLukutaitokoulutuksenArviointi(
  @KoodistoUri("arviointiasteikkovstkoto")
  @KoodistoKoodiarvo("Suoritettu")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Suoritettu", "arviointiasteikkovstkoto"),
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi
