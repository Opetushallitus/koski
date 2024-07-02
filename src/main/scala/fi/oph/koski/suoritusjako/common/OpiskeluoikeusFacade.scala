package fi.oph.koski.suoritusjako.common

import cats.data.EitherT
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema
import fi.oph.koski.util.Futures

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Future, TimeoutException}
import scala.reflect.runtime.universe.TypeTag
import scala.util.control.NonFatal

class OpiskeluoikeusFacade[OPISKELUOIKEUS: TypeTag](
  val application: KoskiApplication,
  val fetchYtrWithConverter: Option[schema.YlioppilastutkinnonOpiskeluoikeus => OPISKELUOIKEUS],
  val fetchVirtaWithConverter: Option[schema.KorkeakoulunOpiskeluoikeus => OPISKELUOIKEUS]
) extends Logging with GlobalExecutionContext {
  val opiskeluoikeusRepository = new OpiskeluoikeusRepository[OPISKELUOIKEUS](
    application.replicaDatabase.db,
    application.validatingAndResolvingExtractor
  )

  def haeOpiskeluoikeudet(
    oppijaOid: String,
    palautettavatOpiskeluoikeudenTyypit: Seq[String],
    useDownloadedYtr: Boolean = false
  )(
    implicit user: KoskiSpecificSession
  ): Either[HttpStatus, RawOppija[OPISKELUOIKEUS]] = {

    val notFoundResult = KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(
      "Oppijaa ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."
    )

    if (user.hasGlobalReadAccess) {
      val masterHenkilöFut: Future[Either[HttpStatus, LaajatOppijaHenkilöTiedot]] = Future(
        application.opintopolkuHenkilöFacade.findMasterOppija(oppijaOid)
          .toRight(notFoundResult)
      )

      val ytrResultFut: Future[Either[HttpStatus, Seq[OPISKELUOIKEUS]]] = {
        masterHenkilöFut
          .map(_.flatMap(masterHenkilö =>
            fetchYtrWithConverter match {
              case Some(converter) =>
                fetchYtrData(masterHenkilö, useDownloadedYtr, converter)
              case None =>
                Right(Seq.empty)
            }
          ))
      }

      val virtaResultFut: Future[Either[HttpStatus, Seq[OPISKELUOIKEUS]]] = {
        masterHenkilöFut
          .map(_.flatMap(masterHenkilö =>
            try {
              Right(fetchVirtaWithConverter match {
                case Some(converter) =>
                  application.virta.findByOppija(masterHenkilö).map {
                    case kk: schema.KorkeakoulunOpiskeluoikeus => converter(kk)
                  }
                case _ =>
                  Seq.empty
              })
            } catch {
              case NonFatal(e) =>
                logger.warn(e)("Failed to fetch data from Virta")
                Left(KoskiErrorCategory.unavailable.virta())
            }
          ))
      }

      val opiskeluoikeudetFut: Future[Either[HttpStatus, Seq[OPISKELUOIKEUS]]] =
        masterHenkilöFut
          .map(_.flatMap(masterHenkilö =>
            Right(opiskeluoikeusRepository.getOppijanKaikkiOpiskeluoikeudet(
              palautettavatOpiskeluoikeudenTyypit = palautettavatOpiskeluoikeudenTyypit,
              oppijaMasterOid = masterHenkilö.oid
            ))
          ))

      val rawOppija = for {
        henkilö <- EitherT(masterHenkilöFut)
        opiskeluoikeudet <- EitherT(opiskeluoikeudetFut)
        ytrResult <- EitherT(ytrResultFut)
        virtaResult <- EitherT(virtaResultFut)
      } yield RawOppija(henkilö, opiskeluoikeudet ++ ytrResult ++ virtaResult)

      try {
        Futures.await(
          future = rawOppija.value,
          atMost = if (Environment.isUnitTestEnvironment(application.config)) { 10.seconds } else { 5.minutes }
        )
      } catch {
        case _: TimeoutException => Left(KoskiErrorCategory.unavailable())
      }
    } else {
      Left(notFoundResult)
    }
  }


  def fetchYtrData(
    masterHenkilö: LaajatOppijaHenkilöTiedot,
    useDownloadedYtr: Boolean,
    converter: schema.YlioppilastutkinnonOpiskeluoikeus => OPISKELUOIKEUS
  ): Either[HttpStatus, Seq[OPISKELUOIKEUS]] = {
    try {
      val opiskeluoikeudet: Seq[schema.YlioppilastutkinnonOpiskeluoikeus] =
        if (useDownloadedYtr) {
          fetchDownloadedYtrOpiskeluoikeudet(masterHenkilö)
        } else {
          fetchYtrOpiskeluoikeudet(masterHenkilö)
        }

      Right(opiskeluoikeudet.map(converter))
    } catch {
      case NonFatal(e) =>
        logger.warn(e)("Failed to fetch data from YTR")
        Left(KoskiErrorCategory.unavailable.ytr())
    }
  }

  def fetchDownloadedYtrOpiskeluoikeudet(
    masterHenkilö: LaajatOppijaHenkilöTiedot
  ): Seq[schema.YlioppilastutkinnonOpiskeluoikeus] = {
    application.oppijaFacade.findYtrDownloadedOppija(
        masterHenkilö.oid,
        findMasterIfSlaveOid = true
      )(KoskiSpecificSession.systemUserTallennetutYlioppilastutkinnonOpiskeluoikeudet)
      .flatMap(_.warningsToLeft)
      .map(_.opiskeluoikeudet.collect { case yo: schema.YlioppilastutkinnonOpiskeluoikeus => yo })
      .getOrElse(fetchYtrOpiskeluoikeudet(masterHenkilö))
  }

  def fetchYtrOpiskeluoikeudet(
    masterHenkilö: LaajatOppijaHenkilöTiedot
  ): Seq[schema.YlioppilastutkinnonOpiskeluoikeus] = {
    application.ytr.findByOppija(masterHenkilö)(KoskiSpecificSession.systemUserTallennetutYlioppilastutkinnonOpiskeluoikeudet).collect {
      case yo: schema.YlioppilastutkinnonOpiskeluoikeus => yo
    }
  }
}

case class RawOppija[OPISKELUOIKEUS: TypeTag](
  henkilö: LaajatOppijaHenkilöTiedot,
  opiskeluoikeudet: Seq[OPISKELUOIKEUS]
)
