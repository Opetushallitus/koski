package fi.oph.koski.luovutuspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("Ammatillinen opiskeluoikeus")
case class HslAmmatillinenOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslAmmatillinenPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate],
  aikaleima: Option[LocalDateTime],
  alkamispäivä: Option[LocalDate],
  versionumero: Option[Int],
  ostettu: Boolean,
  päättymispäivä: Option[LocalDate],
) extends HslOpiskeluoikeus

object HslAmmatillinenOpiskeluoikeus {
  def apply(oo: AmmatillinenOpiskeluoikeus): HslAmmatillinenOpiskeluoikeus =
    HslAmmatillinenOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.getOrElse(""),
      oppilaitos = oo.oppilaitos,
      tila = HslOpiskeluoikeudenTila.apply(oo.tila),
      suoritukset = oo.suoritukset.map(HslAmmatillinenPäätasonSuoritus.apply),
      lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
      arvioituPäättymispäivä = oo.arvioituPäättymispäivä,
      aikaleima = oo.aikaleima,
      alkamispäivä = oo.alkamispäivä,
      versionumero = oo.versionumero,
      ostettu = oo.ostettu,
      päättymispäivä = oo.päättymispäivä,
    )
}


@Title("Ammatillisen opiskeluoikeuden päätason suoritus")
case class HslAmmatillinenPäätasonSuoritus(
  tyyppi: Koodistokoodiviite,
  osaamisenHankkimistavat: Option[List[HslOsaamisenHankkimistapajakso]],
  koulutussopimukset: Option[List[HslKoulutussopimusjakso]],
  järjestämismuodot: Option[List[HslJärjestämismuotojakso]]
) extends HslPäätasonSuoritus

object HslAmmatillinenPäätasonSuoritus {
  def apply(s: AmmatillinenPäätasonSuoritus): HslAmmatillinenPäätasonSuoritus = s match {
    case x: OsaamisenHankkimistavallinen with Järjestämismuodollinen => HslAmmatillinenPäätasonSuoritus(
      tyyppi = s.tyyppi,
      koulutussopimukset = x.koulutussopimukset.map(y => y.map(HslKoulutussopimusjakso.apply)),
      osaamisenHankkimistavat = x.osaamisenHankkimistavat.map(y => y.map(HslOsaamisenHankkimistapajakso.apply)),
      järjestämismuodot = x.järjestämismuodot.map(y => y.map(HslJärjestämismuotojakso.apply))
    )
    case x: OsaamisenHankkimistavallinen => HslAmmatillinenPäätasonSuoritus(
      tyyppi = s.tyyppi,
      koulutussopimukset = x.koulutussopimukset.map(y => y.map(HslKoulutussopimusjakso.apply)),
      osaamisenHankkimistavat = x.osaamisenHankkimistavat.map(y => y.map(HslOsaamisenHankkimistapajakso.apply)),
      järjestämismuodot = None
    )
    case x: Järjestämismuodollinen => HslAmmatillinenPäätasonSuoritus(
      tyyppi = s.tyyppi,
      koulutussopimukset = x.koulutussopimukset.map(y => y.map(HslKoulutussopimusjakso.apply)),
      osaamisenHankkimistavat = None,
      järjestämismuodot = x.järjestämismuodot.map(y => y.map(HslJärjestämismuotojakso.apply))
    )
  }
}

case class HslOsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  osaamisenHankkimistapa: HslOsaamisenHankkimistapa
)

object HslOsaamisenHankkimistapajakso {
  def apply(o: OsaamisenHankkimistapajakso): HslOsaamisenHankkimistapajakso = new HslOsaamisenHankkimistapajakso(o.alku, o.loppu, HslOsaamisenHankkimistapa(o.osaamisenHankkimistapa.tunniste))
}

case class HslOsaamisenHankkimistapa(
  tunniste: Koodistokoodiviite
)

case class HslKoulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  paikkakunta: Koodistokoodiviite,
  maa: Koodistokoodiviite,
)

object HslKoulutussopimusjakso {
  def apply(k: Koulutussopimusjakso): HslKoulutussopimusjakso = new HslKoulutussopimusjakso(k.alku, k.loppu, k.paikkakunta, k.maa)
}

case class HslJärjestämismuotojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  järjestämismuoto: HslJärjestämismuoto
)

object HslJärjestämismuotojakso {
  def apply(j: Järjestämismuotojakso): HslJärjestämismuotojakso = new HslJärjestämismuotojakso(
    alku = j.alku,
    loppu = j.loppu,
    järjestämismuoto = HslJärjestämismuoto(tunniste = j.järjestämismuoto.tunniste)
  )
}

case class HslJärjestämismuoto(
  tunniste: Koodistokoodiviite
)
