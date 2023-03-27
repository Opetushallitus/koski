
package fi.oph.koski.valpas.oppijahaku

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.validation.MaksuttomuusValidation
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOppijaLaajatTiedot, ValpasOppivelvollinenOppijaLaajatTiedot, ValpasRajapäivätService}
import fi.oph.koski.valpas.oppija.{ValpasAccessResolver, ValpasErrorCategory}
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}
import fi.oph.scalaschema.annotation.SyntheticProperty
import slick.jdbc.GetResult

import java.time.LocalDate

class ValpasOppijaSearchService(application: KoskiApplication) extends Logging {
  private val henkilöRepository = application.henkilöRepository
  private val hetuValidator = application.hetu
  private val accessResolver = new ValpasAccessResolver
  private val oppijaLaajatTiedotService = application.valpasOppijaLaajatTiedotService
  private val opiskeluoikeusRepository = application.opiskeluoikeusRepository
  private val rajapäivätService = application.valpasRajapäivätService
  private val opintopolkuHenkilöt = application.opintopolkuHenkilöFacade
  private val oppijanumerorekisteriService = application.valpasOppijanumerorekisteriService

  def findHenkilöSuorittaminen
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    accessResolver.assertAccessToAnyOrg(ValpasRooli.OPPILAITOS_SUORITTAMINEN)
      .flatMap(_ => findHenkilö(asSuorittaminenHenkilöhakuResult _, query))
  }

  def findHenkilöKunta
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    accessResolver.assertAccessToAnyOrg(ValpasRooli.KUNTA)
      .flatMap(_ => findHenkilö(asKuntaHenkilöhakuResult _, query))
  }

  def findHenkilöMaksuttomuus
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    accessResolver.assertAccessToAnyOrg(ValpasRooli.OPPILAITOS_MAKSUTTOMUUS)
      .flatMap(_ => findHenkilö(asMaksuttomuusHenkilöhakuResultIlmanOikeustarkistusta _, query))
  }

  def findHenkilöOideillaIlmanOikeustarkastusta
    (oppijaOids: Seq[String])
    (implicit session: ValpasSession)
  : Seq[Either[HttpStatus, ValpasHenkilöhakuResult]] = {
    opintopolkuHenkilöt
      .findMasterOppijat(oppijaOids.toList)
      .values
      .map(asMaksuttomuusHenkilöhakuResultIlmanOikeustarkistusta)
      .toSeq
  }

  def findHenkilöHetuillaIlmanOikeustarkastusta
    (hetus: Seq[String])
    (implicit session: ValpasSession)
  : Seq[Either[HttpStatus, ValpasHenkilöhakuResult]] = {
    opintopolkuHenkilöt
      .findOppijatByHetusNoSlaveOids(hetus.toList)
      .map(asMaksuttomuusHenkilöhakuResultIlmanOikeustarkistusta)
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
      Left(ValpasErrorCategory.badRequest.validation.epävalidiHenkilöhakutermi())
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
    asSearchResult(asHenkilöhakuResult, henkilöRepository.findByOid(oid, findMasterIfSlaveOid = true))

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
    val perusopetuksenAikavälit = opiskeluoikeusRepository.getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(henkilö.oid)
    val onMahdollisestiLainPiirissä =
      MaksuttomuusValidation.eiOppivelvollisuudenLaajentamislainPiirissäSyyt(
        henkilö.syntymäaika,
        perusopetuksenAikavälit,
        rajapäivätService
      ).isEmpty

    if (onMahdollisestiLainPiirissä) {
      oppijaLaajatTiedotService.getOppijaLaajatTiedotIlmanOikeustarkastusta(henkilö.oid)
        .map({
          case Some(o) if o.onOikeusValvoaMaksuttomuutta && !o.oppivelvollisuudestaVapautettu => ValpasLöytyiHenkilöhakuResult(o, rajapäivätService)
          // Henkilö, jonka tiedot löytyvät, mutta jolla maksuttomuus on päättynyt esim. toiselta asteelta
          // valmistumiseen, ei ole enää maksuttomuuden piirissä:
          case Some(o) => ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(Some(o.henkilö.oid), o.henkilö.hetu)
          case None => oppijanumerorekisteriService.asLaajatOppijaHenkilöTiedot(henkilö) match {
            case Some(h) if !h.turvakielto && h.laajennetunOppivelvollisuudenUlkopuolinenKunnanPerusteella => ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(Some(h.oid), h.hetu)
            case Some(h) if oppijanumerorekisteriService.onMaksuttomuuskäyttäjälleNäkyväVainOnrssäOlevaOppija(h) =>
              ValpasLöytyiHenkilöhakuResult(h, vainOppijanumerorekisterissä = true, rajapäivätService)
            case _ => ValpasEiLöytynytHenkilöhakuResult()
          }
        })
    } else {
      Right(ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(Some(henkilö.oid), henkilö.hetu))
    }
  }

  private def asKuntaHenkilöhakuResult
    (henkilö: OppijaHenkilö)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasLöytyiHenkilöhakuResult] = {
    oppijaLaajatTiedotService.getOppijaLaajatTiedotIlmanOikeustarkastusta(henkilö.oid) match {
      case Right(Some(oppija)) =>
        // Oli Koskessa, tarkista käyttöoikeudet ja palauta, jos ok
        accessResolver.withOppijaAccessAsRole(ValpasRooli.KUNTA)(oppija)
          .map(o => ValpasLöytyiHenkilöhakuResult.apply(o, rajapäivätService))
      case Right(None) if oppijanumerorekisteriService.onKunnalleNäkyväVainOnrssäOlevaOppija(henkilö) =>
        Right(ValpasLöytyiHenkilöhakuResult(henkilö, vainOppijanumerorekisterissä = true, rajapäivätService))
      case _ => Left(ValpasErrorCategory.forbidden.oppija())
    }
  }

  private def asSuorittaminenHenkilöhakuResult
    (henkilö: OppijaHenkilö)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasLöytyiHenkilöhakuResult] =
    oppijaLaajatTiedotService.getOppijaLaajatTiedot(ValpasRooli.OPPILAITOS_SUORITTAMINEN, henkilö.oid, haeMyösVainOppijanumerorekisterissäOleva = false)
      .map(o => ValpasLöytyiHenkilöhakuResult.apply(o, rajapäivätService))

  implicit private val getResultValpasLöytyiHenkilöhakuResult: GetResult[ValpasLöytyiHenkilöhakuResult] = GetResult(row =>
    ValpasLöytyiHenkilöhakuResult(
      oid = row.rs.getString("oid"),
      hetu = Option(row.rs.getString("hetu")),
      etunimet = row.rs.getString("etunimet"),
      sukunimi = row.rs.getString("sukunimi"),
    ))
}

