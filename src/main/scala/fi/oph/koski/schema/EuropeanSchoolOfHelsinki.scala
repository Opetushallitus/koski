package fi.oph.koski.schema

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

// TODO: TOR-1685 Saatetaan tarvita
/*
object EuropeanSchoolOfHelsinkiOpiskeluoikeus {
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
}
*/

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
    with LuokalleJääntiTiedonSisältäväSuoritus {
  @Title("Koulutus")
  def tyyppi: Koodistokoodiviite
  @Description("Luokan tunniste, esimerkiksi 9C.")
  @Tooltip("Luokan tunniste, esimerkiksi 9C.")
  def luokka: Option[String]
  def jääLuokalle: Boolean
  @Tooltip("Vuosiluokan alkamispäivä")
  def alkamispäivä: Option[LocalDate]
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
  jääLuokalle: Boolean = false
) extends EuropeanSchoolOfHelsinkiVuosiluokanSuoritus

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
  override val osasuoritukset: Option[List[PrimaryOsasuoritus]] = None
) extends EuropeanSchoolOfHelsinkiVuosiluokanSuoritus

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
  override val osasuoritukset: Option[List[SecondaryLowerOppiaineenSuoritus]] = None
) extends EuropeanSchoolOfHelsinkiVuosiluokanSuoritus

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
  override val osasuoritukset: Option[List[SecondaryUpperOppiaineenSuoritus]] = None
) extends EuropeanSchoolOfHelsinkiVuosiluokanSuoritus with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta

