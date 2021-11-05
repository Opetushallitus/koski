package fi.oph.koski.valpas.rouhinta

import java.time.LocalDate

import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.valpas.OppijaHakutilanteillaLaajatTiedot
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOpiskeluoikeusLaajatTiedot, ValpasOpiskeluoikeusTiedot}
import fi.oph.koski.valpas.valpasrepository.ValpasOppivelvollisuudenKeskeytys

case class ValpasRouhintaOppivelvollinen(
  oppijanumero: ValpasHenkilö.Oid,
  etunimet: String,
  sukunimi: String,
  syntymäaika: Option[LocalDate],
  hetu: Option[String],
  viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus: Option[RouhintaOpiskeluoikeus],
  oppivelvollisuudenKeskeytys: Seq[ValpasOppivelvollisuudenKeskeytys],
) {
  def suorittaaOppivelvollisuutta: Boolean =
    viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus.exists(_.viimeisinTila.koodiarvo == "lasna")
}

object ValpasRouhintaOppivelvollinen {
  def apply(tiedot: OppijaHakutilanteillaLaajatTiedot): ValpasRouhintaOppivelvollinen = {
    val oos = tiedot.oppija.opiskeluoikeudet
      .filter(_.oppivelvollisuudenSuorittamiseenKelpaava)
      .flatMap(oo => RouhintaOpiskeluoikeus.apply(oo))

    ValpasRouhintaOppivelvollinen(
      oppijanumero = tiedot.oppija.henkilö.oid,
      etunimet = tiedot.oppija.henkilö.etunimet,
      sukunimi = tiedot.oppija.henkilö.sukunimi,
      syntymäaika = tiedot.oppija.henkilö.syntymäaika,
      hetu = tiedot.oppija.henkilö.hetu,
      viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus = oos.sorted.lastOption,
      oppivelvollisuudenKeskeytys = tiedot.oppivelvollisuudenKeskeytykset.filter(_.voimassa)
    )
  }

  def apply(henkilö: OppijaHenkilö): ValpasRouhintaOppivelvollinen = ValpasRouhintaOppivelvollinen(
    oppijanumero = henkilö.oid,
    etunimet = henkilö.etunimet,
    sukunimi = henkilö.sukunimi,
    syntymäaika = henkilö.syntymäaika,
    hetu = henkilö.hetu,
    viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus = None,
    oppivelvollisuudenKeskeytys = Nil,
  )
}

case class RouhintaOpiskeluoikeus(
  suorituksenTyyppi: Koodistokoodiviite,
  päättymispäivä: Option[String],
  viimeisinTila: Koodistokoodiviite,
  toimipiste: LocalizedString,
) extends Ordered[RouhintaOpiskeluoikeus] {
  override def compare(that: RouhintaOpiskeluoikeus): Int =
    OrderedOpiskeluoikeusTiedot.compareDates(päättymispäivä, that.päättymispäivä)
}

object RouhintaOpiskeluoikeus {
  def apply(oo: ValpasOpiskeluoikeusLaajatTiedot): Option[RouhintaOpiskeluoikeus] = {
    oo.tarkasteltavaPäätasonSuoritus.flatMap(päätasonSuoritus => {
      Seq(
        oo.perusopetusTiedot.map(t => OrderedOpiskeluoikeusTiedot(t)),
        oo.perusopetuksenJälkeinenTiedot.map(t => OrderedOpiskeluoikeusTiedot(t)),
      )
        .flatten
        .sorted
        .lastOption
        .map(viimeisinTila => RouhintaOpiskeluoikeus(
          suorituksenTyyppi = päätasonSuoritus.suorituksenTyyppi,
          päättymispäivä = viimeisinTila.tiedot.päättymispäivä,
          viimeisinTila = viimeisinTila.tiedot.tarkastelupäivänKoskiTila,
          toimipiste = päätasonSuoritus.toimipiste.nimi,
        ))
    })
  }
}

case class OrderedOpiskeluoikeusTiedot(
  tiedot: ValpasOpiskeluoikeusTiedot
) extends Ordered[OrderedOpiskeluoikeusTiedot] {
  override def compare(that: OrderedOpiskeluoikeusTiedot): Int =
    if (tiedot.päättymispäivä != that.tiedot.päättymispäivä) {
      OrderedOpiskeluoikeusTiedot.compareDates(tiedot.päättymispäivä, that.tiedot.päättymispäivä)
    } else {
      OrderedOpiskeluoikeusTiedot.compareDates(tiedot.alkamispäivä, that.tiedot.alkamispäivä)
    }
}

object OrderedOpiskeluoikeusTiedot {
  def compareDates(a: Option[String], b: Option[String]): Int =
    a.getOrElse("9999-99-99").compare(b.getOrElse("9999-99-99"))
}
