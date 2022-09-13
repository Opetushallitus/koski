package fi.oph.koski.valpas.kansalainen

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.huoltaja.HuollettavienHakuOnnistui
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOppijaLaajatTiedot, ValpasOppivelvollinenOppijaLaajatTiedot}
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.oph.koski.valpas.oppija.{OppijaHakutilanteillaLaajatTiedot, ValpasErrorCategory}

class ValpasKansalainenService(
  application: KoskiApplication
) extends Logging with Timing {
  private val oppijaLaajatTiedotService = application.valpasOppijaLaajatTiedotService
  private val opiskeluoikeusDbService = application.valpasOpiskeluoikeusDatabaseService
  private val koodistoviitepalvelu = application.koodistoViitePalvelu
  private val rajapäivätService = application.valpasRajapäivätService
  private val oppijanumerorekisteriService = application.valpasOppijanumerorekisteriService

  def getKansalaisnäkymänTiedot()(implicit session: ValpasSession): KansalaisnäkymänTiedot = {
    val omatTiedot = getKansalaisenTiedotIlmanKäyttöoikeustarkastusta(session.user.oid)

    val huollettavat = session.user.huollettavat.toList.flatMap {
      case r: HuollettavienHakuOnnistui => r.huollettavat
        .map(o => {
          def fallback = KansalainenOppijaIlmanTietoja(nimi = s"${o.sukunimi} ${o.etunimet}", hetu = o.hetu)
          o.oid
            .toRight(fallback)
            .map(oid => getKansalaisenTiedotIlmanKäyttöoikeustarkastusta(oppijaOid = oid, piilotaTurvakieltoaineisto = true))
            .flatMap(_.left.map(_ => fallback))
        })
      case _ => Seq.empty
    }

    KansalaisnäkymänTiedot(
      omatTiedot = omatTiedot.toOption,
      huollettavat = huollettavat.collect { case Right(r) => r },
      huollettavatIlmanTietoja = huollettavat.collect { case Left(l) => l },
    )
  }

  private def getKansalaisenTiedotIlmanKäyttöoikeustarkastusta(
    oppijaOid: ValpasHenkilö.Oid,
    piilotaTurvakieltoaineisto: Boolean = false
  ): Either[HttpStatus, KansalainenOppijatiedot] = {
    def yhteystiedotHaettava(o: OppijaHakutilanteillaLaajatTiedot): Boolean = !piilotaTurvakieltoaineisto || !o.oppija.henkilö.turvakielto
    def turvakiellonAlainenTietoPoistettava(o: KansalainenOppijatiedot): Boolean = piilotaTurvakieltoaineisto && o.oppija.henkilö.turvakielto

    val oppijaLaajatTiedot =
      opiskeluoikeusDbService
        .getOppija(oppijaOid, rajaaOVKelpoisiinOpiskeluoikeuksiin = false)
        .toRight(ValpasErrorCategory.notFound.oppijaEiOppivelvollisuuslainPiirissä())
        .flatMap(oppijaLaajatTiedotService.asValpasOppijaLaajatTiedot) match {
        case Left(_) =>
          oppijanumerorekisteriService.getKansalaiselleNäkyvätOppijaLaajatTiedotOppijanumerorekisteristäIlmanKäyttöoikeustarkistusta(oppijaOid)
            .left.map(_ => ValpasErrorCategory.notFound.oppijaEiOppivelvollisuuslainPiirissä())
        case o => o
      }

    oppijaLaajatTiedot
      .map(oppijaLaajatTiedotService.fetchHakuYhteystiedoilla)
      .flatMap(o => if (yhteystiedotHaettava(o)) oppijaLaajatTiedotService.withVirallisetYhteystiedot(o) else Right(o) )
      .map(_.validate(koodistoviitepalvelu))
      .map(oppijaLaajatTiedotService.fetchOppivelvollisuudenKeskeytykset)
      .flatMap(withKuntailmoituksetIlmanKäyttöoikeustarkastusta)
      .flatMap(tarkistaKansalaisenTietojenNäkyvyys)
      .map(KansalainenOppijatiedot.apply)
      .map(o => if (turvakiellonAlainenTietoPoistettava(o)) o.poistaTurvakiellonAlaisetTiedot else o)
  }

  private def withKuntailmoituksetIlmanKäyttöoikeustarkastusta(
    oppija: OppijaHakutilanteillaLaajatTiedot
  ): Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] =
    oppija.oppija.ifOppivelvollinenOtherwiseRight(oppija) { o =>
      timed("fetchKuntailmoitukset", 10) {
        application.valpasKuntailmoitusService.getKuntailmoituksetIlmanKäyttöoikeustarkistusta(o)
          .map(oppijaLaajatTiedotService.lisääAktiivisuustiedot(o))
          .map(kuntailmoitukset => oppija.copy(kuntailmoitukset = kuntailmoitukset))
      }
    }

  private def tarkistaKansalaisenTietojenNäkyvyys(
    oppija: OppijaHakutilanteillaLaajatTiedot
  ): Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] = {
    val vainOnrOppija = !oppija.oppija.henkilö.onTallennettuKoskeen
    val eiTallennettuTietojaValppaasseen =
      oppija.oppivelvollisuudenKeskeytykset.isEmpty && oppija.kuntailmoitukset.isEmpty
    val ohitettu25VuotisVuosi = rajapäivätService.tarkastelupäivä.isAfter(
      oppija.oppija.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti.plusYears(5)
    )

    if (vainOnrOppija && eiTallennettuTietojaValppaasseen && ohitettu25VuotisVuosi) {
      Left(ValpasErrorCategory.notFound.oppijaEiOppivelvollisuuslainPiirissä())
    } else {
      Right(oppija)
    }
  }
}
