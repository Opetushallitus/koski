package fi.oph.koski.luovutuspalvelu.opiskeluoikeus

import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

case class HslOppija(
  henkilö: HslHenkilo,
  opiskeluoikeudet: Seq[HslOpiskeluoikeus],
  suostumuksenPaattymispaiva: Option[LocalDate]
)

case class HslHenkilo(
  oid: String,
  syntymäaika: Option[LocalDate]
)

object HslHenkilo {
  def fromOppija(oppija: OppijaHenkilö): HslHenkilo = HslHenkilo(
    oid = oppija.oid,
    syntymäaika = oppija.syntymäaika
  )
}

case class HslOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslPäätasonSuoritus],
  lisätiedot: Option[HslOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
)

case class HslOpiskeluoikeudenTila(
  opiskeluoikeusJaksot: Option[List[HslOpiskeluoikeusJakso]]
)

case class HslOpiskeluoikeusJakso(
  tila: Koodistokoodiviite,
  alku: LocalDate,
  opiskeluoikeusPäättynyt: Boolean
)

object HslOpiskeluoikeus {
  def apply(oo: Opiskeluoikeus): HslOpiskeluoikeus =
    HslOpiskeluoikeus(
      oo,
      oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
      oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply)
    )

  def apply(
    oo: Opiskeluoikeus,
    suoritukset: List[HslPäätasonSuoritus],
    lisätiedot: Option[HslOpiskeluoikeudenLisätiedot] = None,
  ): HslOpiskeluoikeus = {
    val opiskeluoikeusJaksot = oo.tila.opiskeluoikeusjaksot.map { j =>
      HslOpiskeluoikeusJakso(
        tila = j.tila,
        alku = j.alku,
        opiskeluoikeusPäättynyt = j.opiskeluoikeusPäättynyt
      )
    }

    new HslOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.getOrElse(""),
      oppilaitos = oo.oppilaitos,
      tila = HslOpiskeluoikeudenTila(opiskeluoikeusJaksot = Some(opiskeluoikeusJaksot)),
      suoritukset = suoritukset,
      lisätiedot = lisätiedot,
      arvioituPäättymispäivä = oo.arvioituPäättymispäivä
    )
  }
}

trait HslPäätasonSuoritus {
  def tyyppi: Koodistokoodiviite
}

@Title("Muu päätason suoritus")
case class HslDefaultPäätasonSuoritus(
  tyyppi: Koodistokoodiviite
) extends HslPäätasonSuoritus

object HslDefaultPäätasonSuoritus {
  def apply(s: Suoritus): HslDefaultPäätasonSuoritus = HslDefaultPäätasonSuoritus(
    tyyppi = s.tyyppi
  )
}

case class HslAmmatillinenPäätasonSuoritus(
  tyyppi: Koodistokoodiviite,
  osaamisenHankkimistavat: Option[List[HslOsaamisenHankkimistapajakso]],
  koulutussopimukset: Option[List[HslKoulutussopimusjakso]],
  järjestämismuodot: Option[List[HslJärjestämismuotojakso]]
) extends HslPäätasonSuoritus

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
    järjestämismuoto = new HslJärjestämismuoto(tunniste = j.järjestämismuoto.tunniste)
  )
}

case class HslJärjestämismuoto(
  tunniste: Koodistokoodiviite
)

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

@Title("Opiskeluoikeuden lisätiedot")
trait HslOpiskeluoikeudenLisätiedot {
  def osaAikaisuusjaksot: Option[List[OsaAikaisuusJakso]]
}

case class HslDefaultOpiskeluoikeudenLisätiedot(
  osaAikaisuusjaksot: Option[List[OsaAikaisuusJakso]] = None
) extends HslOpiskeluoikeudenLisätiedot

object HslDefaultOpiskeluoikeudenLisätiedot {
  def apply(l: OpiskeluoikeudenLisätiedot): HslDefaultOpiskeluoikeudenLisätiedot = l match {
    case x: OsaAikaisuusjaksollinen => HslDefaultOpiskeluoikeudenLisätiedot(osaAikaisuusjaksot = x.osaAikaisuusjaksot)
    case _ => HslDefaultOpiskeluoikeudenLisätiedot()
  }
}

case class HslKorkeakouluOpiskeluoikeudenLisätiedot(
  osaAikaisuusjaksot: Option[List[OsaAikaisuusJakso]] = None,
  @Title("Korkeakoulun opiskeluoikeuden tyyppi")
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  virtaOpiskeluoikeudenTyyppi: Option[Koodistokoodiviite],
  lukukausiIlmoittautuminen: Option[HslLukukausiIlmoittautuminen]
) extends HslOpiskeluoikeudenLisätiedot

