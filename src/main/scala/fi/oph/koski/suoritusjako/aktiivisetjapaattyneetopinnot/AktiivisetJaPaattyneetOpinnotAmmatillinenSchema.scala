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

@Title("Ammatillisten opintojen suoritus")
case class AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  suoritustapa: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  osaamisala: Option[List[AktiivisetJaPäättyneetOpinnotOsaamisalajakso]],
  tutkintonimike: Option[List[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite]],
  toinenTutkintonimike: Option[Boolean],
  toinenOsaamisala: Option[Boolean],
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodiViite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  kuvaus: Option[schema.LocalizedString],
) extends SuorituksenKoulutusmoduuli

case class OsaamisenHankkimistapa (
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  // TODO: TOR-1984: Jätetäänkö oppisopimus pois? Ei ole HSL-datassa
  // oppisopimus: Oppisopimus
)

case class OsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  osaamisenHankkimistapa: OsaamisenHankkimistapa
)

case class Koulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  // TODO: TOR-1984: Jätetäänkö pois, ei mukana HSL-datassa?
  // työssäoppimispaikka: Option[schema.LocalizedString],
  paikkakunta: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  maa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
)

case class Järjestämismuoto (
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
)

case class Järjestämismuotojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  järjestämismuoto: Järjestämismuoto
)
