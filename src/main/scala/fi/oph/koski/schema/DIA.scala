package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, OksaUri}
import fi.oph.scalaschema.annotation._

@Description("DIA-tutkinnon opiskeluoikeus")
@Title("DIA-tutkinnon opiskeluoikeus")
case class DIAOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  tila: LukionOpiskeluoikeudenTila,
  @MaxItems(2)
  suoritukset: List[DIAPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.diatutkinto,
  override val lisätiedot: Option[LukionOpiskeluoikeudenLisätiedot] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
}


trait DIAPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Arvioinniton with Suorituskielellinen

@Title("DIA-tutkintovaiheen suoritus")
case class DIATutkintovaiheenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: DIATutkintovaihe = DIATutkintovaihe(),
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[DIAOppiaineenTutkintovaiheenSuoritus]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("diatutkintovaihe", koodistoUri = "suorituksentyyppi")
) extends DIAPäätasonSuoritus with Todistus

@Title("Valmistavan DIA-vaiheen suoritus")
case class DIAValmistavanVaiheenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: DIAValmistavaVaihe = DIAValmistavaVaihe(),
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[DIAOppiaineenValmistavanVaiheenSuoritus]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("diavalmistavavaihe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("diavalmistavavaihe", koodistoUri = "suorituksentyyppi")
) extends DIAPäätasonSuoritus

@Title("Valmistava DIA-vaihe")
@Description("Valmistavan DIA-vaiheen tunnistetiedot")
case class DIAValmistavaVaihe(
  @Description("Valmistavan DIA-vaiheen tunniste")
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("diavalmistavavaihe")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("diavalmistavavaihe", koodistoUri = "suorituksentyyppi")
) extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton

@Title("DIA-tutkintovaihe")
@Description("DIA-tutkintovaiheen tunnistetiedot")
case class DIATutkintovaihe(
  @KoodistoKoodiarvo("301103")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("301103", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Koulutus with Laajuudeton with Tutkinto


trait DIASuoritus extends Vahvistukseton with MahdollisestiSuorituskielellinen

@Title("DIA-oppiaineen valmistavan vaiheen suoritus")
case class DIAOppiaineenValmistavanVaiheenSuoritus(
  @Description("DIA-oppiaineen tunnistetiedot")
  @Title("Oppiaine")
  koulutusmoduuli: DIAOsaAlueOppiaine,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien lukukausien suoritukset")
  @Title("Lukukaudet")
  @MaxItems(2)
  override val osasuoritukset: Option[List[DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus]],
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "diaoppiaine", koodistoUri = "suorituksentyyppi")
) extends DIASuoritus with Arvioinniton

@Title("DIA-oppiaineen tutkintovaiheen suoritus")
case class DIAOppiaineenTutkintovaiheenSuoritus(
  @Description("DIA-oppiaineen tunnistetiedot")
  @Title("Oppiaine")
  koulutusmoduuli: DIAOsaAlueOppiaine,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien lukukausien suoritukset")
  @Title("Lukukaudet")
  @MaxItems(4)
  override val osasuoritukset: Option[List[DIAOppiaineenTutkintovaiheenLukukaudenSuoritus]],
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "diaoppiaine", koodistoUri = "suorituksentyyppi")
) extends DIASuoritus with Arvioinniton

