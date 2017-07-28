package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.{LocalizationRepository, LocalizedString}
import fi.oph.koski.localization.LocalizedString.{concat, english}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.scalaschema.annotation._

@Description("IB-tutkinnon opiskeluoikeus")
@Title("IB-tutkinnon opiskeluoikeus")
case class IBOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  alkamispäivä: Option[LocalDate] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  tila: LukionOpiskeluoikeudenTila,
  @MaxItems(2)
  suoritukset: List[IBPäätasonSuoritus],
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibtutkinto", "opiskeluoikeudentyyppi"),
  override val lisätiedot: Option[LukionOpiskeluoikeudenLisätiedot] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[IBPäätasonSuoritus]])
}

@Title("IB-tutkinnon suoritus")
case class IBTutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: IBTutkinto = IBTutkinto(),
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[IBOppiaineenSuoritus]],
  theoryOfKnowledge: Option[IBTheoryOfKnowledgeSuoritus],
  extendedEssay: Option[IBExtendedEssaySuoritus],
  creativityActionService: Option[IBCASSuoritus],
  @Description("Tutkinnon lisäpisteet. Max 3 pistettä yhteensä.")
  @KoodistoUri("arviointiasteikkolisapisteetib")
  lisäpisteet: Option[Koodistokoodiviite] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibtutkinto", koodistoUri = "suorituksentyyppi")
) extends IBPäätasonSuoritus with Todistus

@Title("Pre IB -opintojen suoritus")
case class PreIBSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: PreIBKoulutusmoduuli = PreIBKoulutusmoduuli(),
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PreIBOppiaineenSuoritus]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("preiboppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("preiboppimaara", koodistoUri = "suorituksentyyppi")
) extends IBPäätasonSuoritus

trait IBPäätasonSuoritus extends PäätasonSuoritus with Toimipisteellinen with Arvioinniton

@Title("Pre IB -koulutus")
@Description("Pre IB-koulutuksen tunnistetiedot.")
case class PreIBKoulutusmoduuli(
  @Description("Pre IB-koulutuksen tunniste.")
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("preiboppimaara")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("preiboppimaara", koodistoUri = "suorituksentyyppi")
) extends KoodistostaLöytyväKoulutusmoduuli {
  override def laajuus: Option[Laajuus] = None
}

@Title("IB-tutkinto")
@Description("IB tutkinnon tunnistetiedot")
case class IBTutkinto(
  @KoodistoKoodiarvo("301102")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("301102", koodistoUri = "koulutus")
) extends Koulutus {
  override def laajuus = None
  override def isTutkinto = true
}

trait IBSuoritus extends VahvistuksetonSuoritus

