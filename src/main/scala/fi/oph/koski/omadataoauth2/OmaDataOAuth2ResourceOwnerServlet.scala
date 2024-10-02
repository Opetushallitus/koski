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
    // TODO: TOR-2210:
    // - parsi ja tarkista parametrit kunnolla, tarkista, ettei riko speksejä (esim. duplikaatteja)
    // - tarkista redirect_uri, että on sallittujen listalla
    // - luo authorization code, tallenna code_challenge yms. sen yhteyteen

    val paramNames = Seq("client_id", "response_type", "response_mode", "redirect_uri", "code_challenge", "code_challenge_method", "state", "scope", "error", "error_id", "error_description", "error_uri")
    paramNames.foreach(n => logger.info(s"${n}: ${multiParams(n)}"))

    if (multiParams("error").length > 0) {
      validateQueryClientParams() match {
        case Left(validationError) =>
          // sisäinen virhe, tänne ei pitäisi päätyä, koska client-parametrit olisi pitänyt jo validoida aiemmin
          // TODO: TOR-2210: tässä voisi kuitenkin palata fronttiin sisäisen virheilmoituksen kera? koska clientillekaan ei mitään tiedoteta
          logger.error(s"Internal error: ${validationError.loggedMessage}")
          halt(500)
        case Right(ClientInfo(clientId, redirectUri)) =>

          val parameters =
            Seq(
              ("client_id", clientId),
              ("redirect_uri", redirectUri)
            )  ++
            multiParams("state").headOption.toSeq.map(v => ("state", v)) ++
            Seq(("error", params("error"))) ++
            multiParams("error_description").headOption.toSeq.map(v => ("error_description", v)) ++
            multiParams("error_uri").headOption.toSeq.map(v => ("error_uri", v))

          val postResponseParams = createParamsString(parameters)

          redirectToPostResponseViaLogout(postResponseParams)
      }
    } else {
      validateQueryParams() match {
        case Left(validationError) if validationError.reportingType == ReportingType.ToResourceOwner =>
          // Parametreissa havaittiin käyttäjälle rendattavia virheitä => redirectaa takaisin fronttiin virhetietojen kera
          // TODO: TOR-2210
          logger.error(s"Internal error: ${validationError.loggedMessage}")
          halt(500)
        case Left(validationError) =>
          // Parametreissa havaittiin virheitä, jotka kuuluu raportoida redirect_uri:n kautta clientille asti, redirectaa virhetietojen kanssa samaan osoitteeseen
          // TODO: TOR-2210
          logger.error(s"Internal error: ${validationError.loggedMessage}")
          halt(500)
        case Right(ClientInfo(clientId, redirectUri)) =>
          // Parametrit ok, välitä post-responsen ja logoutin kautta tiedot clientille

          val parameters = Seq(
              ("client_id", clientId),
              ("redirect_uri", redirectUri),
            ) ++
              multiParams("state").headOption.toSeq.map(v => ("state", v)) ++
            Seq(
              ("code", "foobar")
            )

          val postResponseParams = createParamsString(parameters)

          redirectToPostResponseViaLogout(postResponseParams)
       }
      }
    }

  private def redirectToPostResponseViaLogout(postResponseParams: String) = {
    val postResponseParamsBase64UrlEncoded = base64UrlEncode(postResponseParams)
    val logoutRedirect = s"/koski/omadata-oauth2/cas-workaround/post-response/${postResponseParamsBase64UrlEncoded}"
    val logoutUri = s"/koski/user/logout?target=${logoutRedirect}"

    redirect(logoutUri)
  }
}

case class ClientDetails(
  id: String,
  name: String // TODO: TOR-2210 lokalisoitu merkkijono, josta frontti päättää minkä kielen rendaa?
)
