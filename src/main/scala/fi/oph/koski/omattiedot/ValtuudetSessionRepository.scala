package fi.oph.koski.omattiedot

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.ValtuudetSession
import fi.oph.koski.db.{DatabaseExecutionContext, KoskiDatabaseMethods, ValtuudetSessionRow}
import fi.oph.koski.log.Logging
import slick.dbio.Effect
import slick.sql.SqlAction

case class ValtuudetSessionRepository(db: DB) extends DatabaseExecutionContext with KoskiDatabaseMethods with Logging {
  def getAccessToken[T](session: ValtuudetSessionRow, valtuutusCode: String, newToken: String => Either[T, String]): Either[T, String] = runDbSync {
    (for {
      accessToken <- session.accessToken match {
        case None => fetchAndStoreAccessTokenAction(session.oppijaOid, valtuutusCode, newToken)
        case Some(token) => DBIO.successful(Right(token))
      }
    } yield accessToken).transactionally
  }

  def get(oppijaOid: String): Option[ValtuudetSessionRow] = runDbSync(getSessionAction(oppijaOid))

  def store(oppijaOid: String, valtuudetSessio: SessionResponse): ValtuudetSessionRow = {
    val row = mkRow(oppijaOid, valtuudetSessio)
    runDbSync(storeAction(row))
    logger.debug(s"Store $row")
    row
  }

  private def fetchAndStoreAccessTokenAction[T](oppijaOid: String, valtuutusCode: String, newToken: String => Either[T, String]) = for {
    token <- DBIO.successful(newToken(valtuutusCode))
    _ <- token match {
      case Left(status) => DBIO.successful(Left(status))
      case Right(t) => storeAccessTokenAndValtuutusCodeAction(oppijaOid, valtuutusCode, t)
    }
  } yield token


  private def getSessionAction(oppijaOid: String): SqlAction[Option[ValtuudetSessionRow], NoStream, Effect.Read] = {
    ValtuudetSession.filter(_.oppijaOid === oppijaOid).result.headOption
  }

  private def storeAccessTokenAndValtuutusCodeAction(oppijaOid: String, code: String, accessToken: String): DBIOAction[Int, NoStream, Effect.Write] = {
    ValtuudetSession.filter(_.oppijaOid === oppijaOid).map(r => (r.code, r.accessToken)).update((Some(code), Some(accessToken)))
  }

  private def storeAction(row: ValtuudetSessionRow): DBIOAction[ValtuudetSessionRow, NoStream, Effect.Write] = for {
    _ <- ValtuudetSession.insertOrUpdate(row)
  } yield row

  private def mkRow(oppijaOid: String, valtuudetSession: SessionResponse) = {
    ValtuudetSessionRow(oppijaOid = oppijaOid, sessionId = valtuudetSession.sessionId, userId = valtuudetSession.userId)
  }
}
