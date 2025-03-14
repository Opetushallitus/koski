package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.{Description, Title}
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, ReadOnly, Tooltip}

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
) extends VapaanSivistystyönKoulutuksenPäätasonSuoritus
    with OppivelvollisuudenSuorittamiseenKelpaava
    with OpintopistelaajuuksienYhteislaskennallinenSuoritus[LaajuusOpintopisteissä]

@Description("Vapaan sivistystyön lukutaitokoulutuksen tunnistetiedot")
case class VapaanSivistystyönLukutaitokoulutus(
 @KoodistoKoodiarvo("999911")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("999911", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String] = None,
 koulutustyyppi: Option[Koodistokoodiviite] = None,
 @ReadOnly("Täytetään tiedonsiirrossa automaattisesti osasuoritusten yhteislaajuudella")
 @Tooltip("Lasketaan automaattisesti tallentamisen jälkeen osasuoritusten laajuuksista")
 laajuus: Option[LaajuusOpintopisteissä] = None
) extends DiaarinumerollinenKoulutus
    with Tutkinto
    with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusOpintopisteissä] {
  override def makeLaajuus(laajuusArvo: Double): LaajuusOpintopisteissä = LaajuusOpintopisteissä(laajuusArvo)
}

@Title("Lukutaitokoulutuksen kokonaisuuden suoritus")
case class VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus(
  @Title("Lukutaitokoulutuksen kokonaisuus")
  koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus,
  @KoodistoKoodiarvo("vstlukutaitokoulutuksenkokonaisuudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutuksenkokonaisuudensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[LukutaitokoulutuksenArviointi]] = None
) extends Suoritus with Vahvistukseton

@Title("Arviointi")
case class LukutaitokoulutuksenArviointi(
  @KoodistoKoodiarvo("Hyväksytty")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", "arviointiasteikkovst"),
  päivä: LocalDate,
  @KoodistoUri("arviointiasteikkokehittyvankielitaidontasot")
  @KoodistoKoodiarvo("A1.1")
  @KoodistoKoodiarvo("A1.2")
  @KoodistoKoodiarvo("A1.3")
  @KoodistoKoodiarvo("A2.1")
  @KoodistoKoodiarvo("A2.2")
  @KoodistoKoodiarvo("B1.1")
  @KoodistoKoodiarvo("B1.2")
  @KoodistoKoodiarvo("B2.1")
  @KoodistoKoodiarvo("B2.2")
  @KoodistoKoodiarvo("C1.1")
  @KoodistoKoodiarvo("C1.2")
  @KoodistoKoodiarvo("C2.1")
  @KoodistoKoodiarvo("C2.2")
  taitotaso: Koodistokoodiviite
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi

trait VapaanSivistystyönLukutaitokoulutuksenOsasuoritustenKoulutusmoduuli extends KoulutusmoduuliValinnainenLaajuus with KoodistostaLöytyväKoulutusmoduuli {
  def laajuus: Option[LaajuusOpintopisteissä]
}

@Title("Lukutaitokoulutuksen kokonaisuus")
case class VapaanSivistystyönLukutaidonKokonaisuus(
  @KoodistoUri(koodistoUri = "vstlukutaitokoulutuksenkokonaisuus")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends VapaanSivistystyönLukutaitokoulutuksenOsasuoritustenKoulutusmoduuli with KoodistostaLöytyväKoulutusmoduuli with LaajuuttaEiValidoida
