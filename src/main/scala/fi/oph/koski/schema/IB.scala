package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.{concat, english}
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}

@Description("IB opiskeluoikeus")
case class IBOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OidOrganisaatio] = None,
  alkamispäivä: Option[LocalDate] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  tila: LukionOpiskeluoikeudenTila,
  läsnäolotiedot: Option[YleisetLäsnäolotiedot] = None,
  @MinItems(1) @MaxItems(2)
  suoritukset: List[IBPäätasonSuoritus],
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibtutkinto", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OidOrganisaatio) = this.copy(koulutustoimija = Some(koulutustoimija))
}

case class IBTutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: IBTutkinto = IBTutkinto(),
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[IBOppiaineenSuoritus]],
  theoryOfKnowledge: Option[IBTheoryOfKnowledgeSuoritus],
  extendedEssay: Option[IBExtendedEssaySuoritus],
  creativityActionService: Option[IBCASSuoritus],
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibtutkinto", koodistoUri = "suorituksentyyppi")
) extends IBPäätasonSuoritus

case class PreIBSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: PreIBKoulutusmoduuli = PreIBKoulutusmoduuli(),
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PreIBOppiaineenSuoritus]],
  @KoodistoKoodiarvo("preiboppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("preiboppimaara", koodistoUri = "suorituksentyyppi")
) extends IBPäätasonSuoritus

trait IBPäätasonSuoritus extends Suoritus with Toimipisteellinen {
  def arviointi = None
}

case class PreIBKoulutusmoduuli(
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("preiboppimaara")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("preiboppimaara", koodistoUri = "suorituksentyyppi")
) extends KoodistostaLöytyväKoulutusmoduuli {
  override def laajuus: Option[Laajuus] = None
}

@Description("IB tutkinnon tunnistetiedot")
case class IBTutkinto(
  @KoodistoKoodiarvo("301102")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("301102", koodistoUri = "koulutus")
) extends Koulutus {
  override def laajuus = None
  override def isTutkinto = true
}

trait IBSuoritus extends Suoritus {
  override def tarvitseeVahvistuksen = false
  override def vahvistus: Option[Vahvistus] = None
}

case class IBOppiaineenSuoritus(
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

case class IBTheoryOfKnowledgeSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBOppiaineTheoryOfKnowledge,
  tila: Koodistokoodiviite,
  arviointi: Option[List[IBOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[IBKurssinSuoritus]],
  @KoodistoKoodiarvo("iboppiainetok")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiainetok", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

case class IBCASSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBOppiaineCAS,
  tila: Koodistokoodiviite,
  arviointi: Option[List[IBOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("iboppiainecas")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiainecas", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

case class IBExtendedEssaySuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBOppiaineExtendedEssay,
  tila: Koodistokoodiviite,
  arviointi: Option[List[IBOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("iboppiaineee")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiaineee", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

case class PreIBOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: PreIBOppiaine,
  tila: Koodistokoodiviite,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[PreIBKurssinSuoritus]],
  @KoodistoKoodiarvo("preiboppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "preiboppiaine", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus {
  def arviointi = None
}

trait PreIBOppiaine extends Koulutusmoduuli

case class IBOppiaineenArviointi(
  @Description("Onko arvoitu arvosana vai ei, jos ei niin tarkoittaa IBOn vahvistamaa arvosanaa")
  predicted: Boolean = true,
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate]
) extends IBArviointi {
  override def arviointipäivä: Option[LocalDate] = päivä
}

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

@Description("IB-lukion kurssin tunnistetiedot")
case class IBKurssi(
  @OksaUri("tmpOKSAID873", "kurssi")
  tunniste: PaikallinenKoodi,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa]
) extends Koulutusmoduuli with Valinnaisuus with PreIBKurssi {
  def nimi: LocalizedString = tunniste.nimi
}

case class IBKurssinArviointi(
  arvosana: Koodistokoodiviite,
  @KoodistoUri("effortasteikkoib")
  effort: Option[Koodistokoodiviite] = None,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  päivä: LocalDate
) extends IBArviointi with ArviointiPäivämäärällä

trait IBArviointi extends KoodistostaLöytyväArviointi {
  @KoodistoUri("arviointiasteikkoib")
  def arvosana: Koodistokoodiviite
  def arvioitsijat: Option[List[Arvioitsija]] = None
  override def arvosanaKirjaimin: LocalizedString = arvosana.nimi.getOrElse(english(arvosana.koodiarvo))
  def hyväksytty: Boolean = arvosana.koodiarvo match {
    case "O" | "1" => false
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

case class IBOppiaineMuu(
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusTunneissa],
  taso: Option[Koodistokoodiviite],
  ryhmä: Koodistokoodiviite,
  pakollinen: Boolean = true
) extends IBAineRyhmäOppiaine

case class IBOppiaineLanguage(
  @KoodistoKoodiarvo("A")
  @KoodistoKoodiarvo("A2")
  @KoodistoKoodiarvo("B")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusTunneissa],
  taso: Option[Koodistokoodiviite],
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  ryhmä: Koodistokoodiviite,
  pakollinen: Boolean = true
) extends IBAineRyhmäOppiaine {
  override def description = concat(nimi, ", ",  kieli)
}

trait IBCoreElementOppiaine extends IBOppiaine {
}

case class IBOppiaineCAS(
  @KoodistoKoodiarvo("CAS")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = "CAS", nimi = Some(english("Theory of knowledge"))),
  laajuus: Option[LaajuusTunneissa],
  pakollinen: Boolean = true
) extends IBCoreElementOppiaine

case class IBOppiaineTheoryOfKnowledge(
  @KoodistoKoodiarvo("TOK")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = "TOK", nimi = Some(english("Creativity, activity, service"))),
  pakollinen: Boolean = true
) extends IBCoreElementOppiaine {
  override def laajuus: Option[LaajuusTunneissa] = None
}

case class IBOppiaineExtendedEssay(
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
