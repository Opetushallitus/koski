package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

@Description("Deutsche Internationale Abitur -tutkinnon opiskeluoikeus")
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
  tila: DIAOpiskeluoikeudenTila,
  @MaxItems(2)
  suoritukset: List[DIAPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.diatutkinto,
  override val lisätiedot: Option[DIAOpiskeluoikeudenLisätiedot] = None,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
}

case class DIAOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[DIAOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class DIAOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus. Mikäli kyseessä on kaksoitutkintoa suorittava opiskelija, jonka rahoituksen saa ammatillinen oppilaitos, tulee käyttää arvoa 6: Muuta kautta rahoitettu. Muussa tapauksessa käytetään arvoa 1: Valtionosuusrahoitteinen koulutus.")
  @KoodistoKoodiarvo("1")
  @KoodistoKoodiarvo("6")
  override val opintojenRahoitus: Option[Koodistokoodiviite] = None
) extends KoskiLaajaOpiskeluoikeusjakso

@Description("DIA-opiskeluoikeuden lisätiedot")
case class DIAOpiskeluoikeudenLisätiedot(
  @Description("Opiskeluajan pidennetty päättymispäivä (true/false).")
  @DefaultValue(false)
  pidennettyPäättymispäivä: Boolean = false,
  @Description("Opiskelija on ulkomainen vaihto-opiskelija Suomessa (true/false).")
  @Title("Ulkomainen vaihto-opiskelija.")
  @DefaultValue(false)
  ulkomainenVaihtoopiskelija: Boolean = false,
  erityisenKoulutustehtävänJaksot: Option[List[ErityisenKoulutustehtävänJakso]] = None,
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None
) extends OpiskeluoikeudenLisätiedot with ErityisenKoulutustehtävänJaksollinen with Ulkomaanjaksollinen with UlkomainenVaihtoopiskelija with MaksuttomuusTieto

trait DIAPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Arvioinniton with Suorituskielellinen with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta

@Title("DIA-tutkinnon suoritus")
case class DIATutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: DIATutkinto = DIATutkinto(),
  toimipiste: OrganisaatioWithOid,
  @MinValue(0)
  @MaxValue(900)
  kokonaispistemäärä: Option[Int] = None,
  @MinValue(0)
  @MaxValue(600)
  lukukausisuoritustenKokonaispistemäärä: Option[Int] = None,
  @MinValue(0)
  @MaxValue(300)
  tutkintoaineidenKokonaispistemäärä: Option[Int] = None,
  @MinValue(1.0)
  @MaxValue(6.0)
  @Scale(1)
  kokonaispistemäärästäJohdettuKeskiarvo: Option[Double] = None,
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
case class DIATutkinto(
  @KoodistoKoodiarvo("301103")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("301103", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Koulutus with Laajuudeton with Tutkinto


trait DIASuoritus extends Vahvistukseton

@Title("DIA-oppiaineen valmistavan vaiheen suoritus")
case class DIAOppiaineenValmistavanVaiheenSuoritus(
  @Description("DIA-oppiaineen tunnistetiedot")
  @Title("Oppiaine")
  koulutusmoduuli: DIAOppiaine,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien lukukausien suoritukset")
  @Title("Lukukaudet")
  @MaxItems(2)
  override val osasuoritukset: Option[List[DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus]],
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "diaoppiaine", koodistoUri = "suorituksentyyppi")
) extends DIASuoritus with Arvioinniton with MahdollisestiSuorituskielellinen

@Title("DIA-oppiaineen tutkintovaiheen suoritus")
case class DIAOppiaineenTutkintovaiheenSuoritus(
  @Description("DIA-oppiaineen tunnistetiedot")
  @Title("Oppiaine")
  koulutusmoduuli: DIAOppiaine,
  suorituskieli: Option[Koodistokoodiviite] = None,
  vastaavuustodistuksenTiedot: Option[DIAVastaavuustodistuksenTiedot] = None,
  @Title("Koetuloksen nelinkertainen pistemäärä")
  @MinValue(0)
  @MaxValue(60)
  koetuloksenNelinkertainenPistemäärä: Option[Int] = None,
  @Description("Oppiaineeseen kuuluvien lukukausien ja muiden osasuoritusten suoritukset")
  override val osasuoritukset: Option[List[DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus]],
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "diaoppiaine", koodistoUri = "suorituksentyyppi")
) extends DIASuoritus with Arvioinniton with MahdollisestiSuorituskielellinen

