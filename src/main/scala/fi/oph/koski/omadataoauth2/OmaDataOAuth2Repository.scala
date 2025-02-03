package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiTables.{OAuth2Jako, OAuth2JakoKaikki}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.log.Logging
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Security.{generateSecret, sha256}

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}

class OmaDataOAuth2Repository(val application: KoskiApplication, val db: DB) extends DatabaseExecutionContext with OmaDataOAuth2Support with Logging with QueryMethods {
  def create(
    code: String,
    oppijaOid: String,
    clientId: String,
    scope: String,
    codeChallenge: String,
    redirectUri: String,
    tokenDurationMinutes: Int = 10

  ): Either[OmaDataOAuth2Error, Unit] = {

    val now = LocalDateTime.now()
    val codeVoimassaAsti = Timestamp.valueOf(now.plusMinutes(10))
    val voimassaAsti = Timestamp.valueOf(now.plusMinutes(tokenDurationMinutes))
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
        val error = OmaDataOAuth2Error(OmaDataOAuth2ErrorType.server_error, errorMessage)
        logger.error(t)(error.getLoggedErrorMessage)
        Left(error)
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
        // olla väliaikaisesti pielessä ja halutaan mahdollistaa korjaaminen ilman kaikkien suostumusten uudelleenluontia
        val warning = OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_scope, s"scope=${tooWideScopes.mkString(" ")} exceeds the rights granted to the client ${row.clientId}")
        logger.warn(warning.getLoggedErrorMessage)
        DBIO.successful(Left(warning))
      case Some(row) =>
        updateRow(
          rows = rows,
          accessTokenSHA256 = accessTokenAttemptSHA256.get
        ).flatMap {
          case 1 => DBIO.successful(Right(
            AccessTokenInfo(
              accessToken = accessTokenAttempt,
              expirationTime = row.voimassaAsti.toInstant,
              oppijaOid = row.oppijaOid,
              scope = row.scope
            )
          ))
          case _ => {
            val error = OmaDataOAuth2Error(OmaDataOAuth2ErrorType.server_error, "Internal error querying database")
            logger.error(error.getLoggedErrorMessage)
            DBIO.successful(Left(error))
          }
        }

    }

    runDbSync(updateAction.transactionally)
  }

  def getByAccessToken(
    accessToken: String,
    expectedClientId: String,
    allowedScopes: Set[String]
  ): Either[OmaDataOAuth2Error, AccessTokenInfo] = {
    val accessTokenSHA256 = sha256(accessToken)

    val maybeRow: Option[OAuth2JakoRow] = runDbSync(
      OAuth2Jako.filter(
        row =>
          row.accessTokenSHA256 === accessTokenSHA256 &&
            row.clientId === expectedClientId &&
            row.voimassaAsti >= Timestamp.valueOf(LocalDateTime.now)
      ).result.headOption
    )

    val validateError = maybeRow.flatMap(row => validateScope(row.scope).left.toOption)

    maybeRow match {
      case None =>
        Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_request, "Access token not found or it has expired"))
      case Some(_) if validateError.isDefined =>
        Left(validateError.get)
      case Some(row) if row.scope.split(" ").exists(scope => !allowedScopes.contains(scope)) =>
        val tooWideScopes = row.scope.split(" ").filterNot(allowedScopes.contains)
        val warning = OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_scope, s"scope=${tooWideScopes.mkString(" ")} exceeds the rights granted to the client ${row.clientId}")
        logger.warn(warning.getLoggedErrorMessage)
        Left(warning)
      case Some(row) =>
        Right(
          AccessTokenInfo(
            accessToken = accessToken,
            expirationTime = row.voimassaAsti.toInstant,
            oppijaOid = row.oppijaOid,
            scope = row.scope
          )
        )
    }
  }

  def getByCodeChallenge(codeChallenge: String, clientId: String): Option[OAuth2JakoRow] = {
    runDbSync(OAuth2JakoKaikki.filter(r => r.codeChallenge === codeChallenge && r.clientId === clientId).result.headOption)
  }

  def getBySHA256(codeSHA256: String): Option[OAuth2JakoRow] = {
    runDbSync(OAuth2Jako.filter(_.codeSHA256 === codeSHA256).result.headOption)
  }

  def invalidateBySHA256(codeSHA256: String, mitätöitySyy: String): Left[OmaDataOAuth2Error, Nothing] = {
    runDbSync(invalidateRow(OAuth2Jako.filter(_.codeSHA256 === codeSHA256), mitätöitySyy))
  }

  def getActiveRows(oppijaOids: Seq[String]): Seq[OAuth2JakoRow] = {
    runDbSync(OAuth2Jako.filter(row => row.oppijaOid.inSet(oppijaOids) && row.voimassaAsti >= Timestamp.valueOf(LocalDateTime.now)).result)
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
        case 1 =>
          val warning = OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_request, mitätöitySyy)
          logger.warn(warning.getLoggedErrorMessage)
          DBIO.successful(Left(warning))
        case _ =>
          val error = OmaDataOAuth2Error(OmaDataOAuth2ErrorType.server_error, "Internal error querying database")
          logger.error(error.getLoggedErrorMessage)
          DBIO.successful(Left(error))
      }
  }

  def deleteAllForOppija(oppijaOid: String): Int = {
    runDbSync(OAuth2JakoKaikki.filter(_.oppijaOid === oppijaOid).delete)
  }
}

case class AccessTokenInfo(
  accessToken: String,
  expirationTime: Instant,
  oppijaOid: String,
  scope: String,
)
