package fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Title}

import java.time.LocalDate

@Title("Ammatillisten opintojen opiskeluoikeus")
case class AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila,
  suoritukset: List[AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus],
  lisätiedot: Option[AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotOpiskeluoikeus {

  override def withSuoritukset(suoritukset: List[Suoritus]): AktiivisetJaPäättyneetOpinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus => s }
    )
  override def withoutSisältyyOpiskeluoikeuteen: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus = this.copy(sisältyyOpiskeluoikeuteen = None)
}

case class AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot(
  osaAikaisuusjaksot: Option[List[AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso]],
) extends AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenLisätiedot

@Title("Osa-aikaisuusjakso")
case class AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  osaAikaisuus: Int
)

trait AktiivisetJaPäättyneetOpinnotOsaamisenHankkimistavallinen extends Suoritus {
  def osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]]
}

trait AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus extends Suoritus {
  def koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli
  def koulutussopimukset: Option[List[Koulutussopimusjakso]]
}

trait AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus
  extends AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus
    with AktiivisetJaPäättyneetOpinnotOsaamisenHankkimistavallinen
{
  def tutkintonimike: Option[List[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite]]
  def osaamisala: Option[List[AktiivisetJaPäättyneetOpinnotOsaamisalajakso]]
  def osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]]
  def suoritustapa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

@Title("Ammatillisen tutkinnon osa/osia")
case class AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus,
  vahvistus: Option[Vahvistus],
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  toimipiste: Option[Toimipiste],
  suoritustapa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  osaamisala: Option[List[AktiivisetJaPäättyneetOpinnotOsaamisalajakso]],
  tutkintonimike: Option[List[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite]],
  toinenTutkintonimike: Boolean,
  toinenOsaamisala: Boolean,
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus

@Title("Ammatillisen tutkinnon suoritus")
case class AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus,
  vahvistus: Option[Vahvistus],
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  toimipiste: Option[Toimipiste],
  suoritustapa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  osaamisala: Option[List[AktiivisetJaPäättyneetOpinnotOsaamisalajakso]],
  tutkintonimike: Option[List[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite]],
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus

@Title("Muun ammatillisen koulutuksen suoritus")
case class AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus,
  täydentääTutkintoa: Option[AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus],
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  @KoodistoKoodiarvo("muuammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus with AktiivisetJaPäättyneetOpinnotOsaamisenHankkimistavallinen

@Title("TELMA-koulutuksen suoritus")
case class AktiivisetJaPäättyneetOpinnotTelmaKoulutuksenSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotTelmaKoulutus,
  vahvistus: Option[Vahvistus],
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  toimipiste: Option[Toimipiste],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  @KoodistoKoodiarvo("telma")
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus

@Title("Tutkinnon osaa pienemmistä kokonaisuuksista koostuva suoritus")
case class AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  @KoodistoKoodiarvo("tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus")
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus with AktiivisetJaPäättyneetOpinnotOsaamisenHankkimistavallinen


@Title("VALMA-koulutuksen suoritus")
case class AktiivisetJaPäättyneetOpinnotValmaKoulutuksenSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotValmaKoulutus,
  vahvistus: Option[Vahvistus],
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  toimipiste: Option[Toimipiste],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  @KoodistoKoodiarvo("valma")
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus

trait AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli extends SuorituksenKoulutusmoduuli

case class AktiivisetJaPäättyneetOpinnotAmmatillinenTutkintoKoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
) extends AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli

trait AktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus extends AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli

@Title("Ammatilliseen tehtävään valmistava koulutus")
case class AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  kuvaus: Option[schema.LocalizedString],
) extends AktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus

@Title("Paikallinen muu ammatillinen koulutus")
case class AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotPaikallinenKoodi,
  kuvaus: schema.LocalizedString
) extends AktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus

case class AktiivisetJaPäättyneetOpinnotTelmaKoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
) extends AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli

case class AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotPaikallinenKoodi,
  kuvaus: schema.LocalizedString,
) extends AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli

case class AktiivisetJaPäättyneetOpinnotValmaKoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
) extends AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli

case class OsaamisenHankkimistapa (
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
)

case class OsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  osaamisenHankkimistapa: OsaamisenHankkimistapa
)

case class Koulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  paikkakunta: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  maa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
)
