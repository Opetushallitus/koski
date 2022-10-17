package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

case class InternationalSchoolOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos] = None,
  koulutustoimija: Option[Koulutustoimija] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  tila: InternationalSchoolOpiskeluoikeudenTila,
  suoritukset: List[InternationalSchoolVuosiluokanSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.internationalschool.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.internationalschool,
  override val lisätiedot: Option[InternationalSchoolOpiskeluoikeudenLisätiedot] = None,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
}

object InternationalSchoolOpiskeluoikeus {
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

case class InternationalSchoolOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[InternationalSchoolOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class InternationalSchoolOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus. Mikäli kyseessä on kaksoitutkintoa suorittava opiskelija, jonka rahoituksen saa ammatillinen oppilaitos, tulee käyttää arvoa 6: Muuta kautta rahoitettu. Muussa tapauksessa käytetään arvoa 1: Valtionosuusrahoitteinen koulutus.")
  @KoodistoUri("opintojenrahoitus")
  @KoodistoKoodiarvo("1")
  @KoodistoKoodiarvo("6")
  override val opintojenRahoitus: Option[Koodistokoodiviite] = None
) extends KoskiLaajaOpiskeluoikeusjakso

case class InternationalSchoolOpiskeluoikeudenLisätiedot(
  erityisenKoulutustehtävänJaksot: Option[List[ErityisenKoulutustehtävänJakso]] = None,
  @Description("Rahoituksen laskennassa käytettävä tieto.")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None
) extends OpiskeluoikeudenLisätiedot with ErityisenKoulutustehtävänJaksollinen with Ulkomaanjaksollinen with MaksuttomuusTieto

trait InternationalSchoolVuosiluokanSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Arvioinniton with Suorituskielellinen {
  def tyyppi: Koodistokoodiviite
  @Description("Luokan tunniste, esimerkiksi 9C.")
  @Tooltip("Luokan tunniste, esimerkiksi 9C.")
  def luokka: Option[String]
}

case class PYPVuosiluokanSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: PYPLuokkaAste,
  luokka: Option[String] = None,
  @Tooltip("Vuosiluokan alkamispäivä")
  override val alkamispäivä: Option[LocalDate] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @Hidden
  suorituskieli: Koodistokoodiviite,
  @KoodistoKoodiarvo("internationalschoolpypvuosiluokka")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("internationalschoolpypvuosiluokka", koodistoUri = "suorituksentyyppi"),
  override val osasuoritukset: Option[List[PYPOppiaineenSuoritus]] = None
) extends InternationalSchoolVuosiluokanSuoritus

case class MYPVuosiluokanSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: MYPLuokkaAste,
  luokka: Option[String] = None,
  @Tooltip("Vuosiluokan alkamispäivä")
  override val alkamispäivä: Option[LocalDate] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @Hidden
  suorituskieli: Koodistokoodiviite,
  @KoodistoKoodiarvo("internationalschoolmypvuosiluokka")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("internationalschoolmypvuosiluokka", koodistoUri = "suorituksentyyppi"),
  override val osasuoritukset: Option[List[MYPOppiaineenSuoritus]] = None
) extends InternationalSchoolVuosiluokanSuoritus

case class DiplomaVuosiluokanSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: DiplomaLuokkaAste,
  luokka: Option[String] = None,
  @Tooltip("Vuosiluokan alkamispäivä")
  override val alkamispäivä: Option[LocalDate] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @Hidden
  suorituskieli: Koodistokoodiviite,
  @KoodistoKoodiarvo("internationalschooldiplomavuosiluokka")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("internationalschooldiplomavuosiluokka", koodistoUri = "suorituksentyyppi"),
  override val osasuoritukset: Option[List[DiplomaIBOppiaineenSuoritus]] = None
) extends InternationalSchoolVuosiluokanSuoritus with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta

trait InternationalSchoolLuokkaAste extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton {
  @KoodistoUri("internationalschoolluokkaaste")
  def tunniste: Koodistokoodiviite
}

case class PYPLuokkaAste(
  @KoodistoKoodiarvo("explorer")
  @KoodistoKoodiarvo("1")
  @KoodistoKoodiarvo("2")
  @KoodistoKoodiarvo("3")
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  tunniste: Koodistokoodiviite
) extends InternationalSchoolLuokkaAste

case class MYPLuokkaAste(
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoKoodiarvo("8")
  @KoodistoKoodiarvo("9")
  @KoodistoKoodiarvo("10")
  tunniste: Koodistokoodiviite
) extends InternationalSchoolLuokkaAste

trait DiplomaLuokkaAste extends InternationalSchoolLuokkaAste {
  @Discriminator
  @KoodistoUri("internationalschooldiplomatype")
  def diplomaType: Koodistokoodiviite
  @KoodistoKoodiarvo("11")
  @KoodistoKoodiarvo("12")
  def tunniste: Koodistokoodiviite
}

case class IBDiplomaLuokkaAste(
  @KoodistoKoodiarvo("ib")
  diplomaType: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ib", koodistoUri = "internationalschooldiplomatype"),
  tunniste: Koodistokoodiviite
) extends DiplomaLuokkaAste

