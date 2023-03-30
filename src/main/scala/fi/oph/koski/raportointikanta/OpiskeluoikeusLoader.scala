package fi.oph.koski.raportointikanta

import fi.oph.koski.db.DB
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.PäivitetytOpiskeluoikeudetJonoService
import rx.lang.scala.{Observable, Subscriber}

import java.sql.Timestamp
import java.time.{LocalDateTime, ZonedDateTime}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object OpiskeluoikeusLoader {
  val DefaultBatchSize = 500
}

trait OpiskeluoikeusLoader extends Logging {
  protected val statusName = "opiskeluoikeudet"
  protected val mitätöidytStatusName = "mitätöidyt_opiskeluoikeudet"

  // opiskeluOikeudenLatausEpäonnistui sisältö käytössä AWS hälytyksessä
  protected val opiskeluOikeudenLatausEpäonnistui = "Opiskeluoikeuden lataus epaonnistui"

  protected def db: RaportointiDatabase

  def loadOpiskeluoikeudet(): Observable[LoadResult]

  protected def setStatusStarted(dueTime: Option[Timestamp] = None) = {
    db.setStatusLoadStarted(statusName, dueTime)
    db.setStatusLoadStarted(mitätöidytStatusName, dueTime)
  }

  protected def setStatusCompleted() = {
    db.setStatusLoadCompleted(statusName)
    db.setStatusLoadCompleted(mitätöidytStatusName)
  }

  protected def createIndexesForIncrementalUpdate(): Unit = {
    val indexStartTime = System.currentTimeMillis
    logger.info("Luodaan osa indekseistä opiskeluoikeuksille...")
    db.createIndexesForIncrementalUpdate()
    val indexElapsedSeconds = (System.currentTimeMillis - indexStartTime)/1000
    logger.info(s"Luotiin osa indekseistä opiskeluoikeuksille, ${indexElapsedSeconds} s")
  }

  protected def createIndexes(): Unit = {
    val indexStartTime = System.currentTimeMillis
    logger.info("Luodaan indeksit opiskeluoikeuksille...")
    db.createOpiskeluoikeusIndexes
    val indexElapsedSeconds = (System.currentTimeMillis - indexStartTime)/1000
    logger.info(s"Luotiin indeksit opiskeluoikeuksille, ${indexElapsedSeconds} s")
  }

  protected def progressLogger: Subscriber[LoadResult] = new Subscriber[LoadResult] {
    val LoggingInterval = 5.minutes.toMillis
    val startTime = System.currentTimeMillis
    logger.info("Ladataan opiskeluoikeuksia...")

    var opiskeluoikeusCount = 0
    var suoritusCount = 0
    var errors = 0
    var lastLogged = System.currentTimeMillis
    override def onNext(r: LoadResult) = {
      r match {
        case LoadErrorResult(oid, error) =>
          logger.warn(s"$opiskeluOikeudenLatausEpäonnistui: $oid $error")
          errors += 1
        case LoadProgressResult(o, s) => {
          opiskeluoikeusCount += o
          suoritusCount += s
        }
        case LoadCompleted(_) =>
      }
      val now = System.currentTimeMillis
      if ((now - lastLogged) > LoggingInterval) {
        logIt(false)
        lastLogged = now
      }
    }
    override def onError(e: Throwable) {
      logger.error(e)("Opiskeluoikeuksien lataus epäonnistui")
    }
    override def onCompleted() {
      logIt(true)
    }
    private def logIt(done: Boolean) = {
      val elapsedSeconds = (System.currentTimeMillis - startTime) / 1000.0
      val rate = (opiskeluoikeusCount + errors) / Math.max(1.0, elapsedSeconds)
      logger.info(s"${if (done) "Ladattiin" else "Ladattu tähän mennessä"} $opiskeluoikeusCount opiskeluoikeutta, $suoritusCount suoritusta, $errors virhettä, ${(rate*60).round} opiskeluoikeutta/min")
    }
  }
}

sealed trait LoadResult
case class LoadErrorResult(oid: String, error: String) extends LoadResult
case class LoadProgressResult(opiskeluoikeusCount: Int, suoritusCount: Int) extends LoadResult
case class LoadCompleted(done: Boolean = true) extends LoadResult
