package fi.oph.koski.schema

import fi.oph.koski.koskiuser.Rooli

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

import scala.util.Try

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
  suoritukset: List[EuropeanSchoolOfHelsinkiPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.europeanschoolofhelsinki,
  override val lisätiedot: Option[EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot] = None,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None

  def vuosiluokkasuorituksetJärjestyksessä: List[EuropeanSchoolOfHelsinkiPäätasonSuoritus] =
    suoritukset.sortBy(_.suorituksenJärjestysKriteeriAlustaLoppuun)

}

object EuropeanSchoolOfHelsinkiOpiskeluoikeus {
  def vuosiluokallaMahdollisestiMaksuttomuusLisätieto(
    koulutusmoduulinUri: String,
    koulutusmoduulinKoodiarvo: String
  ): Boolean = (koulutusmoduulinUri, koulutusmoduulinKoodiarvo) match {
    case ("europeanschoolofhelsinkiluokkaaste", "S5") => true
    case ("europeanschoolofhelsinkiluokkaaste", "S6") => true
    case ("europeanschoolofhelsinkiluokkaaste", "S7") => true
    case _ => false
  }
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
  @Deprecated("Helsingin eurooppalaisesta koulusta annetun lain 15 §:ssä säädetään, että opetus kyseisessä oppilaitoksessa on osittain maksutonta.\nTästä johtuen tietoa osittaisesta maksuttomuudesta ei merkitä valtakunnallisista opinto- ja tutkintorekistereistä annetussa laissa säädetyllä tavalla Koski-järjestelmään.")
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  @Deprecated("Helsingin eurooppalaisesta koulusta annetun lain 15 §:ssä säädetään, että opetus kyseisessä oppilaitoksessa on osittain maksutonta.\nTästä johtuen tietoa osittaisesta maksuttomuudesta ei merkitä valtakunnallisista opinto- ja tutkintorekistereistä annetussa laissa säädetyllä tavalla Koski-järjestelmään.")
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None
) extends OpiskeluoikeudenLisätiedot with Ulkomaanjaksollinen with MaksuttomuusTieto

/******************************************************************************
 * PÄÄTASON SUORITUKSET
 *****************************************************************************/


