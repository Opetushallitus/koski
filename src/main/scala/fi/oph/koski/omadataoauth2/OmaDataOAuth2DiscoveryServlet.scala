package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoViite}
import fi.oph.koski.koskiuser.{Unauthenticated}
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.{ContentEncodingSupport}

class OmaDataOAuth2DiscoveryServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet
  with Logging with ContentEncodingSupport with NoCache with Unauthenticated {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/") {

    val opintopolkuBaseUrl = application.config.getString("opintopolku.oppija.url") match {
      case "mock" =>
        val url = request.getRequestURL
        val uri = request.getRequestURI
        url.substring(0, url.indexOf(uri))
      case url => url
    }
    val luovutuspalveluBaseUrl = application.config.getString("omadataoauth2.luovutuspalveluBaseUrl") match {
      case "mock" =>
        "https://localhost:7022"
      case url => url
    }

    val metadata = OAuth2ProviderMetadata(
      issuer = opintopolkuBaseUrl + "/koski/omadata-oauth2",
      authorization_endpoint = opintopolkuBaseUrl + "/koski/omadata-oauth2/authorize",
      scopes_supported = findKoodisto("omadataoauth2scope").map(_.koodiArvo.toUpperCase()).sorted,
      token_endpoint = luovutuspalveluBaseUrl + "/koski/api/omadata-oauth2/authorization-server",
      service_documentation = opintopolkuBaseUrl + "/koski/dokumentaatio/rajapinnat/oauth2/omadata"
    )

    render(metadata)
  }

  def findKoodisto(koodistoUri: String, versioNumero: Option[String] = None): Seq[KoodistoKoodi] = {
    val versio: Option[KoodistoViite] = versioNumero match {
      case Some("latest") =>
        application.koodistoPalvelu.getLatestVersionOptional(koodistoUri)
      case Some(versio) =>
        Some(KoodistoViite(koodistoUri, versio.toInt))
      case _ =>
        application.koodistoPalvelu.getLatestVersionOptional(koodistoUri)
    }
    versio.toSeq.flatMap { koodisto => (application.koodistoPalvelu.getKoodistoKoodit(koodisto)) }
  }
}

case class OAuth2ProviderMetadata(
  issuer: String,

  authorization_endpoint: String,
  response_types_supported: Seq[String] = Seq("code"),
  response_modes_supported: Seq[String] = Seq("form_post"),
  scopes_supported: Seq[String],
  code_challenge_methods_supported: Seq[String] = Seq("S256"),

  token_endpoint: String,
  token_endpoint_auth_methods_supported: Seq[String] = Seq("tls_client_auth"),
  grant_types_supported: Seq[String] = Seq("authorization_code"),

  tls_client_certificate_bound_access_tokens: Boolean = false,

  service_documentation: String
)
