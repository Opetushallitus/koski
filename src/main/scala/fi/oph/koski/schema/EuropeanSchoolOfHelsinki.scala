package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

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
  @KoodistoUri("opintojenrahoitus")
  @KoodistoKoodiarvo("6")
  override val opintojenRahoitus: Option[Koodistokoodiviite] = Some(Koodistokoodiviite("6", "opintojenrahoitus"))
) extends KoskiOpiskeluoikeusjakso

case class EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot(
  @Description("Rahoituksen laskennassa käytettävä tieto.")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None
) extends OpiskeluoikeudenLisätiedot with Ulkomaanjaksollinen with MaksuttomuusTieto

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
  jääLuokalle: Boolean = false,
//  override val osasuoritukset: Option[List[OppiaineenSuoritus]] = None
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
  //  override val osasuoritukset: Option[List[OppiaineenSuoritus]] = None
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
  //  override val osasuoritukset: Option[List[OppiaineenSuoritus]] = None
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
  //  override val osasuoritukset: Option[List[OppiaineenSuoritus]] = None
) extends EuropeanSchoolOfHelsinkiVuosiluokanSuoritus with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta


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
