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
  private val accessResolver = new ValpasAccessResolver(application.organisaatioRepository)
  private val oppijaService = application.valpasOppijaService


  def findHenkilöMaksuttomuus
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöMaksuttomuushakuResult] =
    if (hetuValidator.validate(query).isRight) {
      searchByHetu(query)
    } else if (Henkilö.isValidHenkilöOid(query)) {
      searchByOppijaOid(query)
    } else {
      Left(ValpasErrorCategory.searchValidation())
    }

  private def searchByHetu
    (hetu: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöMaksuttomuushakuResult] =
    asSearchResult(henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu))

  private def searchByOppijaOid
    (oid: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöMaksuttomuushakuResult] =
    asSearchResult(henkilöRepository.findByOid(oid))

  private def asSearchResult
    (oppijaHenkilö: Option[OppijaHenkilö])
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöMaksuttomuushakuResult] = {
    oppijaHenkilö match {
      case None => Right(ValpasHenkilöHakutiedotMaksuttomuusEiPääteltävissä())
      case Some(henkilö) => {
        oppijaService.getOppijaLaajatTiedot (henkilö.oid)
          .flatMap(accessResolver.withOppijaAccess(ValpasRooli.OPPILAITOS_MAKSUTTOMUUS) )
          .map(ValpasHenkilöMaksuttomuushakutulos.apply)
      }
    }
  }
}

object ValpasHenkilöMaksuttomuushakutulos {
  def apply(oppija: ValpasOppijaLaajatTiedot): ValpasHenkilöMaksuttomuushakutulos =
    ValpasHenkilöMaksuttomuushakutulos(
      oid = oppija.henkilö.oid,
      hetu = oppija.henkilö.hetu,
      etunimet = oppija.henkilö.etunimet,
      sukunimi = oppija.henkilö.sukunimi,
    )
}

trait ValpasHenkilöMaksuttomuushakuResult {
  @SyntheticProperty
  def ok: Boolean
}

case class ValpasHenkilöMaksuttomuushakutulos(
  oid: ValpasHenkilö.Oid,
  hetu: Option[String],
  etunimet: String,
  sukunimi: String,
) extends ValpasHenkilöMaksuttomuushakuResult {
  def ok = true
}

case class ValpasHenkilöHakutiedotMaksuttomuusEiPääteltävissä() extends ValpasHenkilöMaksuttomuushakuResult {
  def ok = false
}