trait EuropeanSchoolOfHelsinkiPäätasonSuoritus
  extends KoskeenTallennettavaPäätasonSuoritus
    with Toimipisteellinen
    with Arvioinniton
    with SisältääTodistuksellaNäkyvätLisätiedot
{
  def koulutusmoduuli: EuropeanSchoolOfHelsinkiPäätasonKoulutusmoduuli
  @Title("Koulutus")
  def tyyppi: Koodistokoodiviite
  @Description("Luokan tunniste, esimerkiksi 9C.")
  @Tooltip("Luokan tunniste, esimerkiksi 9C.")
  def luokka: Option[String]
  @Tooltip("Todistuksella näkyvät lisätiedot. Esimerkiksi vuosiluokan sanallinen yleisarviointi.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  def todistuksellaNäkyvätLisätiedot: Option[LocalizedString]

  def suorituksenJärjestysKriteeriAlustaLoppuun: (Int, Boolean) =
    (
      tyypinMukainenJärjestysKriteeri + Try(koulutusmoduuli.tunniste.koodiarvo.slice(1, 2).toInt).getOrElse(20),
      !valmis
    )

  protected def tyypinMukainenJärjestysKriteeri: Int
}

trait EuropeanSchoolOfHelsinkiVuosiluokanSuoritus
  extends EuropeanSchoolOfHelsinkiPäätasonSuoritus
  with LuokalleJääntiTiedonSisältäväSuoritus
  with SisältääTodistuksellaNäkyvätLisätiedot
{
  @Title("Luokka-aste")
  def koulutusmoduuli: EuropeanSchoolOfHelsinkiLuokkaAste
  def jääLuokalle: Boolean
  @Tooltip("Vuosiluokan alkamispäivä")
  def alkamispäivä: Option[LocalDate]
  def ilmanAlkamispäivää(): EuropeanSchoolOfHelsinkiVuosiluokanSuoritus
}

trait OppivelvollisuudenSuorittamiseenKelpaavaESHVuosiluokanSuoritus extends EuropeanSchoolOfHelsinkiVuosiluokanSuoritus

case class NurseryVuosiluokanSuoritus(
  koulutusmoduuli: NurseryLuokkaAste,
  luokka: Option[String] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkanursery")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("europeanschoolofhelsinkivuosiluokkanursery", koodistoUri = "suorituksentyyppi"),
  jääLuokalle: Boolean = false,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None
) extends OppivelvollisuudenSuorittamiseenKelpaavaESHVuosiluokanSuoritus {
  override def ilmanAlkamispäivää(): EuropeanSchoolOfHelsinkiVuosiluokanSuoritus = this.copy(alkamispäivä = None)

  override protected def tyypinMukainenJärjestysKriteeri: Int = 100
}

case class PrimaryVuosiluokanSuoritus(
  koulutusmoduuli: PrimaryLuokkaAste,
  luokka: Option[String] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkaprimary")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("europeanschoolofhelsinkivuosiluokkaprimary", koodistoUri = "suorituksentyyppi"),
  jääLuokalle: Boolean = false,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  override val osasuoritukset: Option[List[PrimaryOsasuoritus]] = None
) extends OppivelvollisuudenSuorittamiseenKelpaavaESHVuosiluokanSuoritus {
  override def ilmanAlkamispäivää(): EuropeanSchoolOfHelsinkiVuosiluokanSuoritus = this.copy(alkamispäivä = None)

  override protected def tyypinMukainenJärjestysKriteeri: Int = 200
}

case class SecondaryLowerVuosiluokanSuoritus(
  koulutusmoduuli: SecondaryLowerLuokkaAste,
  luokka: Option[String] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondarylower")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("europeanschoolofhelsinkivuosiluokkasecondarylower", koodistoUri = "suorituksentyyppi"),
  jääLuokalle: Boolean = false,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  override val osasuoritukset: Option[List[SecondaryLowerOppiaineenSuoritus]] = None
) extends OppivelvollisuudenSuorittamiseenKelpaavaESHVuosiluokanSuoritus {
  override def ilmanAlkamispäivää(): EuropeanSchoolOfHelsinkiVuosiluokanSuoritus = this.copy(alkamispäivä = None)

  override protected def tyypinMukainenJärjestysKriteeri: Int = 300
}

case class SecondaryUpperVuosiluokanSuoritus(
  koulutusmoduuli: SecondaryUpperLuokkaAste,
  luokka: Option[String] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondaryupper")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("europeanschoolofhelsinkivuosiluokkasecondaryupper", koodistoUri = "suorituksentyyppi"),
  jääLuokalle: Boolean = false,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  override val osasuoritukset: Option[List[SecondaryUpperOppiaineenSuoritus]] = None
) extends OppivelvollisuudenSuorittamiseenKelpaavaESHVuosiluokanSuoritus with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta {
  override def ilmanAlkamispäivää(): EuropeanSchoolOfHelsinkiVuosiluokanSuoritus = this.copy(alkamispäivä = None)

  override protected def tyypinMukainenJärjestysKriteeri: Int = 400
}

/******************************************************************************
 * PÄÄTASON SUORITUKSET - KOULUTUSMODUULIT
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiPäätasonKoulutusmoduuli extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton {
  @KoodistoUri("europeanschoolofhelsinkicurriculum")
  def curriculum: Koodistokoodiviite
}

trait KoulutustyypinSisältäväEuropeanSchoolOfHelsinkiPäätasonKoulutusmoduuli extends EuropeanSchoolOfHelsinkiPäätasonKoulutusmoduuli with KoulutustyypinSisältäväKoulutusmoduuli {
  @KoodistoKoodiarvo("21")
  def koulutustyyppi: Option[Koodistokoodiviite]
}

trait EuropeanSchoolOfHelsinkiLuokkaAste extends EuropeanSchoolOfHelsinkiPäätasonKoulutusmoduuli {
  @KoodistoUri("europeanschoolofhelsinkiluokkaaste")
  def tunniste: Koodistokoodiviite
}

trait KoulutustyypinSisältäväEuropeanSchoolOfHelsinkiLuokkaAste extends EuropeanSchoolOfHelsinkiLuokkaAste with KoulutustyypinSisältäväEuropeanSchoolOfHelsinkiPäätasonKoulutusmoduuli

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

trait YksilöllistettyEuropeanSchoolOfHelsinkiOsasuoritus extends EuropeanSchoolOfHelsinkiOsasuoritus with Yksilöllistettävä {
  @DefaultValue(false)
  @Description("Tieto siitä, onko oppimäärä yksilöllistetty (true/false).")
  @Tooltip("Onko oppilas opiskellut yksilöllisen oppimäärän.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  def yksilöllistettyOppimäärä: Boolean
}

trait EuropeanSchoolOfHelsinkiOsasuorituksenAlaosasuoritus extends Suoritus with Vahvistukseton {
  @KoodistoUri("suorituksentyyppi")
  def tyyppi: Koodistokoodiviite
}

trait EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus extends EuropeanSchoolOfHelsinkiOsasuoritus with Suorituskielellinen {
  @KoodistoUri("kieli")
  def suorituskieli: Koodistokoodiviite
}

trait PrimaryOsasuoritus extends YksilöllistettyEuropeanSchoolOfHelsinkiOsasuoritus

case class PrimaryLapsiOppimisalueenSuoritus(
  @Title("Oppimisalue")
  koulutusmoduuli: PrimaryLapsiOppimisalue,
  yksilöllistettyOppimäärä: Boolean = false,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritusprimarylapsi")
  @KoodistoUri("suorituksentyyppi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuoritusprimarylapsi", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[EuropeanSchoolOfHelsinkiOsasuoritusArviointi]] = None,
  override val osasuoritukset: Option[List[PrimaryLapsiOppimisalueenAlaosasuoritus]] = None
) extends PrimaryOsasuoritus

case class PrimaryOppimisalueenSuoritus(
  @Title("Oppimisalue")
  koulutusmoduuli: PrimarySuorituskielenVaativaOppimisalue,
  yksilöllistettyOppimäärä: Boolean = false,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritusprimary")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuoritusprimary", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[EuropeanSchoolOfHelsinkiOsasuoritusArviointi]] = None,
  suorituskieli: Koodistokoodiviite,
  override val osasuoritukset: Option[List[PrimaryOppimisalueenAlaosasuoritus]] = None
) extends PrimaryOsasuoritus with EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus

case class SecondaryLowerOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: SecondaryOppiaine,
  yksilöllistettyOppimäärä: Boolean = false,
  arviointi: Option[List[SecondaryLowerArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritussecondarylower")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuoritussecondarylower", koodistoUri = "suorituksentyyppi"),
  suorituskieli: Koodistokoodiviite
) extends EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus with YksilöllistettyEuropeanSchoolOfHelsinkiOsasuoritus

trait SecondaryUpperOppiaineenSuoritus extends EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus with YksilöllistettyEuropeanSchoolOfHelsinkiOsasuoritus

@OnlyWhen("../../koulutusmoduuli/tunniste/koodiarvo", "S6")
case class SecondaryUpperOppiaineenSuoritusS6(
  @Title("Oppiaine")
  koulutusmoduuli: SecondaryOppiaine,
  yksilöllistettyOppimäärä: Boolean = false,
  arviointi: Option[List[SecondaryNumericalMarkArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss6")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuorituss6", koodistoUri = "suorituksentyyppi"),
  suorituskieli: Koodistokoodiviite,
) extends SecondaryUpperOppiaineenSuoritus

@OnlyWhen("../../koulutusmoduuli/tunniste/koodiarvo", "S7")
case class SecondaryUpperOppiaineenSuoritusS7(
  @Title("Oppiaine")
  koulutusmoduuli: SecondaryOppiaine,
  yksilöllistettyOppimäärä: Boolean = false,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss7")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuorituss7", koodistoUri = "suorituksentyyppi"),
  suorituskieli: Koodistokoodiviite,
  override val osasuoritukset: Option[List[S7OppiaineenAlaosasuoritus]] = None
) extends SecondaryUpperOppiaineenSuoritus with Arvioinniton with Välisuoritus

/******************************************************************************
 * OSASUORITUKSET - KOULUTUSMODUULIT
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiOsasuorituksenKoulutusmoduuli extends KoodistostaLöytyväKoulutusmoduuli

trait EuropeanSchoolOfHelsinkiAlaosasuorituksenKoulutusmoduuli extends KoodistostaLöytyväKoulutusmoduuli

trait EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli extends EuropeanSchoolOfHelsinkiOsasuorituksenKoulutusmoduuli with KoulutusmoduuliPakollinenLaajuusVuosiviikkotunneissa

trait PrimaryOppimisalue extends EuropeanSchoolOfHelsinkiOsasuorituksenKoulutusmoduuli

case class PrimaryLapsiOppimisalue(
  @KoodistoUri("europeanschoolofhelsinkilapsioppimisalue")
  tunniste: Koodistokoodiviite
) extends PrimaryOppimisalue

trait PrimarySuorituskielenVaativaOppimisalue extends PrimaryOppimisalue with EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli

trait SecondaryOppiaine extends EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli

case class EuropeanSchoolOfHelsinkiMuuOppiaine(
  @KoodistoUri("europeanschoolofhelsinkimuuoppiaine")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa
) extends PrimarySuorituskielenVaativaOppimisalue with SecondaryOppiaine with EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli

trait EuropeanSchoolOfHelsinkiKieliaine extends Kieliaine with KoodistostaLöytyväKoulutusmoduuli {
  @KoodistoUri("europeanschoolofhelsinkikielioppiaine")
  override def tunniste: Koodistokoodiviite
  @KoodistoUri("kieli")
  override def kieli: Koodistokoodiviite
  override def description: LocalizedString = kieliaineDescription
}

@NotWhen("tunniste/koodiarvo", List("LA", "GRC"))
case class EuropeanSchoolOfHelsinkiKielioppiaine(
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa,
  kieli: Koodistokoodiviite
) extends PrimarySuorituskielenVaativaOppimisalue with SecondaryOppiaine with EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli with EuropeanSchoolOfHelsinkiKieliaine
// Frontend tarvitsee tämän, jotta oneOfPrototype resolvaa oikein
@OnlyWhen("tunniste/koodiarvo", "LA")
case class EuropeanSchoolOfHelsinkiKielioppiaineLatin(
  @KoodistoKoodiarvo("LA")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa,
  @KoodistoKoodiarvo("LA")
  kieli: Koodistokoodiviite
) extends PrimarySuorituskielenVaativaOppimisalue with SecondaryOppiaine with EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli with EuropeanSchoolOfHelsinkiKieliaine
// Frontend tarvitsee tämän, jotta oneOfPrototype resolvaa oikein
@OnlyWhen("tunniste/koodiarvo", "GRC")
case class EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek(
  @KoodistoKoodiarvo("GRC")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa,
  @KoodistoKoodiarvo("EL")
  kieli: Koodistokoodiviite
) extends PrimarySuorituskielenVaativaOppimisalue with SecondaryOppiaine with EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli with EuropeanSchoolOfHelsinkiKieliaine

/******************************************************************************
 * OSASUORITUKSET - ALAOSASUORITUKSET
 *****************************************************************************/

case class PrimaryLapsiOppimisalueenAlaosasuoritus(
  @Title("Alaosasuoritus")
  koulutusmoduuli: PrimaryLapsiAlaoppimisalue,
  arviointi: Option[List[PrimaryAlaoppimisalueArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkialaosasuoritusprimarylapsi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkialaosasuoritusprimarylapsi", koodistoUri = "suorituksentyyppi")
) extends EuropeanSchoolOfHelsinkiOsasuorituksenAlaosasuoritus

case class PrimaryOppimisalueenAlaosasuoritus(
  @Title("Alaosasuoritus")
  koulutusmoduuli: PrimaryAlaoppimisalue,
  arviointi: Option[List[PrimaryAlaoppimisalueArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkialaosasuoritusprimary")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkialaosasuoritusprimary", koodistoUri = "suorituksentyyppi")
) extends EuropeanSchoolOfHelsinkiOsasuorituksenAlaosasuoritus

case class S7OppiaineenAlaosasuoritus(
  @Title("Arviointikomponentti")
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
) extends EuropeanSchoolOfHelsinkiAlaosasuorituksenKoulutusmoduuli

case class PrimaryAlaoppimisalue(
  @KoodistoUri("europeanschoolofhelsinkiprimaryalaoppimisalue")
  tunniste: Koodistokoodiviite
) extends EuropeanSchoolOfHelsinkiAlaosasuorituksenKoulutusmoduuli

case class S7OppiaineKomponentti(
  @KoodistoUri("europeanschoolofhelsinkis7oppiaineenkomponentti")
  tunniste: Koodistokoodiviite
) extends EuropeanSchoolOfHelsinkiAlaosasuorituksenKoulutusmoduuli

/******************************************************************************
 * OSASUORITUKSET - KOODISTOON PERUSTUVAT ARVIOINNIT
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiArviointi extends Arviointi {
  def arvosana: KoodiViite
  def päivä: Option[LocalDate]
  override def hyväksytty: Boolean = EuropeanSchoolOfHelsinkiArviointi.hyväksytty(arvosana)

  def arviointipäivä: Option[LocalDate] = päivä
}

trait EuropeanSchoolOfHelsinkiSanallinenArviointi extends EuropeanSchoolOfHelsinkiArviointi with SanallinenArviointi {
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @Title("Oppilaan vahvuudet ja kehittymisalueet jatkossa")
  def kuvaus: Option[LocalizedString]
}

trait EuropeanSchoolOfHelsinkiKoodistostaLöytyväArviointi extends EuropeanSchoolOfHelsinkiArviointi with KoodistostaLöytyväArviointi {
  def arvosana: Koodistokoodiviite
  def päivä: Option[LocalDate]
  override def hyväksytty: Boolean = EuropeanSchoolOfHelsinkiArviointi.hyväksytty(arvosana)
}

trait EuropeanSchoolOfHelsinkiKoodistostaLöytyväSanallinenArviointi extends EuropeanSchoolOfHelsinkiSanallinenArviointi with KoodistostaLöytyväArviointi {
  def arvosana: Koodistokoodiviite
  def päivä: Option[LocalDate]
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
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends EuropeanSchoolOfHelsinkiKoodistostaLöytyväSanallinenArviointi

case class PrimaryAlaoppimisalueArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkiprimarymark")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate],
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
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends SecondaryLowerArviointi with EuropeanSchoolOfHelsinkiKoodistostaLöytyväSanallinenArviointi

@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S4")
@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S5")
@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S6")
case class SecondaryNumericalMarkArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkinumericalmark")
  arvosana: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends SecondaryLowerArviointi with EuropeanSchoolOfHelsinkiKoodistostaLöytyväSanallinenArviointi

case class SecondaryS7PreliminaryMarkArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark")
  arvosana: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends EuropeanSchoolOfHelsinkiKoodistostaLöytyväSanallinenArviointi
