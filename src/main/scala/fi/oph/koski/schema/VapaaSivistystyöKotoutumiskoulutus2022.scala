package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, ReadOnly}
import fi.oph.scalaschema.annotation.{DefaultValue, Description, OnlyWhen, Title}

import java.time.LocalDate

/*
  Päätason suoritus
*/
object VSTKoto2022Peruste {
  val diaarinumero = "OPH-649-2022"
}

@OnlyWhen("koulutusmoduuli/perusteenDiaarinumero", VSTKoto2022Peruste.diaarinumero)
@Title("Oppivelvollisille suunnattu maahanmuuttajien kotoutumiskoulutuksen suoritus 2022")
case class OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
  toimipiste: OrganisaatioWithOid,
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: VSTKotoutumiskoulutus2022 = VSTKotoutumiskoulutus2022(),
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  @Description("Koulutuksen opetuskieli")
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Title("Osaamiskokonaisuudet")
  override val osasuoritukset: Option[List[VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022]] = None,
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None
) extends VapaanSivistystyönPäätasonSuoritus
  with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta
  with OpintopistelaajuuksienYhteislaskennallinenPäätasonSuoritus[LaajuusOpintopisteissä]
  with OpintopistelaajuuksienYhteislaskennallinenSuoritus[LaajuusOpintopisteissä]