/******************************************************************************
 * PÄÄTASON SUORITUKSET - KOULUTUSMODUULIT
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiLuokkaAste extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton {
  @KoodistoUri("europeanschoolofhelsinkiluokkaaste")
  def tunniste: Koodistokoodiviite
  @KoodistoUri("europeanschoolofhelsinkicurriculum")
  def curriculum: Koodistokoodiviite
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
  curriculum: Koodistokoodiviite = Koodistokoodiviite("2023", "europeanschoolofhelsinkicurriculum")
) extends EuropeanSchoolOfHelsinkiLuokkaAste


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
  curriculum: Koodistokoodiviite = Koodistokoodiviite("2023", "europeanschoolofhelsinkicurriculum")
) extends EuropeanSchoolOfHelsinkiLuokkaAste


object SecondaryLowerLuokkaAste {
  def apply(koodistokoodiarvo: String): SecondaryLowerLuokkaAste = {
    SecondaryLowerLuokkaAste(tunniste = Koodistokoodiviite(koodistokoodiarvo, "europeanschoolofhelsinkiluokkaaste"))
  }
}

case class SecondaryUpperLuokkaAste(
  @KoodistoKoodiarvo("S6")
  @KoodistoKoodiarvo("S7")
  tunniste: Koodistokoodiviite,
  curriculum: Koodistokoodiviite = Koodistokoodiviite("2023", "europeanschoolofhelsinkicurriculum")
) extends EuropeanSchoolOfHelsinkiLuokkaAste


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

trait EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus extends EuropeanSchoolOfHelsinkiOsasuoritus with Suorituskielellinen {
  @KoodistoUri("kieli")
  def suorituskieli: Koodistokoodiviite
}

trait PrimaryOsasuoritus extends EuropeanSchoolOfHelsinkiOsasuoritus

case class PrimaryLapsiOppimisalueenOsasuoritus(
  koulutusmoduuli: PrimaryLapsiOppimisalue,
  arviointi: Option[List[PrimaryArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritusprimarylapsi")
  @KoodistoUri("suorituksentyyppi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuoritusprimarylapsi", koodistoUri = "suorituksentyyppi")
) extends PrimaryOsasuoritus

case class PrimaryOppimisalueenOsasuoritus(
  koulutusmoduuli: PrimarySuorituskielenVaativaOppimisalue,
  arviointi: Option[List[PrimaryArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritusprimary")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuoritusprimary", koodistoUri = "suorituksentyyppi"),
  suorituskieli: Koodistokoodiviite
) extends PrimaryOsasuoritus with EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus

case class SecondaryLowerOppiaineenSuoritus(
  koulutusmoduuli: SecondaryLowerOppiaine,
  arviointi: Option[List[SecondaryArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritussecondarylower")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuoritussecondarylower", koodistoUri = "suorituksentyyppi"),
  suorituskieli: Koodistokoodiviite
) extends EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus

case class SecondaryUpperOppiaineenSuoritus(
  koulutusmoduuli: SecondaryUpperOppiaine,
  arviointi: Option[List[SecondaryArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritussecondaryupper")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "europeanschoolofhelsinkiosasuoritussecondaryupper", koodistoUri = "suorituksentyyppi"),
  suorituskieli: Koodistokoodiviite
) extends EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus

/******************************************************************************
 * OSASUORITUKSET - KOULUTUSMODUULIT
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiOsasuorituksenKoulutusmoduuli extends KoodistostaLöytyväKoulutusmoduuli

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

trait SecondaryLowerOppiaine extends EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli

case class SecondaryLowerMuuOppiaine(
  @KoodistoUri("europeanschoolofhelsinkimuuoppiaine")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa,
) extends SecondaryLowerOppiaine

case class SecondaryLowerKieliOppiaine(
  @KoodistoUri("europeanschoolofhelsinkikielioppiaine")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa,
  kieli: Koodistokoodiviite
) extends SecondaryLowerOppiaine with EuropeanSchoolOfHelsinkiKieliOppiaine

trait SecondaryUpperOppiaine extends EuropeanSchoolOfHelsinkiOsasuorituksenOppiainemainenKoulutusmoduuli

case class SecondaryUpperMuuOppiaine(
  @KoodistoUri("europeanschoolofhelsinkimuuoppiaine")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa,
) extends SecondaryUpperOppiaine

case class SecondaryUpperKieliOppiaine(
  @KoodistoUri("europeanschoolofhelsinkikielioppiaine")
  tunniste: Koodistokoodiviite,
  laajuus: LaajuusVuosiviikkotunneissa,
  kieli: Koodistokoodiviite
) extends SecondaryUpperOppiaine with EuropeanSchoolOfHelsinkiKieliOppiaine

/******************************************************************************
 * OSASUORITUKSET - KOODISTOON PERUSTUVAT ARVIOINNIT
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiArviointi extends SanallinenArviointi with ArviointiPäivämäärällä {
  def arvosana: KoodiViite
  def päivä: LocalDate
  def kuvaus: Option[LocalizedString]
  override def hyväksytty: Boolean = EuropeanSchoolOfHelsinkiArviointi.hyväksytty(arvosana)
}

trait EuropeanSchoolOfHelsinkiKoodistostaLöytyväArviointi extends EuropeanSchoolOfHelsinkiArviointi with KoodistostaLöytyväArviointi {
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

  def tryInt(koodiarvo: String) = try { Some(koodiarvo.toInt) } catch {
    case _: NumberFormatException => None
  }
}

case class PrimaryArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkiprimarymark")
  arvosana: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate,
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends EuropeanSchoolOfHelsinkiKoodistostaLöytyväArviointi

trait SecondaryArviointi extends EuropeanSchoolOfHelsinkiArviointi

// TODO: TOR-1685: oikeat arvioinnit ylemmille luokka-asteille
//@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S1")
//@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S2")
//@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S3")
case class SecondaryGradeArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkisecondarygrade")
  arvosana: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate,
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends SecondaryArviointi with EuropeanSchoolOfHelsinkiKoodistostaLöytyväArviointi

/******************************************************************************
 * OSASUORITUKSET - SYNTEETTISET ARVIOINNIT
 *****************************************************************************/

trait EuropeanSchoolOfHelsinkiSynteettinenArviointi {
  def arvosana: EuropeanSchoolOfHelsinkiArvosanaKoodiviite
  def arvosanaKirjaimin: LocalizedString = arvosana.nimi.getOrElse(LocalizedString.unlocalized(arvosana.koodiarvo))
}

// TODO: TOR-1685: oikeat arvioinnit ylemmille luokka-asteille
//@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S4")
//@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S5")
//@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S6")
//case class SecondaryNumericalMarkArviointi(
//  @KoodistoUri("esh/numericalmark")
//  arvosana: EuropeanSchoolOfHelsinkiNumericalMarkKoodiviite,
//  kuvaus: Option[LocalizedString],
//  päivä: LocalDate,
//  arvioitsijat: Option[List[Arvioitsija]] = None
//) extends SecondaryArviointi with EuropeanSchoolOfHelsinkiKoodistostaLöytyväArviointi // with EuropeanSchoolOfHelsinkiSynteettinenArviointi
//
//
//@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S7")
//case class SecondaryS7PreliminaryMarkArviointi(
//  @KoodistoUri("esh/s7preliminarymark")
//  arvosana: EuropeanSchoolOfHelsinkiS7PreliminaryMarkKoodiviite,
//  kuvaus: Option[LocalizedString],
//  päivä: LocalDate,
//  arvioitsijat: Option[List[Arvioitsija]] = None
//) extends SecondaryArviointi with EuropeanSchoolOfHelsinkiKoodistostaLöytyväArviointi // with EuropeanSchoolOfHelsinkiSynteettinenArviointi
//
//@OnlyWhen("../../../../koulutusmoduuli/tunniste/koodiarvo", "S7")
//case class SecondaryS7FinalMarkArviointi(
//  @KoodistoUri("esh/s7finalmark")
//  arvosana: EuropeanSchoolOfHelsinkiS7FinalMarkKoodiviite,
//  kuvaus: Option[LocalizedString],
//  päivä: LocalDate,
//  arvioitsijat: Option[List[Arvioitsija]] = None
//) extends SecondaryArviointi with EuropeanSchoolOfHelsinkiKoodistostaLöytyväArviointi // with EuropeanSchoolOfHelsinkiSynteettinenArviointi

