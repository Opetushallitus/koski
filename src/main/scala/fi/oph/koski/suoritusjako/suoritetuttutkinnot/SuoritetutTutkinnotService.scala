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

    val suoritetutTutkinnotOppija = haeOpiskeluoikeudet(oppijaOid)
      .map(teePalautettavaSuoritetutTutkinnotOppija)

    suoritetutTutkinnotOppija.map(sto => sto.opiskeluoikeudet.map(oo =>
      oo.oid.map(application.oppijaFacade.merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta)
    ))

    suoritetutTutkinnotOppija
  }

  case class RawOppija(
    henkilö: LaajatOppijaHenkilöTiedot,
    opiskeluoikeudet: Seq[SuoritetutTutkinnotOpiskeluoikeus],
    ylioppilastutkinnot: Seq[SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus],
    korkeakoulututkinnot: Seq[SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus],
  )

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

      val virtaResultFut: Future[Either[HttpStatus, Seq[SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus]]] = {
        masterHenkilöFut
          .map(_.flatMap(masterHenkilö =>
            try {
              Right(application.virta.findByOppija(masterHenkilö).map {
                case kk: schema.KorkeakoulunOpiskeluoikeus =>
                  SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus.fromKoskiSchema(kk)
              })
            } catch {
              case NonFatal(e) =>
                logger.warn(e)("Failed to fetch data from Virta")
                Left(KoskiErrorCategory.unavailable.virta())
            }
          ))
      }

      val opiskeluoikeudetFut: Future[Either[HttpStatus, Seq[SuoritetutTutkinnotOpiskeluoikeus]]] =
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
        virtaResult <- EitherT(virtaResultFut)
      } yield RawOppija(henkilö, opiskeluoikeudet, ytrResult, virtaResult)

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
    val opiskeluoikeudet = rawOppija.opiskeluoikeudet
    val ytrOpiskeluoikeudet = rawOppija.ylioppilastutkinnot
    val virtaOpiskeluoikeudet = rawOppija.korkeakoulututkinnot

    SuoritetutTutkinnotOppija(
      henkilö = Henkilo.fromOppijaHenkilö(rawOppija.henkilö),
      opiskeluoikeudet = suodataPalautettavat(opiskeluoikeudet ++ ytrOpiskeluoikeudet ++ virtaOpiskeluoikeudet).toList
    )
  }

  private def suodataPalautettavat(opiskeluoikeudet: Seq[SuoritetutTutkinnotOpiskeluoikeus]): Seq[SuoritetutTutkinnotOpiskeluoikeus] = {

    val kuoriOpiskeluoikeusOidit = opiskeluoikeudet.map(_.sisältyyOpiskeluoikeuteen.map(_.oid)).flatten.toSet

    opiskeluoikeudet
      .filterNot(onKuoriOpiskeluoikeus(kuoriOpiskeluoikeusOidit))
      .map(_.withoutSisältyyOpiskeluoikeuteen)
      .map { opiskeluoikeus =>
        opiskeluoikeus.withSuoritukset(
          opiskeluoikeus.suoritukset
            .map(poistaTutkintonimikeJaOsaamisalaTarvittaessa)
            .map(poistaAikaisemmatOsaamisalat)
            .filter(vahvistettuNykyhetkeenMennessä)
            .filter(josMuuAmmatillinenNiinTehtäväänValmistava)
            .filterNot(onKorotusSuoritus)
        )
      }.filter(_.suoritukset.nonEmpty)
  }

  private def onKuoriOpiskeluoikeus(kuoriOpiskeluoikeusOidit: Set[String])(o: SuoritetutTutkinnotOpiskeluoikeus): Boolean = {
    o.oid.map(kuoriOpiskeluoikeusOidit.contains).getOrElse(false)
  }

  private def poistaTutkintonimikeJaOsaamisalaTarvittaessa(s: Suoritus): Suoritus = {
    s match {
      case s: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus =>
        val tutkintonimike = if (s.toinenTutkintonimike.getOrElse(false)) {
          s.tutkintonimike
        } else {
          None
        }
        val osaamisala = if (s.toinenOsaamisala.getOrElse(false)) {
          s.osaamisala
        } else {
          None
        }
        s.copy(tutkintonimike = tutkintonimike, osaamisala = osaamisala)
      case _ => s
    }
  }

  def poistaAikaisemmatOsaamisalat(s: Suoritus): Suoritus = {
    s match {
      case s: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus =>
        s.withVainUusinOsaamisala
      case _ => s
    }
  }

  private def vahvistettuNykyhetkeenMennessä(s: Suoritus): Boolean = {
    s.vahvistus.exists(!_.päivä.isAfter(LocalDate.now))
  }

  private def josMuuAmmatillinenNiinTehtäväänValmistava(s: Suoritus): Boolean = {
    s match {
      case ms: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus
      => ms.koulutusmoduuli.tunniste.koodistoUri.contains("ammatilliseentehtavaanvalmistavakoulutus")
      case _ => true
    }
  }

  private def onKorotusSuoritus(s: Suoritus): Boolean = {
    s match {
      case ms: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus
        if ms.korotettuOpiskeluoikeusOid.isDefined => true
      case _ => false
    }
  }
}
