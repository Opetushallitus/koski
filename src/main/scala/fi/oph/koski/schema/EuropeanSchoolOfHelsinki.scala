package fi.oph.koski.schema

import fi.oph.koski.koodisto.SynteettinenKoodisto

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

/******************************************************************************
 * OPISKELUOIKEUS
 *****************************************************************************/

case class EuropeanSchoolOfHelsinkiOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos] = None,
  koulutustoimija: Option[Koulutustoimija] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  tila: EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila,
  suoritukset: List[EuropeanSchoolOfHelsinkiVuosiluokanSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.europeanschoolofhelsinki,
  override val lisätiedot: Option[EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot] = None,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
}

object EuropeanSchoolOfHelsinkiOpiskeluoikeus {

  lazy val synteettisetKoodistot: List[SynteettinenKoodisto] = List(
    new NumericalMarkSynteettinenKoodisto,
    new S7PreliminaryMarkSynteettinenKoodisto,
    new S7FinalMarkSynteettinenKoodisto
  )

  // TODO: TOR-1685 Saatetaan tarvita
  /*
  def onPeruskouluaVastaavaInternationalSchoolinSuoritus(
    suorituksenTyyppi: String,
    koulutusmoduulinKoodiarvo: String
  ): Boolean =
  {
    (suorituksenTyyppi, koulutusmoduulinKoodiarvo) match {
      case ("internationalschoolpypvuosiluokka", vuosiluokka) => true
      case ("internationalschoolmypvuosiluokka", vuosiluokka) if vuosiluokka != "10" => true
      case _ => false
    }
  }

  def onLukiotaVastaavaInternationalSchoolinSuoritus(
    suorituksenTyyppi: String,
    koulutusmoduulinKoodiarvo: String
  ): Boolean =
  {
    (suorituksenTyyppi, koulutusmoduulinKoodiarvo) match {
      case ("internationalschooldiplomavuosiluokka", _) => true
      case ("internationalschoolmypvuosiluokka", "10") => true
      case _ => false
    }
  }

  def onPeruskoulunPäättötodistustaVastaavaInternationalSchoolinSuoritus(
    suorituksenTyyppi: String,
    koulutusmoduulinKoodiarvo: String
  ): Boolean =
  {
    (suorituksenTyyppi, koulutusmoduulinKoodiarvo) match {
      case ("internationalschoolmypvuosiluokka", "9") => true
      case _ => false
    }
  }

  def onLukionPäättötodistustaVastaavaInternationalSchoolinSuoritus(
    suorituksenTyyppi: String,
    koulutusmoduulinKoodiarvo: String
  ): Boolean =
  {
    (suorituksenTyyppi, koulutusmoduulinKoodiarvo) match {
      case ("internationalschooldiplomavuosiluokka", "12") => true
      case _ => false
    }
  }
  */
}


/******************************************************************************
 * TILAT
 *****************************************************************************/

case class EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("eronnut")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("mitatoity")
  @KoodistoKoodiarvo("valiaikaisestikeskeytynyt")
  @KoodistoKoodiarvo("valmistunut")
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus. Toistaiseksi läsnä- ja valmistunut-tiloille aina 6: Muuta kautta rahoitettu")
  @KoodistoKoodiarvo("6")
  override val opintojenRahoitus: Option[Koodistokoodiviite] = Some(Koodistokoodiviite("6", "opintojenrahoitus"))
) extends KoskiOpiskeluoikeusjakso

/******************************************************************************
 * LISÄTIEDOT
 *****************************************************************************/

case class EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot(
  @Description("Rahoituksen laskennassa käytettävä tieto.")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None
) extends OpiskeluoikeudenLisätiedot with Ulkomaanjaksollinen with MaksuttomuusTieto

