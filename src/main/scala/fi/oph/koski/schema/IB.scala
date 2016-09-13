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
  suoritukset: List[IBTutkinnonOsanSuoritus],
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
) extends IBTutkinnonOsanSuoritus

case class PreIBSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: IBKoulutus, //TODO: joku tähän
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PreIBOppiaineenSuoritus]],
  @KoodistoKoodiarvo("preiboppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("preiboppimaara", koodistoUri = "suorituksentyyppi")
) extends IBTutkinnonOsanSuoritus

trait IBTutkinnonOsanSuoritus extends Suoritus with Toimipisteellinen {
  def arviointi = None
}

trait IBKoulutus extends Koulutus {
  override def laajuus = None
  override def isTutkinto = true
}

@Description("IB tutkinnon tunnistetiedot")
case class IBTutkinto(
  @KoodistoKoodiarvo("301102")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("301102", koodistoUri = "koulutus")
) extends IBKoulutus

trait IBSuoritus extends Suoritus {
  def koulutusmoduuli: KoodistostaLöytyväKoulutusmoduuli with Valinnaisuus
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


trait PreIBOppiaineenSuoritus extends IBSuoritus

case class PreIBLukionOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: LukionOppiaine,
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]],
  @KoodistoKoodiarvo("iboppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiaine", koodistoUri = "suorituksentyyppi")
) extends PreIBOppiaineenSuoritus

case class PreIBVarsinaisenIBOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: IBOppiaine,
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[IBKurssinSuoritus]],
  @KoodistoKoodiarvo("iboppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "iboppiaine", koodistoUri = "suorituksentyyppi")
) extends PreIBOppiaineenSuoritus

case class IBOppiaineenArviointi(
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate]
) extends IBArviointi {
  override def arviointipäivä: Option[LocalDate] = päivä
}

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

@Description("IB-lukion kurssin tunnistetiedot")
case class IBKurssi(
  @KoodistoUri("ibkurssit") // TODO: lisää tämä
  @OksaUri("tmpOKSAID873", "kurssi")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa]
) extends KoodistostaLöytyväKoulutusmoduuli with Valinnaisuus

case class IBKurssinArviointi(
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
case class IBOppiaine(
  @KoodistoUri("koskioppiaineetib") // TODO: lisää tämä
  @OksaUri("tmpOKSAID256", "oppiaine")
  @KoodistoKoodiarvo("TODO")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean,
  laajuus: Option[Laajuus]
) extends KoodistostaLöytyväKoulutusmoduuli with Valinnaisuus