@Title("IB-oppiaineen suoritus")
case class IBOppiaineenSuoritus(
  @Description("IB-lukion oppiaineen tunnistetiedot.")
  @Title("Oppiaine")
  koulutusmoduuli: IBAineRyhmäOppiaine,
  tila: Koodistokoodiviite,
  arviointi: Option[List[IBOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[IBKurssinSuoritus]],
  @KoodistoKoodiarvo("iboppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiaine", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

@Description("Theory of Knowledge-suorituksen tiedot.")
@Title("IB Theory of Knowledge -suoritus")
case class IBTheoryOfKnowledgeSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBOppiaineTheoryOfKnowledge,
  tila: Koodistokoodiviite,
  arviointi: Option[List[IBCoreRequirementsArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[IBKurssinSuoritus]],
  @KoodistoKoodiarvo("iboppiainetok")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiainetok", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

@Description("CAS-suorituksen tiedot.")
@Title("IB CAS -suoritus")
case class IBCASSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBOppiaineCAS,
  tila: Koodistokoodiviite,
  arviointi: Option[List[IBOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("iboppiainecas")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiainecas", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

@Description("Extended Essay-suorituksen tiedot.")
@Title("IB Extended Essay -suoritus")
case class IBExtendedEssaySuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBOppiaineExtendedEssay,
  tila: Koodistokoodiviite,
  arviointi: Option[List[IBCoreRequirementsArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("iboppiaineee")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiaineee", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

@Description("Pre IB-oppiaineiden suoritusten tiedot.")
@Title("Pre IB -oppiaineen suoritus")
case class PreIBOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: PreIBOppiaine,
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[PreIBKurssinSuoritus]],
  @KoodistoKoodiarvo("preiboppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preiboppiaine", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

trait PreIBOppiaine extends Koulutusmoduuli

@Title("IB -oppinaineen arviointi")
case class IBOppiaineenArviointi(
  @Description("Onko arvoitu arvosana vai ei, jos ei niin tarkoittaa IBOn vahvistamaa arvosanaa")
  predicted: Boolean = true,
  @KoodistoUri("arviointiasteikkoib")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate]
) extends IBArviointi {
  override def arviointipäivä: Option[LocalDate] = päivä
}

@Title("Pre IB -kurssin suoritus")
case class PreIBKurssinSuoritus(
  @Description("Pre-IB kurssin tunnistetiedot")
  @Title("Kurssi")
  koulutusmoduuli: PreIBKurssi,
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionKurssinArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("preibkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preibkurssi", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

@Title("IB-kurssin suoritus")
case class IBKurssinSuoritus(
  @Description("IB kurssin tunnistetiedot")
  @Title("Kurssi")
  koulutusmoduuli: IBKurssi,
  tila: Koodistokoodiviite,
  arviointi: Option[List[IBKurssinArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ibkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ibkurssi", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

trait PreIBKurssi extends Koulutusmoduuli

@Title("IB-kurssi")
@Description("IB-lukion kurssin tunnistetiedot")
case class IBKurssi(
  kuvaus: LocalizedString,
  @OksaUri("tmpOKSAID873", "kurssi")
  tunniste: PaikallinenKoodi,
  @Discriminator
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa]
) extends Koulutusmoduuli with Valinnaisuus with PreIBKurssi {
  def nimi: LocalizedString = tunniste.nimi
}

@Title("IB-kurssin arviointi")
case class IBKurssinArviointi(
  @KoodistoUri("arviointiasteikkoib")
  arvosana: Koodistokoodiviite,
  @Description("Effort-arvosana, kuvaa opiskelijan tunnollisuutta, aktiivisuutta ja yritteliäisyyttä. Arvosteluasteikko: A = very good, B = good, C = needs improvement.")
  @KoodistoUri("effortasteikkoib")
  effort: Option[Koodistokoodiviite] = None,
  päivä: LocalDate
) extends IBArviointi with ArviointiPäivämäärällä

trait IBArviointi extends KoodistostaLöytyväArviointi {
  def arvosana: Koodistokoodiviite
  def arvioitsijat: Option[List[Arvioitsija]] = None
  override def arvosanaKirjaimin: LocalizedString = arvosana.nimi.getOrElse(english(arvosana.koodiarvo))
  def hyväksytty: Boolean = arvosana.koodiarvo match {
    case "O" | "1" => false
    case _ => true
  }
}

@Title("IB Core Requiremenst -arviointi")
case class IBCoreRequirementsArviointi(
  @KoodistoUri("arviointiasteikkocorerequirementsib")
  arvosana: Koodistokoodiviite,
  @Description("Onko arvoitu arvosana vai ei, jos ei niin tarkoittaa IBOn vahvistamaa arvosanaa")
  predicted: Boolean = true,
  päivä: Option[LocalDate]
) extends IBArviointi {
  override def arviointipäivä: Option[LocalDate] = päivä
  override def hyväksytty: Boolean = arvosana.koodiarvo match {
    case "f" => false
    case _ => true
  }
}

@Description("IB-lukion oppiaineen tunnistetiedot")
trait IBOppiaine extends KoodistostaLöytyväKoulutusmoduuli with Valinnaisuus {
  @KoodistoUri("oppiaineetib")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
  def laajuus: Option[LaajuusTunneissa]
}

trait IBAineRyhmäOppiaine extends IBOppiaine with PreIBOppiaine {
  @KoodistoUri("aineryhmaib")
  def ryhmä: Koodistokoodiviite
  @KoodistoUri("oppiaineentasoib")
  def taso: Option[Koodistokoodiviite]
}

@Title("Muu IB-oppiaine")
case class IBOppiaineMuu(
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusTunneissa],
  taso: Option[Koodistokoodiviite],
  ryhmä: Koodistokoodiviite,
  pakollinen: Boolean = true
) extends IBAineRyhmäOppiaine

@Title("IB-kielioppiaine")
case class IBOppiaineLanguage(
  @KoodistoKoodiarvo("A")
  @KoodistoKoodiarvo("A2")
  @KoodistoKoodiarvo("B")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusTunneissa],
  taso: Option[Koodistokoodiviite],
  @KoodistoUri("kielivalikoima")
  @Discriminator
  kieli: Koodistokoodiviite,
  ryhmä: Koodistokoodiviite,
  pakollinen: Boolean = true
) extends IBAineRyhmäOppiaine {
  override def description(text: LocalizationRepository) = concat(nimi, ", ",  kieli)
}

trait IBCoreElementOppiaine extends IBOppiaine {
}

@Title("IB-oppiaine CAS")
case class IBOppiaineCAS(
  @Description("Oppiaineen Creativity, activity, service tunniste.")
  @KoodistoKoodiarvo("CAS")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = "CAS", nimi = Some(english("Creativity, activity, service"))),
  laajuus: Option[LaajuusTunneissa],
  pakollinen: Boolean = true
) extends IBCoreElementOppiaine

@Title("IB-oppiaine Theory of Knowledge")
case class IBOppiaineTheoryOfKnowledge(
  @Description("Oppiaineen Theory of Knowledge tunniste.")
  @KoodistoKoodiarvo("TOK")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = "TOK", nimi = Some(english("Theory of knowledge"))),
  pakollinen: Boolean = true
) extends IBCoreElementOppiaine {
  override def laajuus: Option[LaajuusTunneissa] = None
}

@Title("IB-oppiaine Extended Essay")
case class IBOppiaineExtendedEssay(
  @Description("Oppiaineen Extended Essay tunniste.")
  @KoodistoKoodiarvo("EE")
  tunniste: Koodistokoodiviite =  Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = "EE", nimi = Some(english("Extended essay"))),
  aine: IBAineRyhmäOppiaine,
  aihe: LocalizedString,
  pakollinen: Boolean = true
) extends IBCoreElementOppiaine {
  override def laajuus: Option[LaajuusTunneissa] = None
}

case class LaajuusTunneissa(
  arvo: Float,
  @KoodistoKoodiarvo("5")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = "5", nimi = Some(english("hours")))
) extends Laajuus
