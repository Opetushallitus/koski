package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasHenkilöLaajatTiedot, ValpasOppijaLaajatTiedot}
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}
import fi.oph.scalaschema.annotation.SyntheticProperty

class ValpasOppijaSearchService(application: KoskiApplication) extends Logging {
  private val henkilöRepository = application.henkilöRepository
  private val hetuValidator = application.hetu
  private val accessResolver = new ValpasAccessResolver
  private val oppijaService = application.valpasOppijaService

  def findHenkilö
    (rooli: ValpasRooli.Role, query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    accessResolver.assertAccessToAnyOrg(rooli)
      .flatMap(_ => {
        if (hetuValidator.validate(query).isRight) {
          searchByHetu(rooli, query)
        } else if (Henkilö.isValidHenkilöOid(query)) {
          searchByOppijaOid(rooli, query)
        } else {
          Left(ValpasErrorCategory.searchValidation())
        }
      })
  }
  
  private def searchByHetu
    (rooli: ValpasRooli.Role, hetu: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] =
    asSearchResult(rooli, henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu))

  private def searchByOppijaOid
    (rooli: ValpasRooli.Role, oid: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] =
    asSearchResult(rooli, henkilöRepository.findByOid(oid))

  private def asSearchResult
    (rooli: ValpasRooli.Role, oppijaHenkilö: Option[OppijaHenkilö])
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    oppijaHenkilö match {
      case None => Right(ValpasEiLöytynytHenkilöhakuResult())
      case Some(henkilö) => {
        oppijaService.getOppijaLaajatTiedot(rooli, henkilö.oid)
          .map(ValpasLöytyiHenkilöhakuResult.apply)
      }
    }
  }
}

object ValpasLöytyiHenkilöhakuResult {
  def apply(oppija: ValpasOppijaLaajatTiedot): ValpasLöytyiHenkilöhakuResult =
    ValpasLöytyiHenkilöhakuResult(
      oid = oppija.henkilö.oid,
      hetu = oppija.henkilö.hetu,
      etunimet = oppija.henkilö.etunimet,
      sukunimi = oppija.henkilö.sukunimi,
    )
}

trait ValpasHenkilöhakuResult {
  @SyntheticProperty
  def ok: Boolean
}

case class ValpasLöytyiHenkilöhakuResult(
  oid: ValpasHenkilö.Oid,
  hetu: Option[String],
  etunimet: String,
  sukunimi: String,
) extends ValpasHenkilöhakuResult {
  def ok = true
}

case class ValpasEiLöytynytHenkilöhakuResult() extends ValpasHenkilöhakuResult {
  def ok = false
}