/******************************************************************************
 * PÄÄTASON SUORITUKSET
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiVuosiluokanSuoritus
  extends KoskeenTallennettavaPäätasonSuoritus
    with Toimipisteellinen
    with Arvioinniton
    with Suorituskielellinen
    with LuokalleJääntiTiedonSisältäväSuoritus
    with Todistus
    {
  @Title("Koulutus")
  def tyyppi: Koodistokoodiviite
  @Description("Luokan tunniste, esimerkiksi 9C.")
  @Tooltip("Luokan tunniste, esimerkiksi 9C.")
  def luokka: Option[String]
  def jääLuokalle: Boolean
  @Tooltip("Vuosiluokan alkamispäivä")
  def alkamispäivä: Option[LocalDate]
  def ilmanAlkamispäivää(): EuropeanSchoolOfHelsinkiVuosiluokanSuoritus
  @Tooltip("Todistuksella näkyvät lisätiedot. Esimerkiksi vuosiluokan sanallinen yleisarviointi.")
  def todistuksellaNäkyvätLisätiedot: Option[LocalizedString]
}

case class NurseryVuosiluokanSuoritus(
  koulutusmoduuli: NurseryLuokkaAste,
  luokka: Option[String] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  suorituskieli: Koodistokoodiviite,
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkanursery")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("europeanschoolofhelsinkivuosiluokkanursery", koodistoUri = "suorituksentyyppi"),
  jääLuokalle: Boolean = false,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None
) extends EuropeanSchoolOfHelsinkiVuosiluokanSuoritus {
  override def ilmanAlkamispäivää(): EuropeanSchoolOfHelsinkiVuosiluokanSuoritus = this.copy(alkamispäivä = None)
}

case class PrimaryVuosiluokanSuoritus(
  koulutusmoduuli: PrimaryLuokkaAste,
  luokka: Option[String] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  suorituskieli: Koodistokoodiviite,
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkaprimary")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("europeanschoolofhelsinkivuosiluokkaprimary", koodistoUri = "suorituksentyyppi"),
  jääLuokalle: Boolean = false,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  override val osasuoritukset: Option[List[PrimaryOsasuoritus]] = None
) extends EuropeanSchoolOfHelsinkiVuosiluokanSuoritus {
  override def ilmanAlkamispäivää(): EuropeanSchoolOfHelsinkiVuosiluokanSuoritus = this.copy(alkamispäivä = None)
}

case class SecondaryLowerVuosiluokanSuoritus(
  koulutusmoduuli: SecondaryLowerLuokkaAste,
  luokka: Option[String] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  suorituskieli: Koodistokoodiviite,
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondarylower")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("europeanschoolofhelsinkivuosiluokkasecondarylower", koodistoUri = "suorituksentyyppi"),
  jääLuokalle: Boolean = false,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  override val osasuoritukset: Option[List[SecondaryLowerOppiaineenSuoritus]] = None
) extends EuropeanSchoolOfHelsinkiVuosiluokanSuoritus {
  override def ilmanAlkamispäivää(): EuropeanSchoolOfHelsinkiVuosiluokanSuoritus = this.copy(alkamispäivä = None)
}

case class SecondaryUpperVuosiluokanSuoritus(
  koulutusmoduuli: SecondaryUpperLuokkaAste,
  luokka: Option[String] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  suorituskieli: Koodistokoodiviite,
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondaryupper")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("europeanschoolofhelsinkivuosiluokkasecondaryupper", koodistoUri = "suorituksentyyppi"),
  jääLuokalle: Boolean = false,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  override val osasuoritukset: Option[List[SecondaryUpperOppiaineenSuoritus]] = None
) extends EuropeanSchoolOfHelsinkiVuosiluokanSuoritus with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta {
  override def ilmanAlkamispäivää(): EuropeanSchoolOfHelsinkiVuosiluokanSuoritus = this.copy(alkamispäivä = None)
}

/******************************************************************************
 * PÄÄTASON SUORITUKSET - KOULUTUSMODUULIT
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiLuokkaAste extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton {
  @KoodistoUri("europeanschoolofhelsinkiluokkaaste")
  def tunniste: Koodistokoodiviite
  @KoodistoUri("europeanschoolofhelsinkicurriculum")
  def curriculum: Koodistokoodiviite
}

trait KoulutustyypinSisältäväEuropeanSchoolOfHelsinkiLuokkaAste extends EuropeanSchoolOfHelsinkiLuokkaAste with KoulutustyypinSisältäväKoulutusmoduuli {
  @KoodistoKoodiarvo("21")
  def koulutustyyppi: Option[Koodistokoodiviite]
}

case class NurseryLuokkaAste(
  @KoodistoKoodiarvo("N1")
  @KoodistoKoodiarvo("N2")
  tunniste: Koodistokoodiviite,
  curriculum: Koodistokoodiviite = Koodistokoodiviite("2023", "europeanschoolofhelsinkicurriculum")
) extends EuropeanSchoolOfHelsinkiLuokkaAste

object NurseryLuokkaAste {
  def apply(koodistokoodiarvo: String): NurseryLuokkaAste = {
    NurseryLuokkaAste(tunniste = Koodistokoodiviite(koodistokoodiarvo, "europeanschoolofhelsinkiluokkaaste"))
  }
}

case class PrimaryLuokkaAste(
  @KoodistoKoodiarvo("P1")
  @KoodistoKoodiarvo("P2")
  @KoodistoKoodiarvo("P3")
  @KoodistoKoodiarvo("P4")
  @KoodistoKoodiarvo("P5")
  tunniste: Koodistokoodiviite,
  curriculum: Koodistokoodiviite = Koodistokoodiviite("2023", "europeanschoolofhelsinkicurriculum"),
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends KoulutustyypinSisältäväEuropeanSchoolOfHelsinkiLuokkaAste


object PrimaryLuokkaAste {
  def apply(koodistokoodiarvo: String): PrimaryLuokkaAste = {
    PrimaryLuokkaAste(tunniste = Koodistokoodiviite(koodistokoodiarvo, "europeanschoolofhelsinkiluokkaaste"))
  }
}

case class SecondaryLowerLuokkaAste(
  @KoodistoKoodiarvo("S1")
  @KoodistoKoodiarvo("S2")
  @KoodistoKoodiarvo("S3")
  @KoodistoKoodiarvo("S4")
  @KoodistoKoodiarvo("S5")
  tunniste: Koodistokoodiviite,
  curriculum: Koodistokoodiviite = Koodistokoodiviite("2023", "europeanschoolofhelsinkicurriculum"),
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends KoulutustyypinSisältäväEuropeanSchoolOfHelsinkiLuokkaAste


object SecondaryLowerLuokkaAste {
  def apply(koodistokoodiarvo: String): SecondaryLowerLuokkaAste = {
    SecondaryLowerLuokkaAste(tunniste = Koodistokoodiviite(koodistokoodiarvo, "europeanschoolofhelsinkiluokkaaste"))
  }
}

case class SecondaryUpperLuokkaAste(
  @KoodistoKoodiarvo("S6")
  @KoodistoKoodiarvo("S7")
  tunniste: Koodistokoodiviite,
  curriculum: Koodistokoodiviite = Koodistokoodiviite("2023", "europeanschoolofhelsinkicurriculum"),
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends KoulutustyypinSisältäväEuropeanSchoolOfHelsinkiLuokkaAste


object SecondaryUpperLuokkaAste {
  def apply(koodistokoodiarvo: String): SecondaryUpperLuokkaAste = {
    SecondaryUpperLuokkaAste(tunniste = Koodistokoodiviite(koodistokoodiarvo, "europeanschoolofhelsinkiluokkaaste"))
  }
}

/******************************************************************************
 * OSASUORITUKSET
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiOsasuoritus extends Suoritus with Vahvistukseton {
  @KoodistoUri("suorituksentyyppi")
  def tyyppi: Koodistokoodiviite
}

trait EuropeanSchoolOfHelsinkiOsasuorituksenAlaosasuoritus extends Suoritus with Vahvistukseton {
  @KoodistoUri("suorituksentyyppi")
  def tyyppi: Koodistokoodiviite
}

trait EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus extends EuropeanSchoolOfHelsinkiOsasuoritus with Suorituskielellinen {
  @KoodistoUri("kieli")
  def suorituskieli: Koodistokoodiviite
}

trait PrimaryOsasuoritus extends EuropeanSchoolOfHelsinkiOsasuoritus

case class PrimaryLapsiOppimisalueenSuoritus(
  koulutusmoduuli: PrimaryLapsiOppimisalue,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritusprimarylapsi")
  @KoodistoUri("suorituksentyyppi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuoritusprimarylapsi", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[EuropeanSchoolOfHelsinkiOsasuoritusArviointi]] = None,
  override val osasuoritukset: Option[List[PrimaryLapsiOppimisalueenAlaosasuoritus]] = None
) extends PrimaryOsasuoritus

case class PrimaryOppimisalueenSuoritus(
  koulutusmoduuli: PrimarySuorituskielenVaativaOppimisalue,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritusprimary")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuoritusprimary", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[EuropeanSchoolOfHelsinkiOsasuoritusArviointi]] = None,
  suorituskieli: Koodistokoodiviite,
  override val osasuoritukset: Option[List[PrimaryOppimisalueenAlaosasuoritus]] = None
) extends PrimaryOsasuoritus with EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus

case class SecondaryLowerOppiaineenSuoritus(
  koulutusmoduuli: SecondaryOppiaine,
  arviointi: Option[List[SecondaryLowerArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritussecondarylower")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuoritussecondarylower", koodistoUri = "suorituksentyyppi"),
  suorituskieli: Koodistokoodiviite
) extends EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus

trait SecondaryUpperOppiaineenSuoritus extends EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus

@OnlyWhen("../../koulutusmoduuli/tunniste/koodiarvo", "S6")
case class SecondaryUpperOppiaineenSuoritusS6(
  koulutusmoduuli: SecondaryOppiaine,
  arviointi: Option[List[SecondaryNumericalMarkArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss6")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuorituss6", koodistoUri = "suorituksentyyppi"),
  suorituskieli: Koodistokoodiviite,
) extends SecondaryUpperOppiaineenSuoritus

@OnlyWhen("../../koulutusmoduuli/tunniste/koodiarvo", "S7")
case class SecondaryUpperOppiaineenSuoritusS7(
  koulutusmoduuli: SecondaryOppiaine,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss7")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuorituss7", koodistoUri = "suorituksentyyppi"),
  suorituskieli: Koodistokoodiviite,
  // TODO: TOR-1685: osasuoritusten osasuoritusten pakollisuus mukaan validaatioon ennenkuin voi merkitä päätason suoritusta vahvistetuksi. Luultavasti niin, että pitää olla
  // joko A + B, tai year mark.
  override val osasuoritukset: Option[List[S7OppiaineenAlaosasuoritus]] = None
) extends SecondaryUpperOppiaineenSuoritus with Arvioinniton

/******************************************************************************
 * OSASUORITUKSET - KOULUTUSMODUULIT
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiOsasuorituksenKoulutusmoduuli extends KoodistostaLöytyväKoulutusmoduuli

// TODO: uudeelleennimeä EuropeanSchoolOfHelsinkiAlaosasuorituksenKoulutusmoduuli
trait EuropeanSchoolOfHelsinkiOsasuorituksenOsasuorituksenKoulutusmoduuli extends KoodistostaLöytyväKoulutusmoduuli

trait EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli extends EuropeanSchoolOfHelsinkiOsasuorituksenKoulutusmoduuli with KoulutusmoduuliPakollinenLaajuusVuosiviikkotunneissa

trait PrimaryOppimisalue extends EuropeanSchoolOfHelsinkiOsasuorituksenKoulutusmoduuli

case class PrimaryLapsiOppimisalue(
  @KoodistoUri("europeanschoolofhelsinkilapsioppimisalue")
  tunniste: Koodistokoodiviite
) extends PrimaryOppimisalue

trait PrimarySuorituskielenVaativaOppimisalue extends PrimaryOppimisalue with EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli

case class PrimaryMuuOppimisalue(
  @KoodistoUri("europeanschoolofhelsinkimuuoppiaine")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa
) extends PrimarySuorituskielenVaativaOppimisalue with EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli

trait EuropeanSchoolOfHelsinkiKieliOppiaine extends Kieliaine {
  @KoodistoUri("kieli")
  override def kieli: Koodistokoodiviite
  override def description: LocalizedString = kieliaineDescription
}

case class PrimaryKieliOppimisalue(
  @KoodistoUri("europeanschoolofhelsinkikielioppiaine")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa,
  kieli: Koodistokoodiviite
) extends PrimarySuorituskielenVaativaOppimisalue with EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli with EuropeanSchoolOfHelsinkiKieliOppiaine

trait SecondaryOppiaine extends EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli

case class SecondaryMuuOppiaine(
  @KoodistoUri("europeanschoolofhelsinkimuuoppiaine")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa,
) extends SecondaryOppiaine

case class SecondaryKieliOppiaine(
  @KoodistoUri("europeanschoolofhelsinkikielioppiaine")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa,
  kieli: Koodistokoodiviite
) extends SecondaryOppiaine with EuropeanSchoolOfHelsinkiKieliOppiaine

/******************************************************************************
 * OSASUORITUKSET - ALAOSASUORITUKSET
 *****************************************************************************/