object HslKorkeakouluOpiskeluoikeudenLisätiedot {
  def apply(lt: KorkeakoulunOpiskeluoikeudenLisätiedot): HslKorkeakouluOpiskeluoikeudenLisätiedot =
    new HslKorkeakouluOpiskeluoikeudenLisätiedot(
      virtaOpiskeluoikeudenTyyppi = lt.virtaOpiskeluoikeudenTyyppi,
      lukukausiIlmoittautuminen = lt.lukukausiIlmoittautuminen.map(HslLukukausiIlmoittautuminen.apply)
    )
}

case class HslLukukausiIlmoittautuminen(
  ilmoittautumisjaksot: List[HslLukukausiIlmoittautumisjakso]
)

object HslLukukausiIlmoittautuminen {
  def apply(l: Lukukausi_Ilmoittautuminen): HslLukukausiIlmoittautuminen =
    HslLukukausiIlmoittautuminen(
      ilmoittautumisjaksot = l.ilmoittautumisjaksot.map(HslLukukausiIlmoittautumisjakso.apply)
    )
}

case class HslLukukausiIlmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("virtalukukausiilmtila")
  tila: Koodistokoodiviite,
  ylioppilaskunnanJäsen: Option[Boolean] = None,
  @Title("Lukuvuosimaksu")
  maksetutLukuvuosimaksut: Option[HslLukuvuosiMaksu] = None
) extends Jakso

object HslLukukausiIlmoittautumisjakso {
  def apply(j: Lukukausi_Ilmoittautumisjakso): HslLukukausiIlmoittautumisjakso =
    HslLukukausiIlmoittautumisjakso(
      alku = j.alku,
      loppu = j.loppu,
      tila = j.tila,
      ylioppilaskunnanJäsen = j.ylioppilaskunnanJäsen,
      maksetutLukuvuosimaksut = j.maksetutLukuvuosimaksut.map(HslLukuvuosiMaksu.apply)
    )
}

case class HslLukuvuosiMaksu(
  @Title("Maksettu kokonaan")
  maksettu: Option[Boolean] = None,
  summa: Option[Int] = None,
  apuraha: Option[Int] = None
)

object HslLukuvuosiMaksu {
  def apply(m: Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu): HslLukuvuosiMaksu =
    HslLukuvuosiMaksu(
      maksettu = m.maksettu,
      summa = m.summa,
      apuraha = m.apuraha
    )
}

object HslAikuistenPerusopetuksenOpiskeluoikeus {
  def apply(oo: AikuistenPerusopetuksenOpiskeluoikeus): HslOpiskeluoikeus = HslOpiskeluoikeus(oo)
}

object HslAmmatillinenOpiskeluoikeus {
  def apply(oo: AmmatillinenOpiskeluoikeus): HslOpiskeluoikeus = HslOpiskeluoikeus(
    oo,
    oo.suoritukset.map(HslAmmatillinenPäätasonSuoritus.apply),
    oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply)
  )
}

object HslDiaOpiskeluoikeus {
  def apply(oo: DIAOpiskeluoikeus): HslOpiskeluoikeus = HslOpiskeluoikeus(oo)
}

object HslIBOpiskeluoikeus {
  def apply(oo: IBOpiskeluoikeus): HslOpiskeluoikeus = HslOpiskeluoikeus(oo)
}

object HslInternationalSchoolOpiskeluoikeus {
  def apply(oo: InternationalSchoolOpiskeluoikeus): HslOpiskeluoikeus = HslOpiskeluoikeus(oo)
}

object HslKorkeakoulunOpiskeluoikeus {
  def apply(oo: KorkeakoulunOpiskeluoikeus): HslOpiskeluoikeus =
    HslOpiskeluoikeus(
      oo,
      oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
      oo.lisätiedot.map(HslKorkeakouluOpiskeluoikeudenLisätiedot.apply)
    )
}

object HslLukionOpiskeluoikeus {
  def apply(oo: LukionOpiskeluoikeus): HslOpiskeluoikeus = HslOpiskeluoikeus(oo)
}

object HslLukioonValmistavaKoulutus {
  def apply(oo: LukioonValmistavanKoulutuksenOpiskeluoikeus): HslOpiskeluoikeus = HslOpiskeluoikeus(oo)
}

object HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus {
  def apply(oo: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus): HslOpiskeluoikeus = HslOpiskeluoikeus(oo)
}

object HslPerusopetuksenLisäopetuksenOpiskeluoikeus {
  def apply(oo: PerusopetuksenLisäopetuksenOpiskeluoikeus): HslOpiskeluoikeus = HslOpiskeluoikeus(oo)
}

object HslPerusopetuksenOpiskeluoikeus {
  def apply(oo: PerusopetuksenOpiskeluoikeus): HslOpiskeluoikeus = HslOpiskeluoikeus(oo)
}

object HslYlioppilastutkinnonOpiskeluoikeus {
  def apply(oo: YlioppilastutkinnonOpiskeluoikeus): HslOpiskeluoikeus = HslOpiskeluoikeus(oo)
}