@Title("DIA-oppiaineen valmistavan vaiheen lukukauden suoritus")
case class DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus(
  koulutusmoduuli: DIAOppiaineenLukukausi,
  arviointi: Option[List[DIAOppiaineenValmistavanVaiheenLukukaudenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("diaoppiaineenvalmistavanvaiheenlukukaudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "diaoppiaineenvalmistavanvaiheenlukukaudensuoritus", koodistoUri = "suorituksentyyppi")
) extends DIASuoritus with KurssinSuoritus

@Title("DIA-oppiaineen tutkintovaiheen lukukauden suoritus")
case class DIAOppiaineenTutkintovaiheenLukukaudenSuoritus(
  koulutusmoduuli: DIAOppiaineenLukukausi,
  arviointi: Option[List[DIAOppiaineenTutkintovaiheenLukukaudenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("diaoppiaineentutkintovaiheenlukukaudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "diaoppiaineentutkintovaiheenlukukaudensuoritus", koodistoUri = "suorituksentyyppi")
) extends DIASuoritus with KurssinSuoritus

@Title("DIA-oppiaineen lukukausi")
@Description("DIA-oppiaineen lukukauden tunnistetiedot")
case class DIAOppiaineenLukukausi(
  @KoodistoUri("dialukukausi")
  tunniste: Koodistokoodiviite,
) extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton


trait DIAArviointi extends KoodistostaLöytyväArviointi {
  def arvosana: Koodistokoodiviite
  def arvioitsijat: Option[List[Arvioitsija]] = None
}

@Title("DIA-oppiaineen valmistavan vaiheen lukukauden arviointi")
case class DIAOppiaineenValmistavanVaiheenLukukaudenArviointi(
  @KoodistoUri("arviointiasteikkodiavalmistava")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate]
) extends DIAArviointi {
  override def arviointipäivä: Option[LocalDate] = päivä
  override def hyväksytty: Boolean = arvosana.koodiarvo match {
    case "6" => false
    case _ => true
  }
}

@Title("DIA-oppiaineen tutkintovaiheen lukukauden arviointi")
case class DIAOppiaineenTutkintovaiheenLukukaudenArviointi(
  @KoodistoUri("arviointiasteikkodiatutkinto")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate]
) extends DIAArviointi {
  override def arviointipäivä: Option[LocalDate] = päivä
  override def hyväksytty: Boolean = arvosana.koodiarvo match {
    case "0" => false
    case _ => true
  }
}


@Description("DIA-oppiaineen tunnistetiedot")
trait DIAOppiaine extends KoodistostaLöytyväKoulutusmoduuli {
  @KoodistoUri("oppiaineetdia")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
}

trait DIAOsaAlueOppiaine extends DIAOppiaine {
  @KoodistoUri("diaosaalue")
  def osaAlue: Koodistokoodiviite
}

@Title("Muu DIA-oppiaine")
case class DIAOppiaineMuu(
  @Description("DIA-lukion oppiaineen tunnistetiedot")
  @KoodistoKoodiarvo("SA")
  @KoodistoKoodiarvo("SU")
  @KoodistoKoodiarvo("A")
  @KoodistoKoodiarvo("B1RA")
  @KoodistoKoodiarvo("B1RU")
  @KoodistoKoodiarvo("B3VE")
  @KoodistoKoodiarvo("B3RA")
  @KoodistoKoodiarvo("KU")
  @KoodistoKoodiarvo("MU")
  @KoodistoKoodiarvo("MA")
  @KoodistoKoodiarvo("FY")
  @KoodistoKoodiarvo("BI")
  @KoodistoKoodiarvo("KE")
  @KoodistoKoodiarvo("TI")
  @KoodistoKoodiarvo("TK")
  @KoodistoKoodiarvo("HISA")
  @KoodistoKoodiarvo("HISU")
  @KoodistoKoodiarvo("MAA")
  @KoodistoKoodiarvo("TA")
  @KoodistoKoodiarvo("US")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("ET")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusTunneissa],
  @Description("Oppiaineen osa-alue (1-3)")
  osaAlue: Koodistokoodiviite,
  pakollinen: Boolean = true
) extends DIAOsaAlueOppiaine

@Title("DIA-kielioppiaine")
case class DIAOppiaineKieli(
  @Description("DIA-lukion kielioppiaineen tunnistetiedot")
  @KoodistoKoodiarvo("A")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B3")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusTunneissa],
  @KoodistoUri("kielivalikoima")
  @KoodistoKoodiarvo("FR")
  @KoodistoKoodiarvo("SV")
  @KoodistoKoodiarvo("RU")
  @Discriminator
  @Description("Mikä kieli on kyseessä")
  kieli: Koodistokoodiviite,
  @Description("Oppiaineen osa-alue (1)")
  @KoodistoKoodiarvo("1")
  osaAlue: Koodistokoodiviite,
  pakollinen: Boolean = true
) extends DIAOsaAlueOppiaine with Kieliaine {
  override def description = kieliaineDescription
}