object ValpasLöytyiHenkilöhakuResult {
  def apply(oppija: ValpasOppijaLaajatTiedot, rajapäivät: ValpasRajapäivätService): ValpasLöytyiHenkilöhakuResult =
    ValpasLöytyiHenkilöhakuResult(
      oid = oppija.henkilö.oid,
      hetu = oppija.henkilö.hetu,
      etunimet = oppija.henkilö.etunimet,
      sukunimi = oppija.henkilö.sukunimi,
      maksuttomuusVoimassaAstiIänPerusteella = oppija.henkilö.syntymäaika.map(rajapäivät.maksuttomuusVoimassaAstiIänPerusteella)
    )

  def apply(oppija: OppijaHenkilö, vainOppijanumerorekisterissä: Boolean, rajapäivät: ValpasRajapäivätService): ValpasLöytyiHenkilöhakuResult = {
    ValpasLöytyiHenkilöhakuResult(
      oid = oppija.oid,
      hetu = oppija.hetu,
      etunimet = oppija.etunimet,
      sukunimi = oppija.sukunimi,
      vainOppijanumerorekisterissä = vainOppijanumerorekisterissä,
      maksuttomuusVoimassaAstiIänPerusteella = oppija.syntymäaika.map(rajapäivät.maksuttomuusVoimassaAstiIänPerusteella)
    )
  }
}

trait ValpasHenkilöhakuResult {
  @SyntheticProperty
  def ok: Boolean

  def cleanUpForUserSearch: ValpasHenkilöhakuResult = this
}

case class ValpasLöytyiHenkilöhakuResult(
  oid: ValpasHenkilö.Oid,
  hetu: Option[String],
  etunimet: String,
  sukunimi: String,
  vainOppijanumerorekisterissä: Boolean = false,
  maksuttomuusVoimassaAstiIänPerusteella: Option[LocalDate] = None
) extends ValpasHenkilöhakuResult {
  def ok = true
}

case class ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(
  oid: Option[ValpasHenkilö.Oid],
  hetu: Option[String],
  eiLainTaiMaksuttomuudenPiirissä: Boolean = true
) extends ValpasHenkilöhakuResult {
  def ok = false

  override def cleanUpForUserSearch: ValpasHenkilöhakuResult = this.copy(oid = None, hetu = None)
}

case class ValpasEiLöytynytHenkilöhakuResult() extends ValpasHenkilöhakuResult {
  def ok = false
}
