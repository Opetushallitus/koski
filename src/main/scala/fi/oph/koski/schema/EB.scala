package fi.oph.koski.schema

import fi.oph.koski.koskiuser.Rooli

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

@Title("EB-tutkinnon opiskeluoikeus")
case class EBOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos] = None,
  koulutustoimija: Option[Koulutustoimija] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  tila: EBOpiskeluoikeudenTila,
  @MaxItems(1)
  suoritukset: List[EBTutkinnonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.ebtutkinto,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def lisätiedot: Option[OpiskeluoikeudenLisätiedot] = None
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
}

case class EBOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[EBOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class EBOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("eronnut")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("mitatoity")
  @KoodistoKoodiarvo("valmistunut")
  tila: Koodistokoodiviite,
) extends KoskiOpiskeluoikeusjakso

@Title("EB-tutkinnon suoritus")
case class EBTutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: EBTutkinto,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @Title("Koulutus")
  @KoodistoKoodiarvo("ebtutkinto2")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ebtutkinto2", "suorituksentyyppi"),
  @Tooltip("Todistuksella näkyvät lisätiedot. Esimerkiksi vuosiluokan sanallinen yleisarviointi.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @MinValue(0)
  @MaxValue(100)
  @Scale(2)
  yleisarvosana: Option[Double] = None,
  override val osasuoritukset: Option[List[EBTutkinnonOsasuoritus]] = None
) extends KoskeenTallennettavaPäätasonSuoritus
  with Toimipisteellinen
  with Arvioinniton
  with SisältääTodistuksellaNäkyvätLisätiedot

@Title("EB-tutkinto")
case class EBTutkinto(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("301104")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("301104", "koulutus"),
  @KoodistoKoodiarvo("21")
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  curriculum: Koodistokoodiviite = Koodistokoodiviite("2023", "europeanschoolofhelsinkicurriculum")
) extends KoulutustyypinSisältäväEuropeanSchoolOfHelsinkiPäätasonKoulutusmoduuli

@Title("EB-tutkinnon osasuoritus")
case class EBTutkinnonOsasuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: SecondaryOppiaine,
  @KoodistoKoodiarvo("ebtutkinnonosasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ebtutkinnonosasuoritus", koodistoUri = "suorituksentyyppi"),
  suorituskieli: Koodistokoodiviite,
  override val osasuoritukset: Option[List[EBOppiaineenAlaosasuoritus]] = None
) extends EuropeanSchoolOfHelsinkiSuorituskielellinenOsasuoritus with Arvioinniton with Välisuoritus

@Title("EB-oppiaineen alaosasuoritus")
case class EBOppiaineenAlaosasuoritus(
  @Title("Arviointikomponentti")
  koulutusmoduuli: EBOppiaineKomponentti,
  arviointi: Option[List[EBArviointi]] = None,
  @KoodistoKoodiarvo("ebtutkinnonalaosasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ebtutkinnonalaosasuoritus", koodistoUri = "suorituksentyyppi")
) extends EuropeanSchoolOfHelsinkiOsasuorituksenAlaosasuoritus

@Title("EB-oppiainekomponentti")
case class EBOppiaineKomponentti(
  @KoodistoUri("ebtutkinnonoppiaineenkomponentti")
  @KoodistoKoodiarvo("Final")
  @KoodistoKoodiarvo("Oral")
  @KoodistoKoodiarvo("Written")
  tunniste: Koodistokoodiviite
) extends EuropeanSchoolOfHelsinkiAlaosasuorituksenKoulutusmoduuli

trait EBArviointi extends EuropeanSchoolOfHelsinkiArviointi with KoodistostaLöytyväArviointi

case class EBTutkintoFinalMarkArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkifinalmark")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends EBArviointi