trait EuropeanSchoolOfHelsinkiArvosanaKoodiviite extends SynteettinenKoodiviite {
  def kelpaaArvosanaksi: Boolean
}

case class EuropeanSchoolOfHelsinkiNumericalMarkKoodiviite(
  koodiarvo: String,
  koodistoUri: Option[String] = Some("esh/numericalmark")
) extends EuropeanSchoolOfHelsinkiArvosanaKoodiviite {
  def nimi: Option[LocalizedString] = lyhytNimi
  def lyhytNimi: Option[LocalizedString] = Some(LocalizedString.unlocalized(koodiarvo))

  // 0 - 10 , 0:ssa ja 10:ssä ei saa olla desimaaleja, muissa pitää olla tasan 1, joka on joko .0 tai .5
  def kelpaaArvosanaksi: Boolean = {
    EuropeanSchoolOfHelsinkiArviointi.tryFloat(koodiarvo) match {
      case Some(_) => koodiarvo.split(".").toList.map(EuropeanSchoolOfHelsinkiArviointi.tryInt) match {
        case List(Some(a)) if a == 0 || a == 10 => true
        case List(Some(a), Some(b)) if a == 0 && b == 0 => false
        case List(Some(a), Some(b)) if a >= 0 && a < 10 && (b == 0 || b == 5) => true
        case _ => false
      }
      case _ => false
    }
  }
}

case class EuropeanSchoolOfHelsinkiS7PreliminaryMarkKoodiviite(
  koodiarvo: String,
  koodistoUri: Option[String] = Some("esh/s7preliminarymark")
) extends EuropeanSchoolOfHelsinkiArvosanaKoodiviite {
  def nimi: Option[LocalizedString] = lyhytNimi
  def lyhytNimi: Option[LocalizedString] = Some(LocalizedString.unlocalized(koodiarvo))

  // 0 - 10 , 0:ssa ja 10:ssä ei saa olla desimaaleja, muissa pitää olla tasan 1
  def kelpaaArvosanaksi: Boolean = {
    EuropeanSchoolOfHelsinkiArviointi.tryFloat(koodiarvo) match {
      case Some(_) => koodiarvo.split(".").toList.map(EuropeanSchoolOfHelsinkiArviointi.tryInt) match {
        case List(Some(a)) if a == 0 || a == 10 => true
        case List(Some(a), Some(b)) if a == 0 && b == 0 => false
        case List(Some(a), Some(b)) if a >= 0 && a < 10 && b >= 0 && b < 10 => true
        case _ => false
      }
      case _ => false
    }
  }
}

case class EuropeanSchoolOfHelsinkiS7FinalMarkKoodiviite(
  koodiarvo: String,
  koodistoUri: Option[String] = Some("esh/s7finalmark")

) extends EuropeanSchoolOfHelsinkiArvosanaKoodiviite {
  def nimi: Option[LocalizedString] = lyhytNimi
  def lyhytNimi: Option[LocalizedString] = Some(LocalizedString.unlocalized(koodiarvo))

  // 0.00 - 10 , 10:ssä ei saa olla desimaaleja, muissa pitää olla tasan 2
  def kelpaaArvosanaksi: Boolean = {
    EuropeanSchoolOfHelsinkiArviointi.tryFloat(koodiarvo) match {
      case Some(_) => koodiarvo.split(".").toList match {
        case List(a) if a == "10" => true
        case List(_, b) if b.length != 2 => false
        case List(a, b) => List(a, b).map(EuropeanSchoolOfHelsinkiArviointi.tryInt) match {
          case List(Some(a), Some(b)) if a >= 0 && a < 10 && b >= 0 && b < 100 => true
          case _ => false
        }
        case _ => false
      }
      case _ => false
    }
  }
}
