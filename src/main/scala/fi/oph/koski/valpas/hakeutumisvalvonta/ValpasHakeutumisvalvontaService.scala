package fi.oph.koski.valpas.hakeutumisvalvonta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.valpas.opiskeluoikeusrepository._
import fi.oph.koski.valpas.oppija.{OppijaHakutilanteillaLaajatTiedot, OppijaHakutilanteillaSuppeatTiedot}
import fi.oph.koski.valpas.rouhinta.ValpasRouhintaTiming
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

class ValpasHakeutumisvalvontaService(
  application: KoskiApplication
) extends Logging with ValpasRouhintaTiming {
  private val lisätiedotRepository = application.valpasOpiskeluoikeusLisätiedotRepository
  private val oppijalistatService = application.valpasOppijalistatService
  private val kuntailmoitusService = application.valpasKuntailmoitusService
  private val hakukoosteService = application.valpasHakukoosteService

  def getOppijatSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value, haeHakutilanteet: Seq[Henkilö.Oid] = Seq.empty)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] =
    getOppijatLaajatTiedot(oppilaitosOid, hakeutumisvalvontaTieto, haeHakutilanteet)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))

  private def getOppijatLaajatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value, haeHakutilanteet: Seq[Henkilö.Oid])
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    (if (haeHakutilanteet.nonEmpty) {
      getOppijatLaajatTiedotHakutilanteilla(
        ValpasRooli.OPPILAITOS_HAKEUTUMINEN,
        oppilaitosOid,
        hakeutumisvalvontaTieto,
        haeHakutilanteet,
      ).map(_.filter(o => haeHakutilanteet.contains(o.oppija.henkilö.oid)))
    } else {
      getOppijatLaajatTiedotIlmanHakutilanteita(
        ValpasRooli.OPPILAITOS_HAKEUTUMINEN,
        oppilaitosOid,
        hakeutumisvalvontaTieto
      )
    })
      .map(kuntailmoitusService.poistaKuntailmoitetutOpiskeluoikeudet(säästäJosOpiskeluoikeusVoimassa = false))
      .map(lisätiedotRepository.readForOppijat)
      .map(_.map(oppijaLisätiedotTuple => oppijaLisätiedotTuple._1.withLisätiedot(oppijaLisätiedotTuple._2)))
  }

  private def getOppijatLaajatTiedotHakutilanteilla
    (rooli: ValpasRooli.Role, oppilaitosOid: ValpasOppilaitos.Oid, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value, oppijatJoilleHaetaanHakutiedot: Seq[Henkilö.Oid])
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    val errorClue = oppilaitosOidErrorClue(oppilaitosOid)

    oppijalistatService.getOppijatLaajatTiedot(rooli, oppilaitosOid, hakeutumisvalvontaTieto)
      .map(hakukoosteService.fetchHautIlmanYhteystietoja(errorClue, oppijatJoilleHaetaanHakutiedot))
  }

  private def oppilaitosOidErrorClue(oppilaitosOid: ValpasOppilaitos.Oid): String =
    s"oppilaitos: ${oppilaitosOid}"

  private def getOppijatLaajatTiedotIlmanHakutilanteita
    (rooli: ValpasRooli.Role, oppilaitosOid: ValpasOppilaitos.Oid, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    oppijalistatService.getOppijatLaajatTiedot(rooli, oppilaitosOid, hakeutumisvalvontaTieto)
      .map(oppijat => oppijat.map(OppijaHakutilanteillaLaajatTiedot.apply))
  }

  def getKunnalleTehdytIlmoituksetSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] =
    kuntailmoitusService.getOppilaitoksenKunnalleTekemätIlmoituksetLaajatTiedot(oppilaitosOid)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))
}
