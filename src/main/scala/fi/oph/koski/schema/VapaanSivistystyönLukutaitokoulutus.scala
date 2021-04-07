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
 laajuus: Option[LaajuusTunneissa] = None
) extends DiaarinumerollinenKoulutus with Tutkinto

@Title("Lukutaitokoulutuksen kokonaisuuden suoritus")
case class VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus(
  @Title("Lukutaitokoulutuksen kokonaisuus")
  koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus,
  @KoodistoKoodiarvo("vstlukutaitokoulutuksenkokonaisuudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutuksenkokonaisuudensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[VapaanSivistystyönLukutaitokoulutuksenArviointi]] = None
) extends Suoritus with Vahvistukseton

trait VapaanSivistystyönLukutaitokoulutuksenOsasuoritustenKoulutusmoduuli extends KoulutusmoduuliValinnainenLaajuus with KoodistostaLöytyväKoulutusmoduuli {
  def laajuus: Option[LaajuusTunneissa]
}

@Title("Lukutaitokoulutuksen kokonaisuus")
case class VapaanSivistystyönLukutaidonKokonaisuus(
  @KoodistoUri(koodistoUri = "vstlukutaitokoulutuksenkokonaisuus")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusTunneissa] = None
) extends VapaanSivistystyönLukutaitokoulutuksenOsasuoritustenKoulutusmoduuli with KoodistostaLöytyväKoulutusmoduuli with LaajuuttaEiValidoida

@Title("Arviointi")
case class VapaanSivistystyönLukutaitokoulutuksenArviointi(
  @KoodistoUri("arviointiasteikkovstkoto")
  @KoodistoKoodiarvo("Suoritettu")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Suoritettu", "arviointiasteikkovstkoto"),
  @KoodistoUri("arviointiasteikkokehittyvankielitaidontasot")
  taitotaso: Koodistokoodiviite,
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi
