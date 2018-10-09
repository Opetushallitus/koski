package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.henkilo.PossiblyUnverifiedHenkilöOid
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{HenkilönTunnisteet, HenkilötiedotJaOid, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}
import fi.oph.koski.util.{Futures, WithWarnings}
import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluoikeusRow}
import fi.oph.koski.log.Logging

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class CompositeOpiskeluoikeusRepository(main: KoskiOpiskeluoikeusRepository, virta: AuxiliaryOpiskeluoikeusRepository, ytr: AuxiliaryOpiskeluoikeusRepository) extends GlobalExecutionContext with Logging {

  private val aux: List[AuxiliaryOpiskeluoikeusRepository] = List(virta, ytr)

  def filterOppijat(oppijat: List[HenkilötiedotJaOid])(implicit user: KoskiSession): List[HenkilötiedotJaOid] = {
    val found1 = main.filterOppijat(oppijat)
    val left1 = oppijat.diff(found1)
    val found2 = virta.filterOppijat(left1)
    val left2 = left1.diff(found2)
    val found3 = ytr.filterOppijat(left2)
    found1 ++ found2 ++ found3
  }

  def findByOid(oid: String)(implicit user: KoskiSession): Either[HttpStatus, OpiskeluoikeusRow] =
    main.findByOid(oid)

  def createOrUpdate(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowUpdate: Boolean, allowDeleteCompleted: Boolean = false)(implicit user: KoskiSession): Either[HttpStatus, CreateOrUpdateResult] =
    main.createOrUpdate(oppijaOid, opiskeluoikeus, allowUpdate, allowDeleteCompleted)

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

  def findByOppija(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSession): WithWarnings[Seq[Opiskeluoikeus]] = {
    val oid = tunnisteet.oid
    val virtaResultFuture = Future { virta.findByOppija(tunnisteet) }.transform(mapFailureToVirtaUnavailable(_, oid))
    val ytrResultFuture = Future { ytr.findByOppija(tunnisteet) }.transform(mapFailureToYtrUnavailable(_, oid))
    val mainResult = main.findByOppijaOid(oid)
    val virtaResult = Futures.await(virtaResultFuture)
    val ytrResult = Futures.await(ytrResultFuture)
    WithWarnings(mainResult ++ virtaResult.getIgnoringWarnings ++ ytrResult.getIgnoringWarnings, virtaResult.warnings ++ ytrResult.warnings)
  }

  def findByCurrentUser(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSession): WithWarnings[Seq[Opiskeluoikeus]] = {
    val oid = tunnisteet.oid
    val virtaResultFuture = Future { virta.findByCurrentUser(tunnisteet) }.transform(mapFailureToVirtaUnavailable(_, oid))
    val ytrResultFuture = Future { ytr.findByCurrentUser(tunnisteet) }.transform(mapFailureToYtrUnavailable(_, oid))
    val mainResult = main.findByCurrentUserOid(oid)
    val virtaResult = Futures.await(virtaResultFuture)
    val ytrResult = Futures.await(ytrResultFuture)
    WithWarnings(mainResult ++ virtaResult.getIgnoringWarnings ++ ytrResult.getIgnoringWarnings, virtaResult.warnings ++ ytrResult.warnings)
  }

  def getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSession): Either[HttpStatus, List[Oid]] =
    main.getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid)
}
