package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasHenkilöLaajatTiedot, ValpasOppijaLaajatTiedot}
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

class ValpasOppijaSearchService(application: KoskiApplication) extends Logging {
  private val henkilöRepository = application.henkilöRepository
  private val hetuValidator = application.hetu
  private val accessResolver = new ValpasAccessResolver(application.organisaatioRepository)
  private val oppijaService = application.valpasOppijaService

  def findOppijaLaajatTiedot
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppijaLaajatTiedot] =
    if (hetuValidator.validate(query).isRight) {
      searchByHetu(query)
    } else if (Henkilö.isValidHenkilöOid(query)) {
      searchByOppijaOid(query)
    } else {
      Left(HttpStatus(400, Nil))
    }

  def findHenkilö
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöHakutiedot] =
    findOppijaLaajatTiedot(query)
      .map(ValpasHenkilöHakutiedot.apply)

  private def searchByHetu
    (hetu: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppijaLaajatTiedot] =
    henkilöRepository
      .findByHetuOrCreateIfInYtrOrVirta(hetu)
      .toRight(ValpasErrorCategory.searchNotFound.hetu())
      .flatMap(asValpasOppijaLaajatTiedot)

  private def searchByOppijaOid
    (oid: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppijaLaajatTiedot] =
    henkilöRepository
      .findByOid(oid)
      .toRight(ValpasErrorCategory.searchNotFound.oid())
      .flatMap(asValpasOppijaLaajatTiedot)

  private def asValpasOppijaLaajatTiedot
    (henkilö: OppijaHenkilö)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppijaLaajatTiedot] =
    oppijaService
      .getOppijaLaajatTiedot(henkilö.oid)
      .flatMap(accessResolver.withOppijaAccess(ValpasRooli.OPPILAITOS_MAKSUTTOMUUS))
}

object ValpasHenkilöHakutiedot {
  def apply(oppija: ValpasOppijaLaajatTiedot): ValpasHenkilöHakutiedot =
    ValpasHenkilöHakutiedot(
      oid = oppija.henkilö.oid,
      hetu = oppija.henkilö.hetu,
      etunimet = oppija.henkilö.etunimet,
      sukunimi = oppija.henkilö.sukunimi,
    )
}

case class ValpasHenkilöHakutiedot(
  oid: ValpasHenkilö.Oid,
  hetu: Option[String],
  etunimet: String,
  sukunimi: String,
)
