package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.henkilo.PossiblyUnverifiedHenkilöOid
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{HenkilötiedotJaOid, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}
import fi.oph.koski.util.{Futures, WithWarnings}
import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluoikeusRow}
import fi.oph.koski.log.Logging

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class CompositeOpiskeluoikeusRepository(main: KoskiOpiskeluoikeusRepository, virta: AuxiliaryOpiskeluoikeusRepository, ytr: AuxiliaryOpiskeluoikeusRepository) extends GlobalExecutionContext with Logging {

  private val aux: List[AuxiliaryOpiskeluoikeusRepository] = List(virta, ytr)

  def filterOppijat(oppijat: List[HenkilötiedotJaOid])(implicit user: KoskiSession): List[HenkilötiedotJaOid] = {
    (main :: aux).foldLeft((oppijat, ListBuffer.empty[HenkilötiedotJaOid])) { case ((left, found), repo) =>
      val newlyFound = repo.filterOppijat(left)
      val stillLeft = left.diff(newlyFound)
      (stillLeft, found ++= newlyFound)
    }._2.toList
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

  def findByOppijaOid(oid: String)(implicit user: KoskiSession): WithWarnings[Seq[Opiskeluoikeus]] = {
    val virtaResultFuture = Future { virta.findByOppijaOid(oid) }.transform(mapFailureToVirtaUnavailable(_, oid))
    val ytrResultFuture = Future { ytr.findByOppijaOid(oid) }.transform(mapFailureToYtrUnavailable(_, oid))
    val mainResult = main.findByOppijaOid(oid)
    val virtaResult = Futures.await(virtaResultFuture)
    val ytrResult = Futures.await(ytrResultFuture)
    WithWarnings(mainResult ++ virtaResult.getIgnoringWarnings ++ ytrResult.getIgnoringWarnings, virtaResult.warnings ++ ytrResult.warnings)
  }

  def findByUserOid(oid: String)(implicit user: KoskiSession): WithWarnings[Seq[Opiskeluoikeus]] = {
    val virtaResultFuture = Future { virta.findByUserOid(oid) }.transform(mapFailureToVirtaUnavailable(_, oid))
    val ytrResultFuture = Future { ytr.findByUserOid(oid) }.transform(mapFailureToYtrUnavailable(_, oid))
    val mainResult = main.findByUserOid(oid)
    val virtaResult = Futures.await(virtaResultFuture)
    val ytrResult = Futures.await(ytrResultFuture)
    WithWarnings(mainResult ++ virtaResult.getIgnoringWarnings ++ ytrResult.getIgnoringWarnings, virtaResult.warnings ++ ytrResult.warnings)
  }

  def getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSession): Either[HttpStatus, List[Oid]] =
    main.getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid)
}