case class PrimaryLapsiOppimisalueenAlaosasuoritus(
  koulutusmoduuli: PrimaryLapsiAlaoppimisalue,
  arviointi: Option[List[PrimaryAlaoppimisalueArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkialaosasuoritusprimarylapsi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkialaosasuoritusprimarylapsi", koodistoUri = "suorituksentyyppi")
) extends EuropeanSchoolOfHelsinkiOsasuorituksenAlaosasuoritus

case class PrimaryOppimisalueenAlaosasuoritus(
  koulutusmoduuli: PrimaryAlaoppimisalue,
  arviointi: Option[List[PrimaryAlaoppimisalueArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkialaosasuoritusprimary")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkialaosasuoritusprimary", koodistoUri = "suorituksentyyppi")
) extends EuropeanSchoolOfHelsinkiOsasuorituksenAlaosasuoritus

case class S7OppiaineenAlaosasuoritus(
  koulutusmoduuli: S7OppiaineKomponentti,
  arviointi: Option[List[SecondaryS7PreliminaryMarkArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkialaosasuorituss7")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkialaosasuorituss7", koodistoUri = "suorituksentyyppi")
) extends EuropeanSchoolOfHelsinkiOsasuorituksenAlaosasuoritus

/******************************************************************************
 * OSASUORITUKSET - ALAOSASUORITUSTEN KOULUTUSMODUULIT
 *****************************************************************************/

case class PrimaryLapsiAlaoppimisalue(
  @KoodistoUri("europeanschoolofhelsinkiprimarylapsialaoppimisalue")
  tunniste: Koodistokoodiviite
) extends EuropeanSchoolOfHelsinkiOsasuorituksenOsasuorituksenKoulutusmoduuli

case class PrimaryAlaoppimisalue(
  @KoodistoUri("europeanschoolofhelsinkiprimaryalaoppimisalue")
  tunniste: Koodistokoodiviite
) extends EuropeanSchoolOfHelsinkiOsasuorituksenOsasuorituksenKoulutusmoduuli

trait S7OppiaineKomponentti extends EuropeanSchoolOfHelsinkiOsasuorituksenOsasuorituksenKoulutusmoduuli {
  @KoodistoUri("europeanschoolofhelsinkis7oppiaineenkomponentti")
  def tunniste: Koodistokoodiviite
}

case class S7OppiaineKomponenttiA(
  @KoodistoKoodiarvo("A")
  tunniste: Koodistokoodiviite
) extends S7OppiaineKomponentti

case class S7OppiaineKomponenttiB(
  @KoodistoKoodiarvo("B")
  tunniste: Koodistokoodiviite
) extends S7OppiaineKomponentti

case class S7OppiaineKomponenttiYearMark(
  @KoodistoKoodiarvo("yearmark")
  tunniste: Koodistokoodiviite
) extends S7OppiaineKomponentti

/******************************************************************************
 * OSASUORITUKSET - KOODISTOON PERUSTUVAT ARVIOINNIT
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiArviointi extends ArviointiPäivämäärällä {
  def arvosana: KoodiViite
  def päivä: LocalDate
  override def hyväksytty: Boolean = EuropeanSchoolOfHelsinkiArviointi.hyväksytty(arvosana)
}

trait EuropeanSchoolOfHelsinkiSanallinenArviointi extends EuropeanSchoolOfHelsinkiArviointi with SanallinenArviointi {
  def kuvaus: Option[LocalizedString]
}

trait EuropeanSchoolOfHelsinkiKoodistostaLöytyväArviointi extends EuropeanSchoolOfHelsinkiArviointi with KoodistostaLöytyväArviointi {
  def arvosana: Koodistokoodiviite
  def päivä: LocalDate
  override def hyväksytty: Boolean = EuropeanSchoolOfHelsinkiArviointi.hyväksytty(arvosana)
}

trait EuropeanSchoolOfHelsinkiKoodistostaLöytyväSanallinenArviointi extends EuropeanSchoolOfHelsinkiSanallinenArviointi with KoodistostaLöytyväArviointi {
  def arvosana: Koodistokoodiviite
  def päivä: LocalDate
  def kuvaus: Option[LocalizedString]
  override def hyväksytty: Boolean = EuropeanSchoolOfHelsinkiArviointi.hyväksytty(arvosana)
}

object EuropeanSchoolOfHelsinkiArviointi {
  def hyväksytty(arvosana: KoodiViite) = !hylätyt.contains(arvosana.koodiarvo) && !onHylättySynteettinenArviointi(arvosana.koodiarvo)
  private val hylätyt = List("F", "FX", "fail")

  private def onHylättySynteettinenArviointi(koodiarvo: String): Boolean = {
    tryFloat(koodiarvo) match {
      case Some(f) if f < 5.0 => true
      case _ => false
    }
  }

  def tryFloat(koodiarvo: String) = try { Some(koodiarvo.toFloat) } catch {
    case _: NumberFormatException => None
  }
}

// pass/fail arviointi sitä varten, että oppiainetasolle voidaan tallentaa sanallinen kuvaus, vaikka numeeriset arvosanat ovat alaosasuorituksissa
case class EuropeanSchoolOfHelsinkiOsasuoritusArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkiosasuoritus")
  arvosana: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate,
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends EuropeanSchoolOfHelsinkiKoodistostaLöytyväSanallinenArviointi

case class PrimaryAlaoppimisalueArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkiprimarymark")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends EuropeanSchoolOfHelsinkiKoodistostaLöytyväArviointi

trait SecondaryLowerArviointi extends EuropeanSchoolOfHelsinkiArviointi

@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S1")
@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S2")
@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S3")
case class SecondaryGradeArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkisecondarygrade")
  arvosana: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate,
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends SecondaryLowerArviointi with EuropeanSchoolOfHelsinkiKoodistostaLöytyväSanallinenArviointi

/******************************************************************************
 * OSASUORITUKSET - SYNTEETTISET ARVIOINNIT
 *****************************************************************************/

class NumericalMarkSynteettinenKoodisto extends SynteettinenKoodisto {
  val koodistoUri: String = "esh/numericalmark"
  val dokumentaatio: String = "0, 10, tai luku siltä väliltä tasan 1 desimaalilla, joka on 0 tai 5"
  def validoi(koodiarvo: String): Boolean = {
    koodiarvo == "0" || koodiarvo == "0.5" || koodiarvo == "10" || koodiarvo.matches("^[123456789]\\.[05]$")
  }
}

class S7PreliminaryMarkSynteettinenKoodisto extends SynteettinenKoodisto {
  val koodistoUri: String = "esh/s7preliminarymark"
  val dokumentaatio: String = "0, 10, tai luku siltä väliltä tasan 1 desimaalilla"
  def validoi(koodiarvo: String): Boolean = {
    koodiarvo == "0" || koodiarvo == "10" || koodiarvo.matches("^0\\.[123456789]$") || koodiarvo.matches("^[123456789]\\.\\d$")
  }
}

class S7FinalMarkSynteettinenKoodisto extends SynteettinenKoodisto {
  val koodistoUri: String = "esh/s7finalmark"
  val dokumentaatio: String = "10, tai luku 0.00-9.99 tasan 2 desimaalilla"
  def validoi(koodiarvo: String): Boolean = {
    koodiarvo == "10" || koodiarvo.matches("^\\d\\.\\d\\d$")
  }
}

trait EuropeanSchoolOfHelsinkiSynteettinenArviointi {
  def arvosana: SynteettinenKoodiviite
  def arvosanaKirjaimin: LocalizedString = arvosana.nimi.getOrElse(LocalizedString.unlocalized(arvosana.koodiarvo))
}

@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S4")
@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S5")
@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S6")
case class SecondaryNumericalMarkArviointi(
  @SynteettinenKoodistoUri("esh/numericalmark")
  arvosana: SynteettinenKoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate,
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends SecondaryLowerArviointi with EuropeanSchoolOfHelsinkiSynteettinenArviointi


case class SecondaryS7PreliminaryMarkArviointi(
  @SynteettinenKoodistoUri("esh/s7preliminarymark")
  arvosana: SynteettinenKoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate,
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends EuropeanSchoolOfHelsinkiSanallinenArviointi with EuropeanSchoolOfHelsinkiSynteettinenArviointi
