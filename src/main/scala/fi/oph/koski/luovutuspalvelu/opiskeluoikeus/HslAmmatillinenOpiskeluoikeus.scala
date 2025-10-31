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
  organisaatiohistoria: Option[List[HslOpiskeluoikeudenOrganisaatiohistoria]],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus]
) extends HslOpiskeluoikeus

object HslAmmatillinenOpiskeluoikeus {
  def apply(oo: AmmatillinenOpiskeluoikeus): HslAmmatillinenOpiskeluoikeus =
    HslAmmatillinenOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.getOrElse(""),
      oppilaitos = oo.oppilaitos,
      tila = HslOpiskeluoikeudenTila.apply(oo.tila),
      suoritukset = oo.suoritukset.map(HslAmmatillinenPäätasonSuoritus.fromLike),
      lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
      arvioituPäättymispäivä = oo.arvioituPäättymispäivä,
      aikaleima = oo.aikaleima,
      alkamispäivä = oo.alkamispäivä,
      versionumero = oo.versionumero,
      ostettu = oo.ostettu,
      päättymispäivä = oo.päättymispäivä,
      organisaatiohistoria = oo.organisaatiohistoria.map(x => x.map(HslOpiskeluoikeudenOrganisaatiohistoria.apply)),
      sisältyyOpiskeluoikeuteen = oo.sisältyyOpiskeluoikeuteen
    )
}

@Title("Ammatillisen opiskeluoikeuden päätason suoritus")
case class HslAmmatillinenPäätasonSuoritus(
  tyyppi: Koodistokoodiviite,
  osaamisenHankkimistavat: Option[List[HslOsaamisenHankkimistapajakso]],
  koulutussopimukset: Option[List[HslKoulutussopimusjakso]]
) extends HslPäätasonSuoritus

object HslAmmatillinenPäätasonSuoritus {
  def apply(s: AmmatillinenPäätasonSuoritus): HslAmmatillinenPäätasonSuoritus = s match {
    case x: OsaamisenHankkimistavallinen => HslAmmatillinenPäätasonSuoritus(
      tyyppi = s.tyyppi,
      koulutussopimukset = x.koulutussopimukset.map(y => y.map(HslKoulutussopimusjakso.apply)),
      osaamisenHankkimistavat = x.osaamisenHankkimistavat.map(y => y.map(HslOsaamisenHankkimistapajakso.apply)),
    )
    case _ => HslAmmatillinenPäätasonSuoritus(
      tyyppi = s.tyyppi,
      koulutussopimukset = s.koulutussopimukset.map(y => y.map(HslKoulutussopimusjakso.apply)),
      osaamisenHankkimistavat = None,
    )
  }
  def fromLike(s: AmmatillinenPäätasonsuoritusLike): HslAmmatillinenPäätasonSuoritus = s match {
    case a: AmmatillinenPäätasonSuoritus => apply(a)
    case _ =>
      val koulutussopimuksetOpt =
        s match {
          case ks: Koulutussopimuksellinen =>
            ks.koulutussopimukset.map(_.map(HslKoulutussopimusjakso.apply))
          case _ => None
        }

      val osaamisenHankkimistavatOpt =
        s match {
          case oh: OsaamisenHankkimistavallinen =>
            oh.osaamisenHankkimistavat.map(_.map(HslOsaamisenHankkimistapajakso.apply))
          case _ => None
        }
      HslAmmatillinenPäätasonSuoritus(
        tyyppi = s.tyyppi,
        osaamisenHankkimistavat = osaamisenHankkimistavatOpt,
        koulutussopimukset = koulutussopimuksetOpt
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
