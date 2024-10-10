package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.koskiuser.KoskiSpecificAuthenticationSupport
import fi.oph.koski.servlet.{OmaOpintopolkuSupport, OppijaHtmlServlet}
import org.scalatra.{MatchedRoute, ScalatraServlet}

class OmaDataOAuth2ResourceOwnerReactServlet(implicit val application: KoskiApplication) extends ScalatraServlet
  with OppijaHtmlServlet with KoskiSpecificAuthenticationSupport with OmaOpintopolkuSupport with OmaDataOAuth2Support with OmaDataOAuth2Config {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/authorize")(nonce => {
    setLangCookieFromDomainIfNecessary
    val lang = langFromCookie.getOrElse(langFromDomain)

    if (multiParams("error").length > 0) {
      // Näytetään virhe riippumatta sisäänkirjautumisstatuksesta
      landerHtml(nonce)
    } else {
      (validateQueryParams(), isAuthenticated) match {
        case (Left(validationError), _) if validationError.reportingType == ReportingType.ToResourceOwner =>
          // Parametreissa havaittiin käyttäjälle rendattavia virheitä => redirectaa samaan routeen virhetietojen kanssa niiden näyttämiseksi
          logger.warn(validationError.getLoggedErrorMessage)

          redirect(s"/koski/omadata-oauth2/authorize?${getParamsWithError(validationError)}")
        case (Left(validationError), _) =>
          // Parametreissa havaittiin virheitä, jotka kuuluu raportoida redirect_uri:n kautta clientille asti
          // TODO: TOR-2210: toteuta
          logger.error(s"Internal error: ${validationError.loggedMessage}")
          halt(500)
        case (_, true) =>
          // Käyttäjä kirjautunut sisään, näytä sisältö
          landerHtml(nonce)
        case _ =>
          // Redirect CAS-kirjautumisen kautta
          val casLoginURL = getCasLoginURL(lang)
          redirect(casLoginURL)
      }
    }

    // ===> TODO: TOR-2210: Virheilmoitus redirect_uri:in:
    //    If the resource owner denies the access request or if the request
    //      fails for reasons other than a missing or invalid redirection URI,
    //    the authorization server informs the client by adding the following
    //      parameters to the query component of the redirection URI using the
    //    "application/x-www-form-urlencoded" format, per Appendix B:
  })

  private def getParamsWithError(validationError: ValidationError): String = {
    getCurrentURLParams match {
      case Some(existingParams) =>
        existingParams + s"&${validationError.getClientErrorParams}"
      case _ =>
        s"${validationError.getClientErrorParams}"
    }
  }

  private def getCasLoginURL(lang: String): String = {
    val targetUrl = (request.getRequestURI, getCurrentURLParams) match {
      case (_, Some(requestParamsNoEncoding)) =>
        // Käytä base64url-enkoodaus-workaroundia, koska URL sisälsi query-parametreja
        val noQueryParamsWorkaroundTarget = s"/koski/omadata-oauth2/cas-workaround/authorize/${base64UrlEncode(requestParamsNoEncoding)}"
        noQueryParamsWorkaroundTarget
      case (requestURI, _) =>
        // Suora redirect onnistuu, koska alkuperäisessa URLissa ei ole query-parametreja
        requestURI
    }

    val loginUrl = getLoginURL(targetUrl)

    conf.getString(s"login.cas.$lang") +
      conf.getString("login.cas.targetparam") + loginUrl +
      "&valtuudet=false" +
      getKorhopankkiRedirectURLParameter(targetUrl)
  }

  private def getLoginURL(target: String): String = {
    // CAS ei halua, että sille annettavaa URL:ia enkoodataan, siksi tässä ei sitä tehdä.
    s"${omaDataOAuth2LoginServletURL}?onSuccess=${target}"
  }

  private def omaDataOAuth2LoginServletURL: String =
    conf.getString("login.servlet")

  private def getKorhopankkiRedirectURLParameter(target: String): String = {
    val security = application.config.getString("login.security")

    if(security == "mock") {
      s"&redirect=${urlEncode(target)}"
    } else {
      ""
    }
  }

  private def getCurrentURLParams: Option[String] = {
    if (request.queryString.isEmpty) {
      None
    } else {
      Some(request.queryString)
    }
  }

  private def landerHtml(nonce: String) = {
    val paramNames = Seq("client_id", "response_type", "response_mode", "redirect_uri", "code_challenge", "code_challenge_method", "state", "scope", "error", "error_id")
    paramNames.foreach(n => logger.info(s"${n}: ${multiParams(n)}"))

    htmlIndex(
      scriptBundleName = "koski-omadataoauth2.js",
      responsive = true,
      nonce = nonce
    )
  }
}
