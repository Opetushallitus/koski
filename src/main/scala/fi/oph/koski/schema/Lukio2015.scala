package fi.oph.koski.schema

import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{DefaultValue, Description, MinItems, OnlyWhen, Title}

trait LukionPäätasonSuoritus2015 extends LukionPäätasonSuoritus with Todistus with Ryhmällinen

@Description("Lukion oppimäärän suoritustiedot")
case class LukionOppimääränSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: LukionOppimäärä,
  @KoodistoUri("lukionoppimaara")
  @Description("Tieto siitä, suoritetaanko lukiota nuorten vai aikuisten oppimäärän mukaisesti")
  @Title("Opetussuunnitelma")
  oppimäärä: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  @Description("Oppimäärän suorituksen opetuskieli/suorituskieli. Rahoituksen laskennassa käytettävä tieto.")
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Osallistuminen lukiokoulutusta täydentävän saamen/romanikielen/opiskelijan oman äidinkielen opiskeluun")
  omanÄidinkielenOpinnot: Option[OmanÄidinkielenOpinnotLaajuusKursseina] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[LukionOppimääränOsasuoritus]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends LukionPäätasonSuoritus2015 with Arvioinniton with KoulusivistyskieliKieliaineesta with Oppimäärällinen

@Description("Lukion oppiaineen oppimäärän suoritustiedot")
case class LukionOppiaineenOppimääränSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: LukionOppiaineTaiEiTiedossaOppiaine,
  toimipiste: OrganisaatioWithOid,
  @Description("Lukion oppiaineen oppimäärän arviointi")
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppiaineenoppimaara", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends LukionPäätasonSuoritus2015 with OppiaineenOppimääränSuoritus

trait LukionOppimääränOsasuoritus extends LukionOppimääränPäätasonOsasuoritus

@Title("Muiden lukio-opintojen suoritus")
@Description("Kategoria kursseille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esimerkiksi lukiodiplomi, taiteiden väliset opinnot, teemaopinnot")
case class MuidenLukioOpintojenSuoritus(
  @KoodistoKoodiarvo("lukionmuuopinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionmuuopinto", "suorituksentyyppi"),
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  koulutusmoduuli: MuuLukioOpinto,
  @MinItems(1)
  @Description("Kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]]
) extends LukionOppimääränOsasuoritus with PreIBSuorituksenOsasuoritus with Vahvistukseton

@Title("Muu lukio-opinto")
@Description("Kategoria kursseille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esimerkiksi lukiodiplomi, taiteiden väliset opinnot, teemaopinnot")
case class MuuLukioOpinto(
  @KoodistoUri("lukionmuutopinnot")
  tunniste: Koodistokoodiviite,
  @DefaultValue(None)
  laajuus: Option[LaajuusKursseissa] = None
) extends KoodistostaLöytyväKoulutusmoduuliValinnainenLaajuus

@Description("Lukion oppiaineen suoritustiedot")
case class LukionOppiaineenSuoritus(
  koulutusmoduuli: LukionOppiaine2015,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]],
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiaine", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with Vahvistukseton with LukionOppimääränOsasuoritus with MahdollisestiSuorituskielellinen

@Description("Lukion kurssin suoritustiedot")
case class LukionKurssinSuoritus(
  @Description("Lukion kurssin tunnistetiedot")
  koulutusmoduuli: LukionKurssi,
  @FlattenInUI
  arviointi: Option[List[LukionArviointi]] = None,
  @Description("Jos kurssi on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot. Osaamisen tunnustamisella voidaan opiskelijalle lukea hyväksi ja korvata lukion oppimäärään kuuluvia pakollisia, syventäviä tai soveltavia opintoja. Opiskelijan osaamisen tunnustamisessa noudatetaan, mitä 17 ja 17 a §:ssä säädetään opiskelijan arvioinnista ja siitä päättämisestä. Mikäli opinnot tai muutoin hankittu osaaminen luetaan hyväksi opetussuunnitelman perusteiden mukaan numerolla arvioitavaan kurssiin, tulee kurssista antaa numeroarvosana")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionkurssi", koodistoUri = "suorituksentyyppi"),
  suoritettuLukiodiplomina: Option[Boolean] = None,
  suoritettuSuullisenaKielikokeena: Option[Boolean] = None
) extends KurssinSuoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu

sealed trait LukionKurssi extends KoulutusmoduuliValinnainenLaajuus with PreIBKurssi {
  def laajuus: Option[LaajuusKursseissa]
  @KoodistoUri("lukionkurssintyyppi")
  @Description("Kurssin tyyppi voi olla joko syventävä, soveltava tai pakollinen")
  def kurssinTyyppi: Koodistokoodiviite
}

