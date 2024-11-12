package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koodisto.KoodistoViite
import fi.oph.koski.koskiuser.{KoskiSpecificAuthenticationSupport, RequiresKansalainen}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, LanguageSupport, NoCache}
import org.scalatra.ContentEncodingSupport


class OmaDataOAuth2ResourceOwnerServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with Logging with ContentEncodingSupport with NoCache with LanguageSupport with OmaDataOAuth2Support with RequiresKansalainen {

  get("/client-details/:client_id") {
    val clientId = params("client_id")
    renderObject(ClientDetails(clientId,
      application.koodistoPalvelu.getKoodistoKoodit(application.koodistoPalvelu.getLatestVersionRequired("omadataoauth2client"))
        .find(_.koodiArvo == clientId)
        .flatMap(_.nimi)
        .getOrElse(LocalizedString.unlocalized(clientId))
    ))
  }

  // TODO: TOR-2210: Pitäisikö tämän olla POST-route, ja vasta frontendissä tehdä redirect? Kuten HSL-käyttöliittymässä tehdään, mikä toki on monimutkaisempaa.
  //
  // Tällöin kumppanin palvelu (tai siinä palvelussa oleva tietoturva-aukko) ei pystyisi "kikkailemaan" ja ohjaamaan valmiiksi kirjautuneen käyttäjän
  // selainta suoraan tähän API-osoitteeseen hyväksyntäkäyttöliittymän ohi. Onko tätä tarvetta estää?
  //
  // oppija-CAS-loginin kautta tähän GET-routeen ei voi kuitenkaan suoraa linkkiä voi tehdä, koska CAS estää query-parametrit omassa uudelleenohjauksen käsittelyssään.
  // Eli ulkopuolinen taho ei pysty kuitenkaan muodostamaan tällä hetkellä URL-osoitetta, joka veisi käyttäjän CAS-kirjautumisen kautta suoraan tähän ilman hyväksyntä-käyttöliittymän
  // avaamista.
  get("/authorize") {
    if (multiParams("error").length > 0) {
      validateQueryClientParams() match {
        case Left(validationError) =>
          // .error toistaiseksi, koska tätä virhettä ei yleisesti pitäisi tapahtua, jos clientin fronttikoodissa ei ole bugeja
          logger.error(s"${validationError.getLoggedErrorMessage}: original error: ${multiParams("error").headOption.getOrElse("")}")

          logoutAndRedirectWithErrorsToResourceOwnerFrontend(validationError.getClientErrorParams)
        case Right(clientInfo) =>
          logoutAndSendErrorsInParamsToClient(clientInfo)
      }
    } else {
      validateQueryClientParams() match {
        case Left(validationError) =>
          // .error toistaiseksi, koska tätä virhettä ei yleisesti pitäisi tapahtua, jos clientin fronttikoodissa ei ole bugeja
          logger.error(validationError.getLoggedErrorMessage)

          logoutAndRedirectWithErrorsToResourceOwnerFrontend(validationError.getClientErrorParams)
        case Right(clientInfo) =>
          validateQueryOtherParams(clientInfo) match {
            case Left(validationError) =>
              // .error toistaiseksi, koska tätä virhettä ei yleisesti pitäisi tapahtua, jos clientin fronttikoodissa ei ole bugeja
              logger.error(validationError.getLoggedErrorMessage)

              logoutAndRedirectWithErrorsToResourceOwnerFrontend(validationError.getClientErrorParams)
            case Right(paramInfo) =>
              generateCodeAndSendViaLogoutToClient(clientInfo, paramInfo.scope, paramInfo.codeChallenge)
          }
      }
    }
  }

  private def logoutAndSendErrorsInParamsToClient(clientInfo: ClientInfo) = {
    logoutAndSendErrorToClient(
      clientInfo,
      params("error"),
      multiParams("error_description").headOption,
      multiParams("error_uri").headOption
    )
  }

  private def logoutAndSendErrorToClient(
    clientInfo: ClientInfo,
    error: String,
    errorDescription: Option[String] = None,
    errorUri: Option[String] = None
  ) = {
    val parameters =
      Seq(
        ("client_id", clientInfo.clientId),
        ("redirect_uri", clientInfo.redirectUri)
      ) ++
        clientInfo.state.toSeq.map(v => ("state", v)) ++
        Seq(("error", error)) ++
        errorDescription.toSeq.map(v => ("error_description", v)) ++
        errorUri.toSeq.map(v => ("error_uri", v))

    val postResponseParams = createParamsString(parameters)

    redirectToPostResponseViaLogout(postResponseParams)
  }

  private def generateCodeAndSendViaLogoutToClient(clientInfo: ClientInfo, scope: String, codeChallenge: String) = {
    application.omaDataOAuth2Service.create(
      clientId = clientInfo.clientId,
      scope,
      codeChallenge,
      redirectUri = clientInfo.redirectUri,
      koskiSession = session
    ) match {
      case Right(code) =>
        val parameters = Seq(
          ("client_id", clientInfo.clientId),
          ("redirect_uri", clientInfo.redirectUri),
        ) ++
          clientInfo.state.toSeq.map(v => ("state", v)) ++
          Seq(
            ("code", code)
          )
        val postResponseParams = createParamsString(parameters)
        redirectToPostResponseViaLogout(postResponseParams)

      case Left(error) =>
        logoutAndSendErrorToClient(
          clientInfo,
          error.errorType.toString,
          Some(error.errorDescription),
          None
        )
    }
  }
}

case class ClientDetails(
  id: String,
  name: LocalizedString
)
