package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config

import java.time.LocalDate
import fi.oph.koski.db.KoskiOpiskeluoikeusRow
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.henkilo.{HenkilönTunnisteet, Hetu, LaajatOppijaHenkilöTiedot, PossiblyUnverifiedHenkilöOid}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}
import fi.oph.koski.turvakielto.TurvakieltoService
import fi.oph.koski.util.{Futures, WithWarnings}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class CompositeOpiskeluoikeusRepository(main: KoskiOpiskeluoikeusRepository, virta: AuxiliaryOpiskeluoikeusRepository, ytr: AuxiliaryOpiskeluoikeusRepository, config: Config) extends GlobalExecutionContext with Logging {

  val sallitutSynteettisetHetutVirtaHakuihin = config.getStringList("virta.sallitutSynteettisetHetut")

  private def withErrorLogging[T](fun: () => T)(implicit user: KoskiSpecificSession): T = {
    Try(fun()) match {
      case Success(s) => s
      case Failure(t) if t.isInstanceOf[java.lang.reflect.UndeclaredThrowableException] && Option(t.getCause).nonEmpty =>
        logger.error(t.getCause)(s"${t.getCause}: ${t.getMessage}")
        throw t
      case Failure(t) =>
        throw t
    }
  }

  def filterOppijat[A <: HenkilönTunnisteet](oppijat: List[A])(implicit user: KoskiSpecificSession): List[A] = {
    val found1 = main.filterOppijat(oppijat)
    val left1 = oppijat.diff(found1)
    val found2 = virta.filterOppijat(left1)
    val left2 = left1.diff(found2)
    val found3 = ytr.filterOppijat(left2)
    found1 ++ found2 ++ found3
  }

  def findByOid(oid: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, KoskiOpiskeluoikeusRow] =
    main.findByOid(oid)

  def createOrUpdate(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean = false,
  )(
    implicit user: KoskiSpecificSession
  ): Either[HttpStatus, CreateOrUpdateResult] =
    withErrorLogging(() => main.createOrUpdate(oppijaOid, opiskeluoikeus, allowUpdate, allowDeleteCompleted))

  def mapFailureToVirtaUnavailable(result: Try[Seq[Opiskeluoikeus]], oid: String): Try[WithWarnings[Seq[Opiskeluoikeus]]] = {
    result match {
      case Failure(exception) => logger.error(exception)(s"Failed to fetch Virta data for $oid")
      case Success(_) =>
    }
    Success(WithWarnings.fromTry(result, KoskiErrorCategory.unavailable.virta(), Nil))
  }

  def mapFailureToYtrUnavailable(result: Try[Seq[Opiskeluoikeus]], oid: String): Try[WithWarnings[Seq[Opiskeluoikeus]]] = {
    result match {
      case Failure(exception) => logger.error(exception)(s"Failed to fetch YTR data for $oid")
      case Success(_) =>
    }
    Success(WithWarnings.fromTry(result, KoskiErrorCategory.unavailable.ytr(), Nil))
  }

  def findByOppija(tunnisteet: HenkilönTunnisteet, useVirta: Boolean, useYtr: Boolean)(implicit user: KoskiSpecificSession): WithWarnings[Seq[Opiskeluoikeus]] = {
    val oid = tunnisteet.oid

    val mainResult = withErrorLogging(() => main.findByOppijaOids(tunnisteet.oid :: tunnisteet.linkitetytOidit))
    val ytrResultFuture = Future { if (useYtr) ytr.findByOppija(tunnisteet) else Nil }.transform(mapFailureToYtrUnavailable(_, oid))
    val ytrResult = Futures.await(ytrResultFuture)

    val virtaResultFuture = {
      // Vaikka Kosken testiympäristöissä synteettiset hetut sallitaan, Virrassa ne eivät toimi, joten käytetään Virran kanssa oideja
      val hetu = tunnisteet.hetu.getOrElse("")
      val isRealHetu = sallitutSynteettisetHetutVirtaHakuihin.contains(hetu) || (virtaHetuValidator.validate(hetu) match {
        case Right(_) => true
        case _ => false
      })

      Future { if (useVirta) virta.findByOppija(if (isRealHetu) { tunnisteet } else { tunnisteet.ilmanHetua }) else Nil }.transform(mapFailureToVirtaUnavailable(_, oid))
    }
    val virtaResult = Futures.await(virtaResultFuture)
    WithWarnings(mainResult ++ virtaResult.getIgnoringWarnings ++ ytrResult.getIgnoringWarnings, virtaResult.warnings ++ ytrResult.warnings)
  }

  private val virtaHetuValidator = new Hetu(acceptSyntheticHetus = false)

  def findByCurrentUser(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSpecificSession): WithWarnings[Seq[Opiskeluoikeus]] = {
    val oid = tunnisteet.oid
    val virtaResultFuture = Future { virta.findByCurrentUser(tunnisteet) }.transform(mapFailureToVirtaUnavailable(_, oid))
    val ytrResultFuture = Future { ytr.findByCurrentUser(tunnisteet) }.transform(mapFailureToYtrUnavailable(_, oid))
    val mainResult = main.findByCurrentUserOids(tunnisteet.oid :: tunnisteet.linkitetytOidit)
    val virtaResult = Futures.await(virtaResultFuture)
    val ytrResult = Futures.await(ytrResultFuture)
    WithWarnings(mainResult ++ virtaResult.getIgnoringWarnings ++ ytrResult.getIgnoringWarnings, virtaResult.warnings ++ ytrResult.warnings)
  }

  def findHuollettavaByOppija(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSpecificSession): WithWarnings[Seq[Opiskeluoikeus]] = {
    val oid = tunnisteet.oid
    val virtaResultFuture = Future { virta.findHuollettavaByOppija(tunnisteet) }.transform(mapFailureToVirtaUnavailable(_, oid))
    val ytrResultFuture = Future { ytr.findHuollettavaByOppija(tunnisteet) }.transform(mapFailureToYtrUnavailable(_, oid))
    val mainResult = main.findHuollettavaByOppijaOids(tunnisteet.oid :: tunnisteet.linkitetytOidit)
    val virtaResult = Futures.await(virtaResultFuture)
    val ytrResult = Futures.await(ytrResultFuture)

    val opiskeluoikeudet = mainResult ++ virtaResult.getIgnoringWarnings ++ ytrResult.getIgnoringWarnings
    val siivotutOpiskeluoikeudet = tunnisteet match {
      case h: LaajatOppijaHenkilöTiedot if h.turvakielto => opiskeluoikeudet.map(TurvakieltoService.poistaOpiskeluoikeudenTurvakiellonAlaisetTiedot)
      case _ => opiskeluoikeudet
    }

    WithWarnings(siivotutOpiskeluoikeudet, virtaResult.warnings ++ ytrResult.warnings)
  }

  def getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(oppijaOid: String): Seq[Päivämääräväli] = {
    main.getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(oppijaOid)
  }

  def getLukionOpiskeluoikeuksienAlkamisajatIlmanKäyttöoikeustarkistusta(
    oppijaOid: String,
    muutettavanOpiskeluoikeudenOid: Option[String]
  ): Seq[LocalDate] =
  {
    main.getLukionMuidenOpiskeluoikeuksienAlkamisajatIlmanKäyttöoikeustarkistusta(
      oppijaOid,
      muutettavanOpiskeluoikeudenOid
    )
  }

  def getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, List[Oid]] =
    main.getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid)

  def getMasterOppijaOidForOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, Oid] =
    main.getMasterOppijaOidForOpiskeluoikeus(opiskeluoikeusOid)

  def merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta(opiskeluoikeusOid: String) =
    main.merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta(opiskeluoikeusOid)

  def suoritusjakoTehtyIlmanKäyttöoikeudenTarkastusta(opiskeluoikeusOid: String): Boolean =
    main.suoritusjakoTehtyIlmanKäyttöoikeudenTarkastusta(opiskeluoikeusOid)
}