@Title("DIA-oppiaineen valmistavan vaiheen lukukauden suoritus")
case class DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus(
  koulutusmoduuli: DIAOppiaineenValmistavanVaiheenLukukausi,
  arviointi: Option[List[DIAOppiaineenValmistavanVaiheenLukukaudenArviointi]] = None,
  @KoodistoKoodiarvo("diaoppiaineenvalmistavanvaiheenlukukaudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "diaoppiaineenvalmistavanvaiheenlukukaudensuoritus", koodistoUri = "suorituksentyyppi")
) extends DIASuoritus

@Title("DIA-oppiaineen tutkintovaiheen osasuorituksen suoritus")
case class DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
  koulutusmoduuli: DIAOppiaineenTutkintovaiheenOsasuoritus,
  arviointi: Option[List[DIATutkintovaiheenArviointi]] = None,
  @KoodistoKoodiarvo("diaoppiaineentutkintovaiheenosasuorituksensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "diaoppiaineentutkintovaiheenosasuorituksensuoritus", koodistoUri = "suorituksentyyppi")
) extends DIASuoritus

trait DIAOppiaineenOsasuoritus extends KoodistostaLöytyväKoulutusmoduuli

trait DIAOppiaineenLukukausi extends DIAOppiaineenOsasuoritus with KoodistostaLöytyväKoulutusmoduuliValinnainenLaajuus {
  @KoodistoUri("dialukukausi")
  def tunniste: Koodistokoodiviite
}

trait DIAOppiaineenTutkintovaiheenOsasuoritus extends DIAOppiaineenOsasuoritus

@Title("DIA-oppiaineen valmistavan vaiheen lukukausi")
@Description("DIA-oppiaineen valmistavan vaiheen lukukauden tunnistetiedot")
case class DIAOppiaineenValmistavanVaiheenLukukausi(
  @KoodistoKoodiarvo("1")
  @KoodistoKoodiarvo("2")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa]
) extends DIAOppiaineenLukukausi

@Title("DIA-oppiaineen tutkintovaiheen lukukausi")
@Description("DIA-oppiaineen tutkintovaiheen lukukauden tunnistetiedot")
case class DIAOppiaineenTutkintovaiheenLukukausi(
  @KoodistoKoodiarvo("3")
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa]
) extends DIAOppiaineenLukukausi with DIAOppiaineenTutkintovaiheenOsasuoritus

@Title("DIA-tutkinnon päättökoe")
@Description("DIA-tutkinnon päättökokeen tunnistetiedot")
case class DIAPäättökoe (
  @KoodistoUri("diapaattokoe")
  @KoodistoKoodiarvo("kirjallinenkoe")
  @KoodistoKoodiarvo("suullinenkoe")
  tunniste: Koodistokoodiviite
) extends DIAOppiaineenTutkintovaiheenOsasuoritus with Laajuudeton

@Title("DIA-tutkinnon erityisosaamisen näyttötutkinto")
@Description("DIA-tutkinnon erityisosaamisen näyttötutkinnon tunnistetiedot")
case class DIANäyttötutkinto (
  @KoodistoUri("diapaattokoe")
  @KoodistoKoodiarvo("nayttotutkinto")
  tunniste: Koodistokoodiviite
) extends DIAOppiaineenTutkintovaiheenOsasuoritus with Laajuudeton

case class DIAVastaavuustodistuksenTiedot(
  keskiarvo: Double,
  @Description("Valmistavan DIA-vaiheen ja DIA-tutkintovaiheen yhteenlaskettu laajuus")
  @Tooltip("Valmistavan DIA-vaiheen ja DIA-tutkintovaiheen yhteenlaskettu laajuus")
  lukioOpintojenLaajuus: LaajuusOpintopisteissäTaiKursseissa
)

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
  override def hyväksytty = true
}

trait DIATutkintovaiheenArviointi extends DIAArviointi {
  @KoodistoUri("arviointiasteikkodiatutkinto")
  def arvosana: Koodistokoodiviite
  def päivä: Option[LocalDate]
  override def arviointipäivä: Option[LocalDate] = päivä
  override def hyväksytty = true
}