case class ISHDiplomaLuokkaAste(
  @KoodistoKoodiarvo("ish")
  diplomaType: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ish", koodistoUri = "internationalschooldiplomatype"),
  tunniste: Koodistokoodiviite
) extends DiplomaLuokkaAste

case class MYPOppiaineenSuoritus(
  koulutusmoduuli: MYPOppiaine,
  arviointi: Option[List[MYPArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("internationalschoolmypoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "internationalschoolmypoppiaine", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with MahdollisestiSuorituskielellinen with Vahvistukseton

case class PYPOppiaineenSuoritus(
  koulutusmoduuli: PYPOppiaine,
  arviointi: Option[List[SanallinenInternationalSchoolOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("internationalschoolpypoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "internationalschoolpypoppiaine", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with MahdollisestiSuorituskielellinen with Vahvistukseton

trait DiplomaIBOppiaineenSuoritus extends OppiaineenSuoritus with MahdollisestiSuorituskielellinen with Vahvistukseton

case class DiplomaOppiaineenSuoritus(
  koulutusmoduuli: InternationalSchoolIBOppiaine,
  arviointi: Option[List[DiplomaArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("internationalschooldiplomaoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "internationalschooldiplomaoppiaine", koodistoUri = "suorituksentyyppi")
) extends DiplomaIBOppiaineenSuoritus

case class DiplomaCoreRequirementsOppiaineenSuoritus(
  koulutusmoduuli: DiplomaCoreRequirementsOppiaine,
  arviointi: Option[List[InternationalSchoolCoreRequirementsArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("internationalschoolcorerequirements")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "internationalschoolcorerequirements", koodistoUri = "suorituksentyyppi")
) extends DiplomaIBOppiaineenSuoritus

trait InternationalSchoolOppiaine extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton {
  @KoodistoUri("oppiaineetinternationalschool")
  def tunniste: Koodistokoodiviite
}

trait InternationalSchoolKieliOppiaine extends Kieliaine with MYPOppiaine with PYPOppiaine {
  @KoodistoUri("kielivalikoima")
  override def kieli: Koodistokoodiviite
  override def description: LocalizedString = kieliaineDescription
}

trait MYPOppiaine extends InternationalSchoolOppiaine
trait PYPOppiaine extends InternationalSchoolOppiaine

trait DiplomaArviointi extends KoodistostaLöytyväArviointi
trait MYPArviointi extends KoodistostaLöytyväArviointi
trait InternationalSchoolArviointi extends KoodistostaLöytyväArviointi {
  def arvosana: Koodistokoodiviite
  def päivä: Option[LocalDate]
  override def arviointipäivä: Option[LocalDate] = päivä
  override def arvioitsijat: Option[List[Arvioitsija]] = None
  override def hyväksytty: Boolean = InternationalSchoolArviointi.hyväksytty(arvosana)
}
object InternationalSchoolArviointi{
  def hyväksytty(arvosana: Koodistokoodiviite) = !hylätyt.contains(arvosana.koodiarvo)
  private val hylätyt = List("1", "2", "F", "fail")
}

case class SanallinenInternationalSchoolOppiaineenArviointi(
  @KoodistoUri("arviointiasteikkointernationalschool")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate] = None
) extends InternationalSchoolArviointi

trait InternationalSchoolNumeerinenOppiaineenArviointi extends InternationalSchoolArviointi with DiplomaArviointi{
  @KoodistoKoodiarvo("S")
  @KoodistoKoodiarvo("F")
  @KoodistoKoodiarvo("1")
  @KoodistoKoodiarvo("2")
  @KoodistoKoodiarvo("3")
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoUri("arviointiasteikkoib")
  def arvosana: Koodistokoodiviite
}

trait Predicted {
  @Tooltip("Jos valittu niin IBO ei ole vahvistanut arvosanaa")
  @DefaultValue(false)
  def predicted: Boolean
}

case class NumeerinenInternationalSchoolOppiaineenArviointi(
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate] = None
) extends InternationalSchoolNumeerinenOppiaineenArviointi with MYPArviointi

case class InternationalSchoolIBOppiaineenArviointi(
  predicted: Boolean = false,
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate] = None
) extends InternationalSchoolNumeerinenOppiaineenArviointi with Predicted with DiplomaArviointi

case class PassFailOppiaineenArviointi(
  @KoodistoKoodiarvo("pass")
  @KoodistoKoodiarvo("fail")
  @KoodistoUri("arviointiasteikkointernationalschool")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate] = None
) extends InternationalSchoolArviointi with DiplomaArviointi with MYPArviointi

case class InternationalSchoolCoreRequirementsArviointi(
  predicted: Boolean = false,
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate] = None
) extends InternationalSchoolArviointi with CoreRequirementsArvionti with Predicted {
  override def arvioitsijat: Option[List[Arvioitsija]] = None
}

case class LanguageAcquisition(
  @KoodistoKoodiarvo("LAC")
  tunniste: Koodistokoodiviite,
  @KoodistoKoodiarvo("ES")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("FR")
  @KoodistoKoodiarvo("EN")
  kieli: Koodistokoodiviite
) extends InternationalSchoolKieliOppiaine

case class LanguageAndLiterature(
  @KoodistoKoodiarvo("LL")
  tunniste: Koodistokoodiviite,
  @KoodistoKoodiarvo("EN")
  @KoodistoKoodiarvo("FI")
  kieli: Koodistokoodiviite
) extends InternationalSchoolKieliOppiaine

case class MYPOppiaineMuu(
  @KoodistoKoodiarvo("AD")
  @KoodistoKoodiarvo("DE")
  @KoodistoKoodiarvo("DR")
  @KoodistoKoodiarvo("EAL")
  @KoodistoKoodiarvo("EMA")
  @KoodistoKoodiarvo("ILS")
  @KoodistoKoodiarvo("IS")
  @KoodistoKoodiarvo("MA")
  @KoodistoKoodiarvo("ME")
  @KoodistoKoodiarvo("MU")
  @KoodistoKoodiarvo("PHE")
  @KoodistoKoodiarvo("PP")
  @KoodistoKoodiarvo("SCI")
  @KoodistoKoodiarvo("SMA")
  @KoodistoKoodiarvo("VA")
  @KoodistoKoodiarvo("INS")
  @KoodistoKoodiarvo("MF")
  tunniste: Koodistokoodiviite
) extends MYPOppiaine

case class PYPOppiaineMuu(
  @KoodistoKoodiarvo("DD")
  @KoodistoKoodiarvo("DE")
  @KoodistoKoodiarvo("DR")
  @KoodistoKoodiarvo("EAL")
  @KoodistoKoodiarvo("EMA")
  @KoodistoKoodiarvo("FR")
  @KoodistoKoodiarvo("FMT")
  @KoodistoKoodiarvo("ICT")
  @KoodistoKoodiarvo("ILS")
  @KoodistoKoodiarvo("IS")
  @KoodistoKoodiarvo("LA")
  @KoodistoKoodiarvo("LIB")
  @KoodistoKoodiarvo("MA")
  @KoodistoKoodiarvo("ME")
  @KoodistoKoodiarvo("MU")
  @KoodistoKoodiarvo("PE")
  @KoodistoKoodiarvo("PHE")
  @KoodistoKoodiarvo("SCI")
  @KoodistoKoodiarvo("SS")
  @KoodistoKoodiarvo("VA")
  @KoodistoKoodiarvo("ART")
  @KoodistoKoodiarvo("FFL")
  tunniste: Koodistokoodiviite
) extends PYPOppiaine

trait InternationalSchoolIBOppiaine extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton

case class FitnessAndWellBeing(
  @KoodistoKoodiarvo("HAWB")
  tunniste: Koodistokoodiviite,
  taso: Option[Koodistokoodiviite] = None
) extends InternationalSchoolOppiaine with InternationalSchoolIBOppiaine with IBTaso

case class InternationalSchoolMuuDiplomaOppiaine(
  @KoodistoKoodiarvo("F")
  @KoodistoKoodiarvo("HSCM")
  @KoodistoKoodiarvo("ITGS")
  @KoodistoKoodiarvo("MAA")
  @KoodistoKoodiarvo("MAI")
  @KoodistoKoodiarvo("INS")
  tunniste: Koodistokoodiviite,
  taso: Option[Koodistokoodiviite]
) extends InternationalSchoolOppiaine with InternationalSchoolIBOppiaine with IBTaso

case class MuuDiplomaOppiaine(
  @KoodistoKoodiarvo("BIO")
  @KoodistoKoodiarvo("CHE")
  @KoodistoKoodiarvo("ECO")
  @KoodistoKoodiarvo("ESS")
  @KoodistoKoodiarvo("HIS")
  @KoodistoKoodiarvo("MAT")
  @KoodistoKoodiarvo("MATST")
  @KoodistoKoodiarvo("PHY")
  @KoodistoKoodiarvo("PSY")
  @KoodistoKoodiarvo("VA")
  tunniste: Koodistokoodiviite,
  taso: Option[Koodistokoodiviite]
) extends InternationalSchoolIBOppiaine with IBOppiaine with IBTaso

case class KieliDiplomaOppiaine(
  tunniste: Koodistokoodiviite,
  @KoodistoKoodiarvo("EN")
  @KoodistoKoodiarvo("ES")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("FR")
  kieli: Koodistokoodiviite,
  taso: Option[Koodistokoodiviite]
) extends InternationalSchoolIBOppiaine with KieliOppiaineIB with IBTaso

case class DiplomaCoreRequirementsOppiaine(
  @KoodistoKoodiarvo("TOK")
  @KoodistoKoodiarvo("EE")
  @KoodistoKoodiarvo("CAS")
  @KoodistoUri("oppiaineetib")
  tunniste: Koodistokoodiviite
) extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton
