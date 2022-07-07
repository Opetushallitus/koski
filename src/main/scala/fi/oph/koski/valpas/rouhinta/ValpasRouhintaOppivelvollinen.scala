package fi.oph.koski.valpas.rouhinta

import java.time.LocalDate
import fi.oph.koski.henkilo.{OppijaHenkilö, OppijaNumerorekisteriKuntarouhintaOppija}
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.valpas.opiskeluoikeusrepository.{OrderedOpiskeluoikeusTiedot, ValpasHenkilö, ValpasOpiskeluoikeusLaajatTiedot}
import fi.oph.koski.valpas.oppija.{OppijaHakutilanteillaLaajatTiedot, ValpasKuntailmoitusSuppeatTiedot}
import fi.oph.koski.valpas.valpasrepository.ValpasOppivelvollisuudenKeskeytys

case class ValpasRouhintaOppivelvollinen(
  oppijanumero: ValpasHenkilö.Oid,
  kaikkiOidit: Option[Seq[ValpasHenkilö.Oid]],
  etunimet: String,
  sukunimi: String,
  syntymäaika: Option[LocalDate],
  hetu: Option[String],
  viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus: Option[RouhintaOpiskeluoikeus],
  oppivelvollisuudenKeskeytys: Seq[ValpasOppivelvollisuudenKeskeytys],
  vainOppijanumerorekisterissä: Boolean,
  aktiivinenKuntailmoitus: Option[ValpasKuntailmoitusSuppeatTiedot]
)

object ValpasRouhintaOppivelvollinen {
  def apply(tiedot: OppijaHakutilanteillaLaajatTiedot): ValpasRouhintaOppivelvollinen = {
    val oos = tiedot.oppija.opiskeluoikeudet
      .filter(oo => oo.oppivelvollisuudenSuorittamiseenKelpaava && !oo.isOpiskeluTulevaisuudessa)
      .flatMap(oo => RouhintaOpiskeluoikeus.apply(oo))

    val aktiivinenKuntailmoitus =
      tiedot.kuntailmoitukset.find(_.aktiivinen.contains(true))
        .map(ValpasKuntailmoitusSuppeatTiedot.apply)

    ValpasRouhintaOppivelvollinen(
      oppijanumero = tiedot.oppija.henkilö.oid,
      kaikkiOidit = Some(tiedot.oppija.henkilö.kaikkiOidit.toSeq),
      etunimet = tiedot.oppija.henkilö.etunimet,
      sukunimi = tiedot.oppija.henkilö.sukunimi,
      syntymäaika = tiedot.oppija.henkilö.syntymäaika,
      hetu = tiedot.oppija.henkilö.hetu,
      viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus = oos.sorted.lastOption,
      oppivelvollisuudenKeskeytys = tiedot.oppivelvollisuudenKeskeytykset.filter(_.voimassa),
      vainOppijanumerorekisterissä = false,
      aktiivinenKuntailmoitus = aktiivinenKuntailmoitus
    )
  }

  def apply(tiedot: OppijaNumerorekisteriKuntarouhintaOppija): ValpasRouhintaOppivelvollinen = {
    ValpasRouhintaOppivelvollinen(
      oppijanumero = tiedot.oidHenkilo,
      kaikkiOidit = Some(Seq(tiedot.oidHenkilo)),
      etunimet = tiedot.etunimet,
      sukunimi = tiedot.sukunimi,
      syntymäaika = Some(LocalDate.parse(tiedot.syntymaaika)),
      hetu = tiedot.hetu,
      viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus = None,
      oppivelvollisuudenKeskeytys = Seq.empty,
      vainOppijanumerorekisterissä = true,
      aktiivinenKuntailmoitus = None
    )
  }

  def apply(henkilö: OppijaHenkilö): ValpasRouhintaOppivelvollinen = ValpasRouhintaOppivelvollinen(
    oppijanumero = henkilö.oid,
    kaikkiOidit = None,
    etunimet = henkilö.etunimet,
    sukunimi = henkilö.sukunimi,
    syntymäaika = henkilö.syntymäaika,
    hetu = henkilö.hetu,
    viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus = None,
    oppivelvollisuudenKeskeytys = Nil,
    vainOppijanumerorekisterissä = true,
    aktiivinenKuntailmoitus = None
  )
}

case class RouhintaOpiskeluoikeus(
  suorituksenTyyppi: Koodistokoodiviite,
  päättymispäivä: Option[String],
  viimeisinValpasTila: Koodistokoodiviite,
  viimeisinTila: Koodistokoodiviite,
  toimipiste: LocalizedString,
) extends Ordered[RouhintaOpiskeluoikeus] {
  override def compare(that: RouhintaOpiskeluoikeus): Int =
    OrderedOpiskeluoikeusTiedot.compareDates(päättymispäivä, that.päättymispäivä)
}

object RouhintaOpiskeluoikeus {
  def apply(oo: ValpasOpiskeluoikeusLaajatTiedot): Option[RouhintaOpiskeluoikeus] = {
    oo.tarkasteltavaPäätasonSuoritus.flatMap(päätasonSuoritus => {
      oo.viimeisimmätOpiskeluoikeustiedot
        .map(viimeisinTila => RouhintaOpiskeluoikeus(
          suorituksenTyyppi = päätasonSuoritus.suorituksenTyyppi,
          päättymispäivä = viimeisinTila.päättymispäivä,
          viimeisinValpasTila = viimeisinTila.tarkastelupäivänTila,
          viimeisinTila = viimeisinTila.tarkastelupäivänKoskiTila,
          toimipiste = päätasonSuoritus.toimipiste.nimi,
        ))
    })
  }
}
