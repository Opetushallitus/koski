package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.english
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}

@Description("IB opiskeluoikeus")
case class IBOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid] = None,
  alkamispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  tila: LukionOpiskeluoikeudenTila,
  läsnäolotiedot: Option[YleisetLäsnäolotiedot] = None,
  @MinItems(1) @MaxItems(2)
  suoritukset: List[IBPäätasonSuoritus],
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibtutkinto", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä: Option[LocalDate] = None
}

case class IBTutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: IBTutkinto,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[IBOppiaineenSuoritus]],
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ibtutkinto", koodistoUri = "suorituksentyyppi")
) extends IBPäätasonSuoritus

case class PreIBSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: PreIBKoulutusmoduuli,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite],
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
  nimi: LocalizedString,
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("preiboppimaara")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("preiboppimaara", koodistoUri = "suorituksentyyppi")
) extends Koulutusmoduuli {
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
  koulutusmoduuli: IBOppiaine,
  tila: Koodistokoodiviite,
  arviointi: Option[List[IBOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[IBKurssinSuoritus]],
  @KoodistoKoodiarvo("iboppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiaine", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus


case class PreIBOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: PreIBOppiaine,
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[PreIBKurssinSuoritus]],
  @KoodistoKoodiarvo("iboppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiaine", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

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
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("ibkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ibkurssi", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

case class IBKurssinSuoritus(
  @Description("IB kurssin tunnistetiedot")
  @Title("Kurssi")
  koulutusmoduuli: IBKurssi,
  tila: Koodistokoodiviite,
  arviointi: Option[List[IBKurssinArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("ibkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ibkurssi", koodistoUri = "suorituksentyyppi")
) extends IBSuoritus

trait PreIBKurssi extends Koulutusmoduuli

@Description("IB-lukion kurssin tunnistetiedot")
case class IBKurssi(
  @KoodistoUri("ibkurssit") // TODO: pystyykö näitä edes koodittamaan?
  @OksaUri("tmpOKSAID873", "kurssi")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa]
) extends KoodistostaLöytyväKoulutusmoduuli with Valinnaisuus with PreIBKurssi

case class IBKurssinArviointi(
  //TODO: Effort
  arvosana: Koodistokoodiviite,
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
trait IBOppiaine extends KoodistostaLöytyväKoulutusmoduuli with Valinnaisuus with PreIBOppiaine {
  @KoodistoUri("oppiaineetib") // TODO: lisää tämä
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
  def laajuus: Option[LaajuusTunneissa]
  @KoodistoUri("oppiaineentasoib")
  def taso: Option[Koodistokoodiviite]
  def pakollinen: Boolean = true
}

case class IBCoreElementOppiaine(
  @KoodistoKoodiarvo("TOK")
  @KoodistoKoodiarvo("EE")
  @KoodistoKoodiarvo("CAS")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusTunneissa],
  taso: Option[Koodistokoodiviite] = None
) extends IBOppiaine


case class LaajuusTunneissa(
  arvo: Float,
  @KoodistoKoodiarvo("5")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = "5", nimi = Some(english("hours")))
) extends Laajuus