@Description("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen (OPS 2022) tunnistetiedot")
case class VSTKotoutumiskoulutus2022(
  @KoodistoKoodiarvo("999910")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999910", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String] = Some(VSTKoto2022Peruste.diaarinumero),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @ReadOnly("Laajuus lasketaan automaattisesti osasuoritusten laajuuksista.")
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends DiaarinumerollinenKoulutus
  with Tutkinto
  with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuliLaajuusOpintopisteissä

@Title("Vapaan sivistystyön kotoutumiskoulutuksen arviointi")
@Description("Hyväksytty arviointi tarkoittaa osasuorituksen suoritusta")
case class VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022(
  @KoodistoKoodiarvo("Hyväksytty")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", "arviointiasteikkovst"),
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi

/*
  Osasuoritukset
*/

@Description("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen osasuoritus")
trait VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 extends Suoritus
  with Vahvistukseton
  with OpintopistelaajuuksienYhteislaskennallinenSuoritus[LaajuusOpintopisteissä]
{
  override def koulutusmoduuli: VSTKotoutumiskoulutuksenSuorituksenKoulutusmoduuli2022
  override val arviointi: Option[List[VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022]]
  override val tyyppi: Koodistokoodiviite
}

trait VSTKotoutumiskoulutuksenSuorituksenKoulutusmoduuli2022
  extends KoodistostaLöytyväKoulutusmoduuli
    with LaajuuttaEiValidoida
    with KoulutusmoduuliValinnainenLaajuus
    with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuliLaajuusOpintopisteissä {
  @KoodistoUri("vstkoto2022kokonaisuus")
  def tunniste: Koodistokoodiviite
}

@Description("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen osasuorituksen alaosasuoritus")
trait VSTKotoutumiskoulutuksenAlaosasuoritus2022
  extends Suoritus
{
  override def koulutusmoduuli: VSTKotoutumiskoulutuksenAlasuorituksenKoulutusmoduuli2022
  override def vahvistus: Option[Vahvistus] = None
  override def kesken: Boolean = false
}

trait VSTKotoutumiskoulutuksenAlasuorituksenKoulutusmoduuli2022
  extends Koulutusmoduuli
    with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuliLaajuusOpintopisteissä

/*
  Kieli- ja viestintäosaaminen
*/

@Title("VST-KOTO kieli- ja viestintäosaaminen")
@Description("VST-KOTO kieli- ja viestintäosaamisen osasuoritus")
case class VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(
  @Title("Kotoutumiskoulutuksen kieli- ja viestintäosaaminen")
  koulutusmoduuli: VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli = VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli(),
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus", koodistoUri = "suorituksentyyppi"),
  override val osasuoritukset: Option[List[VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus]] = None,
  arviointi: Option[List[VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022]] = None,
) extends VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022

@Title("VST-KOTO kieli- ja viestintäosaamisen kokonaisuus")
case class VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli(
  @KoodistoKoodiarvo("kielijaviestintaosaaminen")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("kielijaviestintaosaaminen", "vstkoto2022kokonaisuus"),
  @ReadOnly("VST-Laajuus lasketaan automaattisesti osasuoritusten laajuuksista.")
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends VSTKotoutumiskoulutuksenSuorituksenKoulutusmoduuli2022

@Title("VST-KOTO kieli- ja viestintäosaamisen osasuorituksen alaosasuoritus")
case class VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus(
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenkielitaitojensuoritus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022,
  arviointi: Option[List[VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi]],
) extends VSTKotoutumiskoulutuksenAlaosasuoritus2022

@Title("VST-KOTO kieli- ja viestintäosaamisen osasuorituksen alaosasuoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022(
  @KoodistoUri("vstkoto2022kielijaviestintakoulutus")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends VSTKotoutumiskoulutuksenAlasuorituksenKoulutusmoduuli2022
  with KoodistostaLöytyväKoulutusmoduuli

@Title("VST-KOTO kieli- ja viestintäosaamisen arviointi")
case class VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi(
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
  arvosana: Koodistokoodiviite,
  arviointipäivä: Option[LocalDate] = None,
  arvioitsijat: Option[List[SuorituksenArvioitsija]] = None,
) extends Arviointi {
  override def hyväksytty: Boolean = true
  override def arvosanaKirjaimin: LocalizedString = LocalizedString.finnish(arvosana.koodiarvo)
}

/*
  Yhteiskunta- ja työosaaminen
*/

@Title("VST-KOTO yhteiskunta- ja työelämäosaaminen")
@Description("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen yhteiskunta- ja työelämäosaamisen osasuoritus")
case class VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(
  @Title("Kotoutumiskoulutuksen yhteyskunta- ja työelämäosaaminen")
  koulutusmoduuli: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 = VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022(),
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus]] = None,
  arviointi: Option[List[VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022]] = None,
) extends VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022

@Title("VST-KOTO yhteiskunta- ja työelämäosaamisen kokonaisuus")
case class VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022(
  @KoodistoKoodiarvo("yhteiskuntajatyoelamaosaaminen")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("yhteiskuntajatyoelamaosaaminen", "vstkoto2022kokonaisuus"),
  @ReadOnly("Laajuus lasketaan automaattisesti osasuoritusten laajuuksista.")
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends VSTKotoutumiskoulutuksenSuorituksenKoulutusmoduuli2022

@Title("VST-KOTO yhteiskunta- ja työelämäosaamisen osasuorituksen alaosasuoritus")
case class VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus(
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus", "suorituksentyyppi"),
  koulutusmoduuli: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022,
) extends VSTKotoutumiskoulutuksenAlaosasuoritus2022 with Vahvistukseton with Arvioinniton

@Title("VST-KOTO yhteuskunta- ja työelämäosaamisen osasuorituksen alaosasuoritus")
case class VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022(
  @KoodistoUri(koodistoUri = "vstkoto2022yhteiskuntajatyoosaamiskoulutus")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends VSTKotoutumiskoulutuksenAlasuorituksenKoulutusmoduuli2022
  with KoodistostaLöytyväKoulutusmoduuli

/*
  Ohjaus
*/

@Title("VST-KOTO ohjaus")
@Description("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen ohjauksen osasuoritus")
case class VSTKotoutumiskoulutuksenOhjauksenSuoritus2022(
  @Title("Kotoutumiskoulutuksen ohjaus")
  koulutusmoduuli: VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 = VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022(),
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus", "suorituksentyyppi"),
) extends VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 {
  override def osasuoritukset: Option[List[Suoritus]] = None
  override val arviointi: Option[List[VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022]] = None
}

@Title("VST-KOTO ohjauksen osasuorituksen kokonaisuus")
case class VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022(
  @KoodistoKoodiarvo("ohjaus")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("ohjaus", "vstkoto2022kokonaisuus"),
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends VSTKotoutumiskoulutuksenSuorituksenKoulutusmoduuli2022
  with OpintopistelaajuuksienYhteenlaskennanOhittavaKoulutusmoduuli[LaajuusOpintopisteissä]

/*
  Valinnaiset opinnot
*/

@Title("VST-KOTO valinnaiset opinnot")
@Description("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen valinnaisten opintojen osasuoritus")
case class VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(
  @Title("Kotoutumiskoulutuksen valinnaiset opinnot")
  koulutusmoduuli: VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 = VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022(),
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus", "suorituksentyyppi"),
  override val osasuoritukset: Option[List[VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus]] = None,
  arviointi: Option[List[VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022]] = None,
) extends VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022

@Title("VST-KOTO valinnaisten opintojen kokonaisuus")
case class VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022(
  @KoodistoKoodiarvo("valinnaisetopinnot")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("valinnaisetopinnot", "vstkoto2022kokonaisuus"),
  @ReadOnly("Laajuus lasketaan automaattisesti osasuoritusten laajuuksista.")
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends VSTKotoutumiskoulutuksenSuorituksenKoulutusmoduuli2022

@Title("VST-KOTO valinnaisten opintojen osasuoritus")
case class VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus(
  @Title("Vapaan sivistystyön maahanmuuttajien kotoutumiskoultuksen valinnaisten opintojen osasuoritus")
  koulutusmoduuli: VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022,
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus", "suorituksentyyppi"),
) extends VSTKotoutumiskoulutuksenAlaosasuoritus2022 with Vahvistukseton with Arvioinniton

case class VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022(
  kuvaus: LocalizedString,
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusOpintopisteissä] = None,
) extends VSTKotoutumiskoulutuksenAlasuorituksenKoulutusmoduuli2022
  with PaikallinenKoulutusmoduuli
  with StorablePreference
