package fi.oph.koski.omadataoauth2

import fi.oph.koski.db.KoskiTables.{OAuth2Jako, OAuth2JakoKaikki}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DB, OAuth2JakoRow, QueryMethods}
import fi.oph.koski.log.Logging
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Security.sha256

import java.sql.Timestamp
import java.time.LocalDateTime

class OmaDataOAuth2Repository(val db: DB) extends Logging with QueryMethods {
  def getByCode(code: String): Option[OAuth2JakoRow] =
    runDbSync(OAuth2Jako.filter(r => r.codeSHA256 === sha256(code) && r.codeVoimassaAsti >= Timestamp.valueOf(LocalDateTime.now)).result.headOption)

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
}
