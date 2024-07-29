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

trait HslOpiskeluoikeus {
  def tyyppi: Koodistokoodiviite
  def oid: String
  def oppilaitos: Option[Oppilaitos]
  def tila: HslOpiskeluoikeudenTila
  def suoritukset: List[HslPäätasonSuoritus]
  def lisätiedot: Option[HslOpiskeluoikeudenLisätiedot]
  def arvioituPäättymispäivä: Option[LocalDate]
}

object HslOpiskeluoikeus {
  def apply(oo: Opiskeluoikeus): Option[HslOpiskeluoikeus] =
    (oo match {
      case o: AikuistenPerusopetuksenOpiskeluoikeus => Some(HslAikuistenPerusopetuksenOpiskeluoikeus(o))
      case o: AmmatillinenOpiskeluoikeus => Some(HslAmmatillinenOpiskeluoikeus(o))
      case o: DIAOpiskeluoikeus => Some(HslDiaOpiskeluoikeus(o))
      case o: IBOpiskeluoikeus => Some(HslIBOpiskeluoikeus(o))
      case o: InternationalSchoolOpiskeluoikeus => Some(HslInternationalSchoolOpiskeluoikeus(o))
      case o: KorkeakoulunOpiskeluoikeus => Some(HslKorkeakoulunOpiskeluoikeus(o))
      case o: LukionOpiskeluoikeus => Some(HslLukionOpiskeluoikeus(o))
      case o: LukioonValmistavanKoulutuksenOpiskeluoikeus => Some(HslLukioonValmistavanKoulutuksenOpiskeluoikeus(o))
      case o: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus => Some(HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(o))
      case o: PerusopetuksenLisäopetuksenOpiskeluoikeus => Some(HslPerusopetuksenLisäopetuksenOpiskeluoikeus(o))
      case o: PerusopetuksenOpiskeluoikeus => Some(HslPerusopetuksenOpiskeluoikeus(o))
      case o: YlioppilastutkinnonOpiskeluoikeus => Some(HslYlioppilastutkinnonOpiskeluoikeus(o))
      case _ => None
    })
}

case class HslOpiskeluoikeudenTila(
  opiskeluoikeusJaksot: Option[List[HslOpiskeluoikeusJakso]]
)

object HslOpiskeluoikeudenTila {
  def apply (t: OpiskeluoikeudenTila): HslOpiskeluoikeudenTila = HslOpiskeluoikeudenTila(Some(t.opiskeluoikeusjaksot.map(j => HslOpiskeluoikeusJakso(j.tila, j.alku, j.opiskeluoikeusPäättynyt))))
}

case class HslOpiskeluoikeusJakso(
  tila: Koodistokoodiviite,
  alku: LocalDate,
  opiskeluoikeusPäättynyt: Boolean
)

trait HslPäätasonSuoritus {
  def tyyppi: Koodistokoodiviite
}

@Title("Päätason suoritus")
case class HslDefaultPäätasonSuoritus(
  tyyppi: Koodistokoodiviite
) extends HslPäätasonSuoritus

object HslDefaultPäätasonSuoritus {
  def apply(s: Suoritus): HslDefaultPäätasonSuoritus = HslDefaultPäätasonSuoritus(
    tyyppi = s.tyyppi
  )
}

@Title("Ammatillisen opiskeluoikeuden päätason suoritus")
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

@Title("Korkeakoulun opiskeluoikeuden lisätiedot")
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

@Title("Aikuisten perusopetuksen opiskeluoikeus")
case class HslAikuistenPerusopetuksenOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslAikuistenPerusopetuksenOpiskeluoikeus {
  def apply(oo: AikuistenPerusopetuksenOpiskeluoikeus): HslAikuistenPerusopetuksenOpiskeluoikeus =
    HslAikuistenPerusopetuksenOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.getOrElse(""),
      oppilaitos = oo.oppilaitos,
      tila = HslOpiskeluoikeudenTila.apply(oo.tila),
      suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
      lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
      arvioituPäättymispäivä = oo.arvioituPäättymispäivä
    )
}