@Description("Valtakunnallisen lukion/IB-lukion kurssin tunnistetiedot")
case class ValtakunnallinenLukionKurssi(
  @Description("Lukion/IB-lukion kurssi")
  @KoodistoUri("lukionkurssit")
  @KoodistoUri("lukionkurssitops2004aikuiset")
  @KoodistoUri("lukionkurssitops2003nuoret")
  @OksaUri("tmpOKSAID873", "kurssi")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  override val laajuus: Option[LaajuusKursseissa],
  kurssinTyyppi: Koodistokoodiviite
) extends LukionKurssi with KoodistostaLöytyväKoulutusmoduuli

@Description("Paikallisen lukion/IB-lukion kurssin tunnistetiedot")
case class PaikallinenLukionKurssi(
  @FlattenInUI
  tunniste: PaikallinenKoodi,
  override val laajuus: Option[LaajuusKursseissa],
  kuvaus: LocalizedString,
  kurssinTyyppi: Koodistokoodiviite
) extends LukionKurssi with PaikallinenKoulutusmoduuli with StorablePreference

trait LukionOppiaineTaiEiTiedossaOppiaine extends Koulutusmoduuli

trait LukionOppiaine2015 extends LukionOppiaine with LukionOppiaineTaiEiTiedossaOppiaine with KoulutusmoduuliValinnainenLaajuus with PreIBOppiaine {
  def laajuus: Option[LaajuusKursseissa]
}

@Title("Paikallinen oppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaineenoppimaara")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "preiboppiaine")
case class PaikallinenLukionOppiaine2015(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  pakollinen: Boolean = true,
  laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionOppiaine2015 with PaikallinenKoulutusmoduuli with StorablePreference

trait LukionValtakunnallinenOppiaine2015 extends LukionOppiaine2015 with YleissivistavaOppiaine

@Title("Muu valtakunnallinen oppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaineenoppimaara")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "preiboppiaine")
case class LukionMuuValtakunnallinenOppiaine(
  @KoodistoKoodiarvo("HI")
  @KoodistoKoodiarvo("MU")
  @KoodistoKoodiarvo("BI")
  @KoodistoKoodiarvo("PS")
  @KoodistoKoodiarvo("ET")
  @KoodistoKoodiarvo("KO")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("KE")
  @KoodistoKoodiarvo("YH")
  @KoodistoKoodiarvo("TE")
  @KoodistoKoodiarvo("KS")
  @KoodistoKoodiarvo("FY")
  @KoodistoKoodiarvo("GE")
  @KoodistoKoodiarvo("LI")
  @KoodistoKoodiarvo("KU")
  @KoodistoKoodiarvo("OP")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @DefaultValue(None)
  laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine2015

@Title("Uskonto")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaineenoppimaara")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "preiboppiaine")
case class LukionUskonto(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  @DefaultValue(None)
  laajuus: Option[LaajuusKursseissa] = None,
  uskonnonOppimäärä: Option[Koodistokoodiviite] = None
) extends LukionValtakunnallinenOppiaine2015 with Uskonto

@Title("Äidinkieli ja kirjallisuus")
@Description("Oppiaineena äidinkieli ja kirjallisuus")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaineenoppimaara")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "preiboppiaine")
case class LukionÄidinkieliJaKirjallisuus2015(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @DefaultValue(None)
  laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine2015 with LukionÄidinkieliJaKirjallisuus {
  override def description: LocalizedString = kieliaineDescription
}

@Description("Oppiaineena vieras tai toinen kotimainen kieli")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaineenoppimaara")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "preiboppiaine")
case class VierasTaiToinenKotimainenKieli(
  @KoodistoKoodiarvo("A1")
  @KoodistoKoodiarvo("A2")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B2")
  @KoodistoKoodiarvo("B3")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @DefaultValue(None)
  laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine2015 with Kieliaine {
  override def description = kieliaineDescription
}

@Title("Matematiikka")
@Description("Oppiaineena matematiikka")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaineenoppimaara")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "preiboppiaine")
case class LukionMatematiikka(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Onko kyseessä laaja vai lyhyt oppimäärä")
  @KoodistoUri("oppiainematematiikka")
  oppimäärä: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @DefaultValue(None)
  laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine2015 with KoodistostaLöytyväKoulutusmoduuli with Oppimäärä {
  override def description = oppimäärä.description
}
