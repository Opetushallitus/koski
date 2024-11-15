package fi.oph.koski.omadataoauth2

import fi.oph.koski.db.KoskiTables.{OAuth2Jako, OAuth2JakoKaikki}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DB, DatabaseExecutionContext, KoskiTables, OAuth2JakoRow, QueryMethods}
import fi.oph.koski.log.Logging
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Security.{generateSecret, sha256}

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class OmaDataOAuth2Repository(val db: DB) extends DatabaseExecutionContext with Logging with QueryMethods {
  def getByAccessToken(accessToken: String): Option[OAuth2JakoRow] =
    runDbSync(OAuth2Jako.filter(r => r.accessTokenSHA256 === sha256(accessToken) && r.voimassaAsti >= Timestamp.valueOf(LocalDateTime.now)).result.headOption)

  def create(
    code: String,
    oppijaOid: String,
    clientId: String,
    scope: String,
    codeChallenge: String,
    redirectUri: String
  ): Either[OmaDataOAuth2Error, Unit] = {
    // TODO: TOR-2210 Voimassaoloajat konffattavaksi, client-kohtaisesti?
    val codeVoimassaAsti = Timestamp.valueOf(LocalDateTime.now().plusSeconds(10 * 60))
    val voimassaAsti = codeVoimassaAsti

    val codeSHA256 = sha256(code)

    try {
      runDbSync(OAuth2JakoKaikki += OAuth2JakoRow(
        codeSHA256,
        oppijaOid,
        clientId,
        scope,
        codeChallenge,
        redirectUri,
        accessTokenSHA256 = None,
        codeVoimassaAsti,
        voimassaAsti,
        luotu = new Timestamp(0), // Will be replaced by db trigger (see V100__create_oauth2_jako.sql)
        muokattu = new Timestamp(0), // Will be replaced by db trigger (see V100__create_oauth2_jako.sql)
        mitätöity = false,
        mitätöitySyy = None
      ))
      Right(Unit)
    } catch {
      case t:Throwable =>
        val errorMessage = s"Failed to add authorization for ${clientId} to access student info of ${oppijaOid}"
        logger.error(t)(errorMessage)
        Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.server_error, errorMessage))
    }
  }

  // Jos parametrit ja muu kunnossa, niin joko lisää tietokantaan ja palauttaa access tokenin, tai vaihtoehtoisesti
  // mitätöi tarvittaessa koko authorization code:n potentiaalisen tietoturvahyökkäyksen takia.
  def createAccessTokenForCode(
    code: String,
    expectedClientId: String,
    expectedCodeChallenge: String,
    expectedRedirectUri: Option[String],
    allowedScopes: Set[String]
  ): Either[OmaDataOAuth2Error, AccessTokenInfo] = {
    val codeSHA256 = sha256(code)
    val accessTokenAttempt = generateSecret
    val accessTokenAttemptSHA256: Option[String] = Some(sha256(accessTokenAttempt))

    val rows = OAuth2Jako.filter(
      row =>
        row.codeSHA256 === codeSHA256 &&
          row.clientId === expectedClientId &&
          row.codeVoimassaAsti >= Timestamp.valueOf(LocalDateTime.now)
    )

    val updateAction = rows.result.headOption.flatMap {
      case None =>
        DBIO.successful(Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_request, "Code not found or it has expired")))
      case Some(row) if row.codeChallenge != expectedCodeChallenge =>
        invalidateRow(rows, "Attempted use of invalid code_verifier")
      case Some(row) if row.accessTokenSHA256.isDefined == true =>
        invalidateRow(rows, "Attempted authorization code reuse")
      case Some(row) if Some(row.redirectUri) != expectedRedirectUri =>
        invalidateRow(rows, "Attempted use of non-matching redirect_uri")
      case Some(row) if row.scope.split(" ").exists(scope => !allowedScopes.contains(scope)) =>
        val tooWideScopes = row.scope.split(" ").filterNot(allowedScopes.contains)
        // Code:a ei tässä tapauksessa suljeta kokonaan, koska scopea säädetään käyttöoikeuksien kautta, ja ne saattavat esim.
        // olla väliaikaisesti pielessä ja halutaan mahdollistaa korjaaminen ilman kaikkian suostumusten uudelleenluontia
        DBIO.successful(Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_scope, s"scope=${tooWideScopes.mkString(" ")} exceeds the rights granted to the client ${row.clientId}")))
      case Some(row)  =>
        updateRow(
          rows = rows,
          accessTokenSHA256 = accessTokenAttemptSHA256.get
        ).flatMap {
          case 1 => DBIO.successful(Right(
            AccessTokenInfo(
              AccessTokenSuccessResponse(
                accessTokenAttempt,
                "Bearer",
                LocalDateTime.now.until(row.voimassaAsti.toLocalDateTime, ChronoUnit.SECONDS).max(0)
              ),
              row.oppijaOid,
              row.scope
            )
          ))
          case _ => DBIO.successful(Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.server_error, "Internal error querying database")))
        }

    }

    runDbSync(updateAction.transactionally)
  }

  private def updateRow(
    rows: Query[KoskiTables.OAuth2JakoTable, OAuth2JakoRow, Seq],
    accessTokenSHA256: String
  ) = {
    rows
      .map(_.accessTokenSHA256)
      .update(Option(accessTokenSHA256))
  }

  private def invalidateRow(
    rows: Query[KoskiTables.OAuth2JakoTable, OAuth2JakoRow, Seq],
    mitätöitySyy: String
  ) = {
    rows
      .map(r => (r.mitätöity, r.mitätöitySyy))
      .update(
        (true, Option(mitätöitySyy))
      )
      .flatMap {
        case 1 => DBIO.successful(Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_request, mitätöitySyy)))
        case _ => DBIO.successful(Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.server_error, "Internal error querying database")))
      }
  }
}

case class AccessTokenInfo(
  successResponse: AccessTokenSuccessResponse,
  oppijaOid: String,
  scope: String,
)