@Title("Ammatillinen opiskeluoikeus")
case class HslAmmatillinenOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslAmmatillinenPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
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
      arvioituPäättymispäivä = oo.arvioituPäättymispäivä
    )
}

@Title("Dia opiskeluoikeus")
case class HslDiaOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslDiaOpiskeluoikeus {
  def apply(oo: DIAOpiskeluoikeus): HslDiaOpiskeluoikeus =
    HslDiaOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.getOrElse(""),
      oppilaitos = oo.oppilaitos,
      tila = HslOpiskeluoikeudenTila.apply(oo.tila),
      suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
      lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
      arvioituPäättymispäivä = oo.arvioituPäättymispäivä
    )
}

@Title("IB opiskeluoikeus")
case class HslIBOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslIBOpiskeluoikeus {
  def apply(oo: IBOpiskeluoikeus): HslIBOpiskeluoikeus =
    HslIBOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.getOrElse(""),
      oppilaitos = oo.oppilaitos,
      tila = HslOpiskeluoikeudenTila.apply(oo.tila),
      suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
      lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
      arvioituPäättymispäivä = oo.arvioituPäättymispäivä
    )
}

@Title("International School opiskeluoikeus")
case class HslInternationalSchoolOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus
object HslInternationalSchoolOpiskeluoikeus {
  def apply(oo: InternationalSchoolOpiskeluoikeus): HslInternationalSchoolOpiskeluoikeus = HslInternationalSchoolOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä
  )
}

@Title("Korkeakoulun opiskeluoikeus")
case class HslKorkeakoulunOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslKorkeakouluOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslKorkeakoulunOpiskeluoikeus {
  def apply(oo: KorkeakoulunOpiskeluoikeus): HslKorkeakoulunOpiskeluoikeus =
    HslKorkeakoulunOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.getOrElse(""),
      oppilaitos = oo.oppilaitos,
      tila = HslOpiskeluoikeudenTila.apply(oo.tila),
      suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
      lisätiedot = oo.lisätiedot.map(HslKorkeakouluOpiskeluoikeudenLisätiedot.apply),
      arvioituPäättymispäivä = oo.arvioituPäättymispäivä
    )
}

@Title("Lukion opiskeluoikeus")
case class HslLukionOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslLukionOpiskeluoikeus {
  def apply(oo: LukionOpiskeluoikeus): HslLukionOpiskeluoikeus = HslLukionOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä
  )
}

@Title("Lukioon valmistavan koulutuksen opiskeluoikeus")
case class HslLukioonValmistavanKoulutuksenOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslLukioonValmistavanKoulutuksenOpiskeluoikeus {
  def apply(oo: LukioonValmistavanKoulutuksenOpiskeluoikeus): HslLukioonValmistavanKoulutuksenOpiskeluoikeus = HslLukioonValmistavanKoulutuksenOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä
  )
}

@Title("Perusopetukseen valmistavan opetuksen opiskeluoikeus")
case class HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot] = None,
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus {
  def apply(oo: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus): HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus = HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = None,
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä
  )
}

@Title("Perusopetuksen lisäopetuksen opiskeluoikeus")
case class HslPerusopetuksenLisäopetuksenOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslPerusopetuksenLisäopetuksenOpiskeluoikeus {
  def apply(oo: PerusopetuksenLisäopetuksenOpiskeluoikeus): HslPerusopetuksenLisäopetuksenOpiskeluoikeus = HslPerusopetuksenLisäopetuksenOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä
  )
}


@Title("Perusopetuksen opiskeluoikeus")
case class HslPerusopetuksenOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslPerusopetuksenOpiskeluoikeus {
  def apply(oo: PerusopetuksenOpiskeluoikeus): HslPerusopetuksenOpiskeluoikeus = HslPerusopetuksenOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä
  )
}

@Title("Ylioppilastutkinnon opiskeluoikeus")
case class HslYlioppilastutkinnonOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslYlioppilastutkinnonOpiskeluoikeus {
  def apply(oo: YlioppilastutkinnonOpiskeluoikeus): HslYlioppilastutkinnonOpiskeluoikeus = HslYlioppilastutkinnonOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä
  )
}
