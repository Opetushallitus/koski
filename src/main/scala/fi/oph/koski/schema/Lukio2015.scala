package fi.oph.koski.schema

import fi.oph.koski.documentation.ExamplesLukio.{aikuistenOpsinPerusteet2004, aikuistenOpsinPerusteet2015}
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{DefaultValue, Description, MinItems, OnlyWhen, Title}

trait LukionPäätasonSuoritus2015 extends LukionPäätasonSuoritus with Todistus with Ryhmällinen {
  def onAikuistenOps(diaari: String) = {
    List(aikuistenOpsinPerusteet2015, aikuistenOpsinPerusteet2004).contains(diaari)
  }

  def oppimääränKoodiarvo: Option[String]
}

@Description("Lukion oppimäärän suoritustiedot")
@Title("Lukion oppimäärän suoritus")
case class LukionOppimääränSuoritus2015(
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
  override val osasuoritukset: Option[List[LukionOppimääränOsasuoritus2015]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends LukionPäätasonSuoritus2015
  with Arvioinniton
  with KoulusivistyskieliKieliaineesta
  with Oppimäärällinen
  with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta
  with LukionOppimääränSuoritus {
  def oppimääränKoodiarvo = Some(oppimäärä.koodiarvo)
}

@Description("Lukion oppiaineen oppimäärän suoritustiedot")
@Title("Lukion oppiaineen oppimäärän suoritus")
case class LukionOppiaineenOppimääränSuoritus2015(
  @Title("Oppiaine")
  koulutusmoduuli: LukionOppiaineTaiEiTiedossaOppiaine2015,
  toimipiste: OrganisaatioWithOid,
  @Description("Lukion oppiaineen oppimäärän arviointi")
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Merkitään, jos lukion oppimäärä on tullut suoritetuksi aineopintoina.")
  @Tooltip("Opiskelija on suorittanut koko lukion oppimäärän ja saanut lukion päättötodistuksen")
  @DefaultValue(false)
  @Deprecated("Käytä opiskeluoikeuden kenttää 'oppimääräSuoritettu'")
  @Hidden
  lukionOppimääräSuoritettu: Option[Boolean] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus2015]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppiaineenoppimaara", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends LukionPäätasonSuoritus2015 with OppiaineenOppimääränSuoritus  {
  def oppimääränKoodiarvo: Option[String] = {
    println("Tänne päädyttiin")
    koulutusmoduuli match {
    case diaari: Diaarinumerollinen =>
      println(diaari)
      println(onAikuistenOps(diaari.perusteenDiaarinumero.getOrElse("")))
      diaari.perusteenDiaarinumero.map(peruste => if (onAikuistenOps(peruste)) "aikuistenops" else "nuortenops")
    case _ => None
  }}
}

trait LukionOppimääränOsasuoritus2015 extends LukionOppimääränPäätasonOsasuoritus

@Title("Muiden lukio-opintojen suoritus")
@Description("Kategoria kursseille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esimerkiksi lukiodiplomi, taiteiden väliset opinnot, teemaopinnot")
case class MuidenLukioOpintojenSuoritus2015(
  @KoodistoKoodiarvo("lukionmuuopinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionmuuopinto", "suorituksentyyppi"),
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  koulutusmoduuli: MuuLukioOpinto2015,
  @MinItems(1)
  @Description("Kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus2015]]
) extends LukionOppimääränOsasuoritus2015 with PreIBSuorituksenOsasuoritus2015 with Vahvistukseton

@Title("Muu lukio-opinto")
@Description("Kategoria kursseille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esimerkiksi lukiodiplomi, taiteiden väliset opinnot, teemaopinnot")
case class MuuLukioOpinto2015(
  @KoodistoUri("lukionmuutopinnot")
  tunniste: Koodistokoodiviite,
  @DefaultValue(None)
  laajuus: Option[LaajuusKursseissa] = None
) extends KoodistostaLöytyväKoulutusmoduuliValinnainenLaajuus

@Title("Lukion oppiaineen suoritus")
@Description("Lukion oppiaineen suoritustiedot")
case class LukionOppiaineenSuoritus2015(
  koulutusmoduuli: LukionOppiaine2015,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus2015]],
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiaine", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with Vahvistukseton with LukionOppimääränOsasuoritus2015 with MahdollisestiSuorituskielellinen

@Title("Lukion kurssin suoritus")
@Description("Lukion kurssin suoritustiedot")
case class LukionKurssinSuoritus2015(
  @Description("Lukion kurssin tunnistetiedot")
  koulutusmoduuli: LukionKurssi2015,
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

@Title("Lukion kurssi")
sealed trait LukionKurssi2015 extends KoulutusmoduuliValinnainenLaajuus with PreIBKurssi2015 {
  def laajuus: Option[LaajuusKursseissa]
  @KoodistoUri("lukionkurssintyyppi")
  @Description("Kurssin tyyppi voi olla joko syventävä, soveltava tai pakollinen")
  def kurssinTyyppi: Koodistokoodiviite
}

@Title("Valtakunnallinen lukion kurssi")
@Description("Valtakunnallisen lukion/IB-lukion kurssin tunnistetiedot")
case class ValtakunnallinenLukionKurssi2015(
  @Description("Lukion/IB-lukion kurssi")
  @KoodistoUri("lukionkurssit")
  @KoodistoUri("lukionkurssitops2004aikuiset")
  @KoodistoUri("lukionkurssitops2003nuoret")
  @OksaUri("tmpOKSAID873", "kurssi")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  override val laajuus: Option[LaajuusKursseissa],
  kurssinTyyppi: Koodistokoodiviite
) extends LukionKurssi2015 with KoodistostaLöytyväKoulutusmoduuli

@Description("Paikallisen lukion/IB-lukion kurssin tunnistetiedot")
case class PaikallinenLukionKurssi2015(
  @FlattenInUI
  tunniste: PaikallinenKoodi,
  override val laajuus: Option[LaajuusKursseissa],
  kuvaus: LocalizedString,
  kurssinTyyppi: Koodistokoodiviite
) extends LukionKurssi2015 with PaikallinenKoulutusmoduuli with StorablePreference

trait LukionOppiaineTaiEiTiedossaOppiaine2015 extends Koulutusmoduuli

trait LukionOppiaine2015 extends LukionOppiaine with LukionOppiaineTaiEiTiedossaOppiaine2015 with KoulutusmoduuliValinnainenLaajuus with PreIBOppiaine2015 {
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
) extends LukionOppiaine2015 with PaikallinenKoulutusmoduuli with StorablePreference with Diaarinumerollinen

trait LukionValtakunnallinenOppiaine2015 extends LukionOppiaine2015 with YleissivistavaOppiaine

@Title("Muu valtakunnallinen oppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaineenoppimaara")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "preiboppiaine")
case class LukionMuuValtakunnallinenOppiaine2015(
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
) extends LukionValtakunnallinenOppiaine2015 with Diaarinumerollinen

@Title("Uskonto")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaineenoppimaara")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "preiboppiaine")
case class LukionUskonto2015(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  @DefaultValue(None)
  laajuus: Option[LaajuusKursseissa] = None,
  uskonnonOppimäärä: Option[Koodistokoodiviite] = None
) extends LukionValtakunnallinenOppiaine2015 with Uskonto with Diaarinumerollinen

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
) extends LukionValtakunnallinenOppiaine2015 with LukionÄidinkieliJaKirjallisuus with Diaarinumerollinen {
  override def description: LocalizedString = kieliaineDescription
}

@Title("Vieras tai toinen kotimainen kieli")
@Description("Oppiaineena vieras tai toinen kotimainen kieli")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaineenoppimaara")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "preiboppiaine")
case class VierasTaiToinenKotimainenKieli2015(
  @KoodistoKoodiarvo("A1")
  @KoodistoKoodiarvo("A2")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B2")
  @KoodistoKoodiarvo("B3")
  @KoodistoKoodiarvo("AOM")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @DefaultValue(None)
  laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine2015 with Kieliaine with Diaarinumerollinen {
  override def description = kieliaineDescription
}

@Title("Matematiikka")
@Description("Oppiaineena matematiikka")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "lukionoppiaineenoppimaara")
@OnlyWhen("../tyyppi/koodiarvo", "luvalukionoppiaine")
@OnlyWhen("../tyyppi/koodiarvo", "preiboppiaine")
case class LukionMatematiikka2015(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Onko kyseessä laaja vai lyhyt oppimäärä")
  @KoodistoUri("oppiainematematiikka")
  oppimäärä: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @DefaultValue(None)
  laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine2015 with KoodistostaLöytyväKoulutusmoduuli with Oppimäärä with Diaarinumerollinen {
  override def description = oppimäärä.description
}
