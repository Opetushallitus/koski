package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.koskiuser.KoskiSpecificAuthenticationSupport
import fi.oph.koski.servlet.{OmaOpintopolkuSupport, OppijaHtmlServlet}
import org.scalatra.{MatchedRoute, ScalatraServlet}

class OmaDataOAuth2ResourceOwnerReactServlet(implicit val application: KoskiApplication) extends ScalatraServlet
  with OppijaHtmlServlet with KoskiSpecificAuthenticationSupport with OmaOpintopolkuSupport with OmaDataOAuth2ServletSupport with OmaDataOAuth2Config {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/authorize")(nonce => {
    setLangCookieFromDomainIfNecessary
    val lang = langFromCookie.getOrElse(langFromDomain)

    val uri = request.getRequestURI
    val queryString = request.getQueryString

    // + -enkoodatut query-stringit rikkoutuvat redirecteissä, mutta esim. openid-client -OAuth2-kirjasto lähettää scopet +-enkoodattuina
    if (queryString.contains('+')) {
      redirect(s"$uri?${queryString.replace("+", "%20")}")
    } else if (multiParams("error").length > 0) {
      // Parametreissa välitettiin virheilmoitus, joten
      // näytetään virhe käyttäjälle riippumatta sisäänkirjautumisstatuksesta
      landerHtml(nonce)
    } else {
      validateQueryClientParams() match {
        case Left(validationError) =>
          redirectToSelfWithErrors(validationError)
        case Right(clientInfo) =>
          validateQueryOtherParams(clientInfo) match {
            case Left(validationError) =>
              sendErrorsInParamsToClient(isAuthenticated, clientInfo, validationError)
            case Right(paramInfo) if !isAuthenticated =>
              loginAndRedirectToSelf(lang)
            case Right(paramInfo) if isAuthenticated =>
              landerHtml(nonce)
          }
      }
    }
  })

  private def landerHtml(nonce: String) = {
    htmlIndex(
      scriptBundleName = "koski-omadataoauth2.js",
      responsive = true,
      nonce = nonce
    )
  }

  private def redirectToSelfWithErrors(validationError: OmaDataOAuth2Error) = {
    // Parametreissa oli käyttäjälle rendattavia virheitä => redirectaa samaan routeen virhetietojen kanssa niiden näyttämiseksi
    logger.warn(validationError.getLoggedErrorMessage)
    redirect(s"/koski/omadata-oauth2/authorize?${getParamsWithError(validationError)}")
  }

  private def sendErrorsInParamsToClient(isAuthenticated: Boolean, clientInfo: ClientInfo, validationError: OmaDataOAuth2Error) = {
    val paramsString = createParamsString(clientInfo.getPostResponseServletParams ++ validationError.getPostResponseServletParams)
    logger.warn(validationError.getLoggedErrorMessage)

    // Lähetä virheet logout-redirectin kautta vain, jos käyttäjä oli jo kirjautunut
    redirectToPostResponse(isAuthenticated && useLogoutBeforeRedirect(clientInfo.clientId), paramsString)
  }

  private def loginAndRedirectToSelf(lang: String) = {
    // Redirect CAS-kirjautumisen kautta
    val casLoginURL = getCasLoginURL(lang)
    redirect(casLoginURL)
  }

  private def getParamsWithError(validationError: OmaDataOAuth2Error): String = {
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
      s"&redirect=${queryStringUrlEncode(target)}"
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
}
