package fi.oph.koski.suoritetuttutkinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoKoodiarvo}
import fi.oph.koski.util.DateOrdering.localDateOptionOrdering
import fi.oph.scalaschema.annotation.{Description, ReadFlattened, Title}

import java.time.LocalDate

@Title("Ammatillisten opintojen opiskeluoikeus")
case class SuoritetutTutkinnotAmmatillinenOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  suoritukset: List[SuoritetutTutkinnotAmmatillinenPäätasonSuoritus],
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
) extends SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus {

  override def withSuoritukset(suoritukset: List[Suoritus]): SuoritetutTutkinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : SuoritetutTutkinnotAmmatillinenPäätasonSuoritus => s }
    )
  override def withoutSisältyyOpiskeluoikeuteen: SuoritetutTutkinnotOpiskeluoikeus = this.copy(sisältyyOpiskeluoikeuteen = None)
}

trait SuoritetutTutkinnotAmmatillinenPäätasonSuoritus extends Suoritus {
  def koulutusmoduuli: SuoritetutTutkinnotAmmatillisenSuorituksenKoulutusmoduuli
  def suoritustapa: Option[SuoritetutTutkinnotKoodistokoodiviite]
  def toimipiste: Option[Toimipiste]
  def suorituskieli: Option[SuoritetutTutkinnotKoodistokoodiviite]
}

trait SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus extends SuoritetutTutkinnotAmmatillinenPäätasonSuoritus {
  def koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  @Description("Osaamisaloista palautetaan vain uusin Koskeen merkitty")
  def osaamisala: Option[List[SuoritetutTutkinnotOsaamisalajakso]]
  def tutkintonimike: Option[List[SuoritetutTutkinnotKoodistokoodiviite]]

  def withVainUusinOsaamisala: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus
}

@Title("Ammatillisen tutkinnon suoritus")
case class SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus(
  koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli,
  suoritustapa: Option[SuoritetutTutkinnotKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: Option[SuoritetutTutkinnotKoodistokoodiviite],
  osaamisala: Option[List[SuoritetutTutkinnotOsaamisalajakso]],
  tutkintonimike: Option[List[SuoritetutTutkinnotKoodistokoodiviite]],
) extends SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus {
  override def withVainUusinOsaamisala: SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus = {
    val uusinOsaamisala = osaamisala.map(_.maxBy(_.alku)(localDateOptionOrdering))
    this.copy(osaamisala = uusinOsaamisala.map(oa => List(oa)))
  }
}

@Title("Ammatillisen tutkinnon osan tai osien suoritus")
case class SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus(
  koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli,
  suoritustapa: Option[SuoritetutTutkinnotKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: Option[SuoritetutTutkinnotKoodistokoodiviite],
  @Description("Palautetaan vain, jos toinenOsaamisala on true")
  osaamisala: Option[List[SuoritetutTutkinnotOsaamisalajakso]],
  @Description("Palautetaan vain, jos toinenTukintonimike on true")
  tutkintonimike: Option[List[SuoritetutTutkinnotKoodistokoodiviite]],
  toinenTutkintonimike: Option[Boolean],
  toinenOsaamisala: Option[Boolean],
  @Deprecated("Ei palauteta. Kenttä on näkyvissä skeemassa vain teknisistä syistä.")
  korotettuOpiskeluoikeusOid: Option[String],
) extends SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus {
  override def withVainUusinOsaamisala: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus = {
    val uusinOsaamisala = osaamisala.map(_.maxBy(_.alku)(localDateOptionOrdering))
    this.copy(osaamisala = uusinOsaamisala.map(oa => List(oa)))
  }
}

@Title("Muun ammatillisen koulutuksen suoritus")
case class SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus(
  koulutusmoduuli: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli,
  suoritustapa: Option[SuoritetutTutkinnotKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("muuammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: Option[SuoritetutTutkinnotKoodistokoodiviite],
) extends SuoritetutTutkinnotAmmatillinenPäätasonSuoritus

trait SuoritetutTutkinnotAmmatillisenSuorituksenKoulutusmoduuli extends SuorituksenKoulutusmoduuli {
  def tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

case class SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli(
  tunniste: SuoritetutTutkinnotKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[SuoritetutTutkinnotKoodistokoodiviite],
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite]
) extends SuoritetutTutkinnotAmmatillisenSuorituksenKoulutusmoduuli

case class SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli(
  tunniste: SuoritetutTutkinnotKoodistokoodiviite,
  laajuus: Option[SuoritetutTutkinnotLaajuus],
  kuvaus: Option[schema.LocalizedString],
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite]
) extends SuoritetutTutkinnotAmmatillisenSuorituksenKoulutusmoduuli

trait OsaamisenHankkimistapa {
  def tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

case class OsaamisenHankkimistapaIlmanLisätietoja (
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
) extends OsaamisenHankkimistapa

case class OppisopimuksellinenOsaamisenHankkimistapa (
  tunniste: SuoritetutTutkinnotKoodistokoodiviite,
  oppisopimus: Oppisopimus
) extends OsaamisenHankkimistapa

case class OsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  osaamisenHankkimistapa: OsaamisenHankkimistapa
)

case class Työssäoppimisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[schema.LocalizedString],
  paikkakunta: SuoritetutTutkinnotKoodistokoodiviite,
  maa: SuoritetutTutkinnotKoodistokoodiviite,
  laajuus: SuoritetutTutkinnotLaajuus
)

case class Koulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[schema.LocalizedString],
  paikkakunta: SuoritetutTutkinnotKoodistokoodiviite,
  maa: SuoritetutTutkinnotKoodistokoodiviite
)

case class Järjestämismuoto (
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
)

case class Järjestämismuotojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  järjestämismuoto: Järjestämismuoto
)

case class Oppisopimus(
  työnantaja: Yritys,
  oppisopimuksenPurkaminen: Option[OppisopimuksenPurkaminen]
)

case class OppisopimuksenPurkaminen(
  päivä: LocalDate,
  purettuKoeajalla: Boolean
)

case class Yritys(
  nimi: schema.LocalizedString,
  yTunnus: String
)

@ReadFlattened
case class SuoritetutTutkinnotOsaamisalajakso(
  osaamisala: SuoritetutTutkinnotKoodistokoodiviite,
  alku: Option[LocalDate] = None,
  loppu: Option[LocalDate] = None
)
