
package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.validation.MaksuttomuusValidation
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasHenkilöLaajatTiedot, ValpasOppijaLaajatTiedot}
import fi.oph.koski.valpas.valpasuser.ValpasRooli.Role
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}
import fi.oph.scalaschema.annotation.SyntheticProperty

class ValpasOppijaSearchService(application: KoskiApplication) extends Logging {
  private val henkilöRepository = application.henkilöRepository
  private val hetuValidator = application.hetu
  private val accessResolver = new ValpasAccessResolver
  private val oppijaService = application.valpasOppijaService
  private val opiskeluoikeusRepository = application.opiskeluoikeusRepository
  private val rajapäivätService = application.valpasRajapäivätService

  def findHenkilöSuorittaminen
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    findHenkilö(ValpasRooli.OPPILAITOS_SUORITTAMINEN, query)
  }

  def findHenkilöKunta
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    findHenkilö(ValpasRooli.KUNTA, query)
  }

  def findHenkilöMaksuttomuus
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    accessResolver.assertAccessToAnyOrg(ValpasRooli.OPPILAITOS_MAKSUTTOMUUS)
      .flatMap(_ => findHenkilö(asMaksuttomuusHenkilöhakuResultIlmanOikeustarkistusta _, query))
  }

  private def findHenkilö
    (rooli: ValpasRooli.Role, query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    accessResolver.assertAccessToAnyOrg(rooli)
      .flatMap(_ => findHenkilö(asYksinkertainenHenkilöhakuResult(rooli) _, query))
  }

  private def findHenkilö
    (asHenkilöhakuResult: (OppijaHenkilö) => Either[HttpStatus, ValpasHenkilöhakuResult], query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    if (hetuValidator.validate(query).isRight) {
      searchByHetu(asHenkilöhakuResult, query)
    } else if (Henkilö.isValidHenkilöOid(query)) {
      searchByOppijaOid(asHenkilöhakuResult, query)
    } else {
      Left(ValpasErrorCategory.validation.epävalidiHenkilöhakutermi())
    }
  }

  private def searchByHetu
    (asHenkilöhakuResult: (OppijaHenkilö) => Either[HttpStatus, ValpasHenkilöhakuResult], hetu: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] =
    asSearchResult(asHenkilöhakuResult, henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu))

  private def searchByOppijaOid
    (asHenkilöhakuResult: (OppijaHenkilö) => Either[HttpStatus, ValpasHenkilöhakuResult], oid: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] =
    asSearchResult(asHenkilöhakuResult, henkilöRepository.findByOid(oid))

  private def asSearchResult
    (asResult: (OppijaHenkilö) => Either[HttpStatus, ValpasHenkilöhakuResult], oppijaHenkilö: Option[OppijaHenkilö])
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    oppijaHenkilö match {
      case None => Right(ValpasEiLöytynytHenkilöhakuResult())
      case Some(henkilö) => {
        asResult(henkilö)
      }
    }
  }

  private def asMaksuttomuusHenkilöhakuResultIlmanOikeustarkistusta
    (henkilö: OppijaHenkilö)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    val onMahdollisestiLainPiirissä =
      MaksuttomuusValidation.eiOppivelvollisuudenLaajentamislainPiirissäSyyt(
        henkilö.syntymäaika,
        henkilö.oid,
        opiskeluoikeusRepository,
        rajapäivätService
      ).isEmpty

    if (onMahdollisestiLainPiirissä) {
      oppijaService.getOppijaLaajatTiedotIlmanOikeustarkastusta(henkilö.oid)
        .map({
          case Some(o) if o.onOikeusValvoaMaksuttomuutta => ValpasLöytyiHenkilöhakuResult(o)
          // Henkilö, jonka tiedot löytyvät, mutta jolla maksuttomuus on päättynyt esim. toiselta asteelta
          // valmistumiseen, ei ole enää maksuttomuuden piirissä:
          case Some(_) => ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult()
          case None => henkilöRepository.findByOid(henkilö.oid) match {
            case Some(h) if h.kotikunta.isEmpty => ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult()
            case _ => ValpasEiLöytynytHenkilöhakuResult()
          }
        })
    } else {
      Right(ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult())
    }
  }

  private def asYksinkertainenHenkilöhakuResult
    (rooli: ValpasRooli.Role)
    (henkilö: OppijaHenkilö)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasLöytyiHenkilöhakuResult] = {
    oppijaService.getOppijaLaajatTiedot(rooli, henkilö.oid)
      .map(ValpasLöytyiHenkilöhakuResult.apply)
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

case class ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(
  eiLainTaiMaksuttomuudenPiirissä: Boolean = true
) extends ValpasHenkilöhakuResult {
  def ok = false
}

case class ValpasEiLöytynytHenkilöhakuResult() extends ValpasHenkilöhakuResult {
  def ok = false
}
