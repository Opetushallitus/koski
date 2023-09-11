package fi.oph.koski.valpas.oppija

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.opiskeluoikeusrepository._
import fi.oph.koski.valpas.rouhinta.ValpasRouhintaTiming
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

class ValpasOppijalistatService(
  application: KoskiApplication
) extends Logging with ValpasRouhintaTiming {
  private val oppijaLaajatTiedotService = application.valpasOppijaLaajatTiedotService
  private val hakukoosteService = application.valpasHakukoosteService
  private val opiskeluoikeusDbService = application.valpasOpiskeluoikeusDatabaseService
  private val localizationRepository = application.valpasLocalizationRepository
  private val koodistoviitepalvelu = application.koodistoViitePalvelu

  private val accessResolver = new ValpasAccessResolver

  def getOppijatLaajatTiedot
    (
      rooli: ValpasRooli.Role,
      oppilaitosOid: ValpasOppilaitos.Oid,
      hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value
    )
      (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[ValpasOppijaLaajatTiedot]] = {
    accessResolver.assertAccessToOrg(rooli, oppilaitosOid)
      .map(_ => opiskeluoikeusDbService.getOppijatByOppilaitos(oppilaitosOid, hakeutumisvalvontaTieto))
      .flatMap(results => HttpStatus.foldEithers(results.map(oppijaLaajatTiedotService.asValpasOppijaLaajatTiedot())))
      .map(accessResolver.filterByOppijaAccess(rooli))
  }

  def getOppijatLaajatTiedotYhteystiedoilla(
    oppilaitosOid: ValpasOppilaitos.Oid,
    oppijaOids: Seq[ValpasHenkilö.Oid]
  )(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    val errorClue = oppilaitosOidErrorClue(oppilaitosOid)

    accessResolver.assertAccessToOrg(ValpasRooli.OPPILAITOS_HAKEUTUMINEN, oppilaitosOid)
      .map(_ => opiskeluoikeusDbService.getOppijatByOppilaitos(oppilaitosOid, HakeutumisvalvontaTieto.Kaikki))
      .map(_.filter(oppijaRow => oppijaOids.contains(oppijaRow.oppijaOid)))
      .flatMap(results => HttpStatus.foldEithers(results.map(oppijaLaajatTiedotService.asValpasOppijaLaajatTiedot())))
      .map(accessResolver.filterByOppijaAccess(ValpasRooli.OPPILAITOS_HAKEUTUMINEN))
      .map(hakukoosteService.fetchHautYhteystiedoilla(errorClue, oppijaOids))
      .flatMap(oppijat => HttpStatus.foldEithers(oppijat.map(oppijaLaajatTiedotService.withVirallisetYhteystiedot)))
      .map(oppijat => oppijat.map(_.validate(koodistoviitepalvelu)))
  }

  def getOppijalistaIlmanOikeustarkastusta
    (oppijaOids: Seq[ValpasHenkilö.Oid])
  : Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    rouhintaTimed("getOppijalista", oppijaOids.size) {
      HttpStatus.foldEithers({
        val oppijat = opiskeluoikeusDbService.getOppijat(oppijaOids, rajaaOVKelpoisiinOpiskeluoikeuksiin = false, haeMyösOppivelvollisuudestaVapautetut = false)

        rouhintaTimed("getOppijalista:asValpasOppijaLaajatTiedot", oppijat.size) {
          oppijat.map(oppijaLaajatTiedotService.asValpasOppijaLaajatTiedot())
        }
      })
        .map(asEmptyOppijaHakutilanteillaLaajatTiedot) // Huom! Ei haeta hakutietoja, halutaan vain vaihtaa tyyppi fetchOppivelvollisuudenKeskeytykset-kutsua varten
    }
  }

  private def asEmptyOppijaHakutilanteillaLaajatTiedot(oppijat: Seq[ValpasOppijaLaajatTiedot]): Seq[OppijaHakutilanteillaLaajatTiedot] = {
    rouhintaTimed("asEmptyOppijaHakutilanteillaLaajatTiedot", oppijat.size) {
      oppijat.map(asEmptyOppijaHakutilanteillaLaajatTiedot)
    }
  }

  private def asEmptyOppijaHakutilanteillaLaajatTiedot(oppija: ValpasOppijaLaajatTiedot): OppijaHakutilanteillaLaajatTiedot = {
    OppijaHakutilanteillaLaajatTiedot.apply(oppija = oppija, yhteystietoryhmänNimi = localizationRepository.get("oppija__yhteystiedot"), haut = Right(Seq.empty))
  }

  def getOppijaOiditHetuillaIlmanOikeustarkastusta(hetut: Seq[String]): Seq[HetuMasterOid] = {
    opiskeluoikeusDbService.haeOppijatHetuilla(hetut)
  }

  def getOppivelvollisetKotikunnallaIlmanOikeustarkastusta(kunta: String): Seq[HetuMasterOid] = {
    opiskeluoikeusDbService.haeOppivelvollisetKotikunnalla(kunta)
  }

  private def oppilaitosOidErrorClue(oppilaitosOid: ValpasOppilaitos.Oid): String =
    s"oppilaitos: ${oppilaitosOid}"
}

