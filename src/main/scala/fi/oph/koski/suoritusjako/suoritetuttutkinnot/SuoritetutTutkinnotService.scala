package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.schema
import fi.oph.koski.util.Futures

import java.time.LocalDate
import scala.concurrent.{Future, TimeoutException}
import scala.concurrent.duration.DurationInt
import scala.util.control.NonFatal
import cats.data.EitherT

class SuoritetutTutkinnotService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val suoritetutTutkinnotOpiskeluoikeusRepository = new SuoritetutTutkinnotOpiskeluoikeusRepository(
    application.replicaDatabase.db,
    application.validatingAndResolvingExtractor
  )

  def findSuoritetutTutkinnotOppija(oppijaOid: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, SuoritetutTutkinnotOppija] = {

    haeOpiskeluoikeudet(oppijaOid)
      .map(teePalautettavaSuoritetutTutkinnotOppija)
  }

  case class RawOppija(
    henkilö: LaajatOppijaHenkilöTiedot,
    opiskeluoikeudet: Seq[SuoritetutTutkinnotOppijanOpiskeluoikeusRow],
    ylioppilastutkinnot: Seq[SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus]
  )

  // TODO: TOR-1025 Lisää Virta-opiskeluoikeuksien nouto
  private def haeOpiskeluoikeudet(oppijaOid: String)(
    implicit user: KoskiSpecificSession
  ): Either[HttpStatus, RawOppija] = {
    val notFoundResult = KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(
      "Oppijaa ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."
    )

    if (user.hasGlobalReadAccess) {
      val masterHenkilöFut: Future[Either[HttpStatus, LaajatOppijaHenkilöTiedot]] = Future(
        application.opintopolkuHenkilöFacade.findMasterOppija(oppijaOid)
          .toRight(notFoundResult)
      )

      val ytrResultFut: Future[Either[HttpStatus, Seq[SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus]]] = {
        masterHenkilöFut
          .map(_.flatMap(masterHenkilö =>
            try {
              Right(application.ytr.findByOppija(masterHenkilö).map {
                case yo: schema.YlioppilastutkinnonOpiskeluoikeus =>
                  SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema(yo)
              })
            } catch {
              case NonFatal(e) =>
                logger.warn(e)("Failed to fetch data from YTR")
                Left(KoskiErrorCategory.unavailable.ytr())
            }
          ))
      }

      val opiskeluoikeudetFut: Future[Either[HttpStatus, Seq[SuoritetutTutkinnotOppijanOpiskeluoikeusRow]]] =
        masterHenkilöFut
          .map(_.flatMap(masterHenkilö =>
            Right(suoritetutTutkinnotOpiskeluoikeusRepository.getOppijanKaikkiOpiskeluoikeudet(
              palautettavatOpiskeluoikeudenTyypit = SuoritetutTutkinnotSchema.schemassaTuetutOpiskeluoikeustyypit,
              oppijaMasterOid = masterHenkilö.oid
            ))
          ))

      val rawOppija = for {
        henkilö <- EitherT(masterHenkilöFut)
        opiskeluoikeudet <- EitherT(opiskeluoikeudetFut)
        ytrResult <- EitherT(ytrResultFut)
      } yield RawOppija(henkilö, opiskeluoikeudet, ytrResult)

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

  private def teePalautettavaSuoritetutTutkinnotOppija(
    rawOppija: RawOppija
  ): SuoritetutTutkinnotOppija = {
    val opiskeluoikeudet = rawOppija.opiskeluoikeudet.map(_.opiskeluoikeus)
    val ytrOpiskeluoikeudet = rawOppija.ylioppilastutkinnot

    SuoritetutTutkinnotOppija(
      henkilö = Henkilo.fromOppijaHenkilö(rawOppija.henkilö),
      opiskeluoikeudet = suodataPalautettavat(opiskeluoikeudet ++ ytrOpiskeluoikeudet).toList
    )
  }

  private def suodataPalautettavat(opiskeluoikeudet: Seq[SuoritetutTutkinnotOpiskeluoikeus]): Seq[SuoritetutTutkinnotOpiskeluoikeus] = {
    def vahvistettuNykyhetkeenMennessä(s: Suoritus): Boolean = {
      s.vahvistus.exists(!_.päivä.isAfter(LocalDate.now))
    }

    def josMuuAmmatillinenNiinTehtäväänValmistava(s: Suoritus): Boolean = {
      s match {
        case ms: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus
          => ms.koulutusmoduuli.tunniste.koodistoUri.contains("ammatilliseentehtavaanvalmistavakoulutus")
        case _ => true
      }
    }

    def eiKorotusSuoritus(s: Suoritus): Boolean = {
      s match {
        case ms: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus
          if ms.korotettuOpiskeluoikeusOid.isDefined => false
        case _ => true
      }
    }

    opiskeluoikeudet.map { opiskeluoikeus =>
      opiskeluoikeus.withSuoritukset(
        opiskeluoikeus.suoritukset
          .filter(vahvistettuNykyhetkeenMennessä)
          .filter(josMuuAmmatillinenNiinTehtäväänValmistava)
          .filter(eiKorotusSuoritus)
      )
    }.filter(_.suoritukset.nonEmpty)
  }
}
