package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoKoodiarvo}
import fi.oph.scalaschema.annotation.{DefaultValue, ReadFlattened, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Ammatillisten opintojen opiskeluoikeus")
case class SuoritetutTutkinnotAmmatillinenOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  @DefaultValue(false)
  suoritukset: List[SuoritetutTutkinnotAmmatillinenPäätasonSuoritus],
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends SuoritetutTutkinnotOpiskeluoikeus {

  override def withSuoritukset(suoritukset: List[Suoritus]): SuoritetutTutkinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : SuoritetutTutkinnotAmmatillinenPäätasonSuoritus => s }
    )
}

trait SuoritetutTutkinnotAmmatillinenPäätasonSuoritus extends Suoritus {
  def koulutusmoduuli: SuoritetutTutkinnotAmmatillisenSuorituksenKoulutusmoduuli
  def suoritustapa: Option[SuoritetutTutkinnotKoodistokoodiviite]
  def toimipiste: Option[Toimipiste]
  def suorituskieli: Option[SuoritetutTutkinnotKoodistokoodiviite]
  def osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]]
  def koulutussopimukset: Option[List[Koulutussopimusjakso]]
}

trait SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus extends SuoritetutTutkinnotAmmatillinenPäätasonSuoritus {
  def koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  def osaamisala: Option[List[SuoritetutTutkinnotOsaamisalajakso]]
  def järjestämismuodot: Option[List[Järjestämismuotojakso]]
  def työssäoppimisjaksot: Option[List[Työssäoppimisjakso]]
  def tutkintonimike: Option[List[SuoritetutTutkinnotKoodistokoodiviite]]
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
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  osaamisala: Option[List[SuoritetutTutkinnotOsaamisalajakso]],
  järjestämismuodot: Option[List[Järjestämismuotojakso]],
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]],
  tutkintonimike: Option[List[SuoritetutTutkinnotKoodistokoodiviite]],
) extends SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus

@Title("Ammatillisen tutkinnon osan tai osien suoritus")
case class SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus(
  koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli,
  suoritustapa: Option[SuoritetutTutkinnotKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: Option[SuoritetutTutkinnotKoodistokoodiviite],
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  osaamisala: Option[List[SuoritetutTutkinnotOsaamisalajakso]],
  järjestämismuodot: Option[List[Järjestämismuotojakso]],
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]],
  tutkintonimike: Option[List[SuoritetutTutkinnotKoodistokoodiviite]],
  toinenTutkintonimike: Option[Boolean],
  toinenOsaamisala: Option[Boolean],
  @Deprecated("Ei palauteta. Kenttä on näkyvissä skeemassa vain teknisistä syistä.")
  korotettuOpiskeluoikeusOid: Option[String],
) extends SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus

@Title("Muun ammatillisen koulutuksen suoritus")
case class SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus(
  koulutusmoduuli: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli,
  suoritustapa: Option[SuoritetutTutkinnotKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("muuammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: Option[SuoritetutTutkinnotKoodistokoodiviite],
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  täydentääTutkintoa: Option[SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli],
) extends SuoritetutTutkinnotAmmatillinenPäätasonSuoritus

trait SuoritetutTutkinnotAmmatillisenSuorituksenKoulutusmoduuli extends SuorituksenKoulutusmoduuli {
  def tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

case class SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli(
  tunniste: SuoritetutTutkinnotKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[SuoritetutTutkinnotKoodistokoodiviite]
) extends SuoritetutTutkinnotAmmatillisenSuorituksenKoulutusmoduuli

case class SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli(
  tunniste: SuoritetutTutkinnotKoodistokoodiviite,
  laajuus: Option[SuoritetutTutkinnotLaajuus],
  kuvaus: Option[schema.LocalizedString]
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