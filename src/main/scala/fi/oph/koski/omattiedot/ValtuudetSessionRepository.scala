package fi.oph.koski.omattiedot

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.ValtuudetSession
import fi.oph.koski.db.{DatabaseExecutionContext, KoskiDatabaseMethods, ValtuudetSessionRow}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging
import fi.vm.sade.suomifi.valtuudet.SessionDto
import slick.dbio.Effect
import slick.sql.SqlAction

import scala.util.Try

case class ValtuudetSessionRepository(db: DB) extends DatabaseExecutionContext with KoskiDatabaseMethods with Logging {
  def getAccessToken(oppijaOid: String, valtuutusCode: String, newToken: () => String): Either[HttpStatus, String] = runDbSync {
    (for {
      session <- getSessionAction(oppijaOid)
      accessToken <- session.flatMap(_.accessToken) match {
        case None => fetchAndStoreAccessTokenAction(oppijaOid, valtuutusCode, newToken)
        case Some(token) => DBIO.successful(Right(token))
      }
    } yield accessToken).transactionally
  }

  def get(oppijaOid: String): Option[ValtuudetSessionRow] = runDbSync(getSessionAction(oppijaOid))

  def store(oppijaOid: String, valtuudetSessio: SessionDto): ValtuudetSessionRow = {
    val row = mkRow(oppijaOid, valtuudetSessio)
    runDbSync(storeAction(row))
    logger.debug(s"Store $row")
    row
  }

  private def fetchAndStoreAccessTokenAction(oppijaOid: String, valtuutusCode: String, newToken: () => String) = for {
    token <- DBIO.successful(fetchAccessToken(valtuutusCode, newToken))
    _ <- token match {
      case Left(status) => DBIO.successful(Left(status))
      case Right(t) => storeAccessTokenAction(oppijaOid, valtuutusCode, t)
    }
  } yield token

  private def fetchAccessToken(valtuutusCode: String, newToken: () => String) =
    Try(newToken()).toEither.left.map { e =>
      logger.error(s"Virhe haettaessa accessTokenia koodilla $valtuutusCode: ${e.getMessage}")
      KoskiErrorCategory.unavailable.suomifivaltuudet()
    }

  private def getSessionAction(oppijaOid: String): SqlAction[Option[ValtuudetSessionRow], NoStream, Effect.Read] = {
    ValtuudetSession.filter(_.oppijaOid === oppijaOid).result.headOption
  }

  private def storeAccessTokenAction(oppijaOid: String, code: String, accessToken: String): DBIOAction[Int, NoStream, Effect.Write] = {
    ValtuudetSession.filter(_.oppijaOid === oppijaOid).map(r => (r.code, r.accessToken)).update((Some(code), Some(accessToken)))
  }

  private def storeAction(row: ValtuudetSessionRow): DBIOAction[ValtuudetSessionRow, NoStream, Effect.Write] = for {
    _ <- ValtuudetSession.insertOrUpdate(row)
  } yield row

  private def mkRow(oppijaOid: String, valtuudetSession: SessionDto) = {
    ValtuudetSessionRow(oppijaOid = oppijaOid, sessionId = valtuudetSession.sessionId, userId = valtuudetSession.userId)
  }
}
