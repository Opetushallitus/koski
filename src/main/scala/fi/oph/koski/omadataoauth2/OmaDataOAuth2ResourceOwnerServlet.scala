package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{KoskiSpecificAuthenticationSupport, RequiresKansalainen}
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, LanguageSupport, NoCache}
import org.scalatra.ContentEncodingSupport


class OmaDataOAuth2ResourceOwnerServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with Logging with ContentEncodingSupport with NoCache with LanguageSupport with OmaDataOAuth2Support with RequiresKansalainen {

  get("/client-details/:client_id") {
    // TODO: TOR-2210: oikea toteutus
    renderObject(ClientDetails(params("client_id"), "Clientin selväkielinen nimi (TODO)"))
  }

  get("/authorize") {
    // TODO: TOR-2210: Siisti tämä koodi siten, että käyttää samaa patternia kuin fronttiserlvetissä
    if (multiParams("error").length > 0) {
      validateQueryClientParams() match {
        case Left(validationError) =>

          // sisäinen virhe, tänne ei pitäisi päätyä, koska client-parametrit olisi pitänyt jo validoida aiemmin
          // TODO: TOR-2210: tässä voisi kuitenkin palata fronttiin sisäisen virheilmoituksen kera? koska clientillekaan ei mitään tiedoteta
          logger.error(s"Internal error: ${validationError.errorDescription}")
          halt(500)
        case Right(clientInfo) =>
          logoutAndSendErrorsToClient(clientInfo)
      }
    } else {
      validateQueryClientParams() match {
        case Left(validationError) =>
          // TODO: TOR-2210 Parametreissa havaittiin käyttäjälle rendattavia virheitä => redirectaa takaisin fronttiin virhetietojen kera
          // näin ei pitäisi käydä, jos fronttikoodissa ei ole bugeja parametrien välityksessä
          logger.error(s"Internal error: ${validationError.errorDescription}")
          halt(500)
        case Right(clientInfo) =>
          validateQueryOtherParams() match {
            case Left(validationError) =>
              // TODO: TOR-2210 Parametreissa havaittiin virheitä, jotka kuuluu raportoida redirect_uri:n kautta clientille asti, redirectaa virhetietojen kanssa samaan osoitteeseen
              // näin ei pitäisi käydä, jos fronttikoodissa ei ole bugeja parametrien välityksessä
              logger.error(s"Internal error: ${validationError.errorDescription}")
              halt(500)
            case _ =>
              generateCodeAndSendViaLogoutToClient(clientInfo)
          }
      }
    }
  }

  private def logoutAndSendErrorsToClient(clientInfo: ClientInfo) = {
    val parameters =
      Seq(
        ("client_id", clientInfo.clientId),
        ("redirect_uri", clientInfo.redirectUri)
      ) ++
        clientInfo.state.toSeq.map(v => ("state", v)) ++
        Seq(("error", params("error"))) ++
        multiParams("error_description").headOption.toSeq.map(v => ("error_description", v)) ++
        multiParams("error_uri").headOption.toSeq.map(v => ("error_uri", v))

    val postResponseParams = createParamsString(parameters)

    redirectToPostResponseViaLogout(postResponseParams)
  }

  private def generateCodeAndSendViaLogoutToClient(clientInfo: ClientInfo) = {
    // TODO: TOR-2210: luo authorization code, tallenna code_challenge yms. sen yhteyteen

    // Parametrit ok, välitä post-responsen ja logoutin kautta tiedot clientille
    val parameters = Seq(
      ("client_id", clientInfo.clientId),
      ("redirect_uri", clientInfo.redirectUri),
    ) ++
      clientInfo.state.toSeq.map(v => ("state", v)) ++
      Seq(
        ("code", "foobar")
      )

    val postResponseParams = createParamsString(parameters)

    redirectToPostResponseViaLogout(postResponseParams)
  }
}

case class ClientDetails(
  id: String,
  name: String // TODO: TOR-2210 lokalisoitu merkkijono, josta frontti päättää minkä kielen rendaa?
)