@Title("DIA-oppiaineen tutkintovaiheen numeerinen arviointi")
case class DIAOppiaineenTutkintovaiheenNumeerinenArviointi(
  @KoodistoKoodiarvo("0")
  @KoodistoKoodiarvo("1")
  @KoodistoKoodiarvo("2")
  @KoodistoKoodiarvo("2-")
  @KoodistoKoodiarvo("3")
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoKoodiarvo("8")
  @KoodistoKoodiarvo("9")
  @KoodistoKoodiarvo("10")
  @KoodistoKoodiarvo("11")
  @KoodistoKoodiarvo("12")
  @KoodistoKoodiarvo("13")
  @KoodistoKoodiarvo("14")
  @KoodistoKoodiarvo("15")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate],
  @DefaultValue(true)
  lasketaanKokonaispistemäärään: Boolean = true
) extends DIATutkintovaiheenArviointi

case class DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi(
  @KoodistoKoodiarvo("S")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate]
) extends DIATutkintovaiheenArviointi

@Description("DIA-oppiaineen tunnistetiedot")
trait DIAOppiaine extends KoodistostaLöytyväKoulutusmoduuliValinnainenLaajuus {
  @KoodistoUri("oppiaineetdia")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
  def laajuus: Option[LaajuusVuosiviikkotunneissa]
}

trait DIAOsaAlueOppiaine extends DIAOppiaine {
  @KoodistoUri("diaosaalue")
  @Title("Osa-alue")
  def osaAlue: Koodistokoodiviite
}

@Title("Muu DIA-oppiaine")
case class DIAOppiaineMuu(
  @Description("DIA-lukion oppiaineen tunnistetiedot")
  @KoodistoKoodiarvo("KU")
  @KoodistoKoodiarvo("MU")
  @KoodistoKoodiarvo("MA")
  @KoodistoKoodiarvo("FY")
  @KoodistoKoodiarvo("BI")
  @KoodistoKoodiarvo("KE")
  @KoodistoKoodiarvo("TI")
  @KoodistoKoodiarvo("TK")
  @KoodistoKoodiarvo("HI")
  @KoodistoKoodiarvo("MAA")
  @KoodistoKoodiarvo("TA")
  @KoodistoKoodiarvo("US")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("ET")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa],
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
  laajuus: Option[LaajuusVuosiviikkotunneissa],
  @KoodistoUri("kielivalikoima")
  @KoodistoKoodiarvo("EN")
  @KoodistoKoodiarvo("FR")
  @KoodistoKoodiarvo("SV")
  @KoodistoKoodiarvo("RU")
  @Discriminator
  @Description("Mikä kieli on kyseessä")
  kieli: Koodistokoodiviite,
  @Description("Oppiaineen osa-alue (1)")
  @KoodistoKoodiarvo("1")
  osaAlue: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "1", koodistoUri = "diaosaalue"),
  pakollinen: Boolean = true
) extends DIAOsaAlueOppiaine with Kieliaine {
  override def description = kieliaineDescription
}

@Title("DIA-äidinkieli")
case class DIAOppiaineÄidinkieli(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa],
  @KoodistoUri("oppiainediaaidinkieli")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("S2")
  @KoodistoKoodiarvo("DE")
  kieli: Koodistokoodiviite,
  @Description("Oppiaineen osa-alue (1)")
  @KoodistoKoodiarvo("1")
  osaAlue: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "1", koodistoUri = "diaosaalue")
) extends DIAOsaAlueOppiaine with Äidinkieli {
  override def description = kieliaineDescription
}

@Title("DIA-lisäaine")
case class DIAOppiaineLisäaine(
  @Description("DIA-lukion lisäoppiaineen tunnistetiedot")
  @KoodistoKoodiarvo("CLOE")
  @KoodistoKoodiarvo("CCEA")
  @KoodistoKoodiarvo("LT")
  @KoodistoKoodiarvo("MASY")
  @KoodistoKoodiarvo("MALI")
  @KoodistoKoodiarvo("LI")
  @KoodistoKoodiarvo("VELI")
  @KoodistoKoodiarvo("ELI")
  @KoodistoKoodiarvo("RALI")
  @KoodistoKoodiarvo("VT")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa]
) extends DIAOppiaine

case class DIAOppiaineLisäaineKieli(
  @KoodistoKoodiarvo("B2")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa],
  @KoodistoUri("kielivalikoima")
  @KoodistoKoodiarvo("LA")
  @Discriminator
  @Description("Mikä kieli on kyseessä")
  kieli: Koodistokoodiviite
) extends DIAOppiaine with Kieliaine {
  override def description = kieliaineDescription
}
