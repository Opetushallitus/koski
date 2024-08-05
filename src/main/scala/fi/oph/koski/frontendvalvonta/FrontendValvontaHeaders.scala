package fi.oph.koski.frontendvalvonta

import fi.oph.koski.frontendvalvonta.FrontendValvontaMode.FrontendValvontaMode

object FrontendValvontaHeaders {

  private val defaultSrc = "default-src 'none'"

  // Huomaa, että unsafe-inline ja whitelistatut https: http: ovat käytössä vain vanhoilla selaimilla: uusissa
  // ne ignoroidaan ja vain nonce ja strict-dynamic ovat voimassa.
  private def scriptSrc(nonce: String) = s"script-src 'report-sample' 'nonce-${nonce}' 'unsafe-inline' 'strict-dynamic' https: http:"

  // YTL:n koesuorituskopioissa on inline-tyylejä, sitä varten mahdollisuus luoda header ilman noncea
  // JSON-schema-viewer:ssä on myös jquery-koodin luomia inline-tyylejä
  private def styleSrc(unsafeAllowInlineStyles: Boolean = false, nonce: String) = {
    val noncePart = if (unsafeAllowInlineStyles) { "" } else { s"'nonce-${nonce}'"}
    List(
      // 'unsafe-inline': jätetty noncen kanssakin vanhoja nonceja tukemattomia selaimia varten: uudet selaimet ignoroivat sen.
      s"style-src 'report-sample' 'unsafe-inline' 'self' fonts.googleapis.com",
      noncePart
    ).mkString(" ")
  }

  private val objectSrc = "object-src 'none'"

  // JSON-schema-viewer asettaa baseUri:n
  private def baseUri(unsafeAllowBaseUri: Boolean = false) = {
    unsafeAllowBaseUri match {
      case true => "base-uri 'self'"
      case _ => "base-uri 'none'"
    }
  }

  private val uriForReportTo = s"/koski/api/frontendvalvonta/report-to"
  private val uriForReportUri = s"/koski/api/frontendvalvonta/report-uri"
  private val cspEndPointGroup = "csp-endpoint"

  private val reportTo = s"report-to ${cspEndPointGroup}"
  private val reportUri = s"report-uri ${uriForReportUri}"

  // Raameissa on data-fontti, siksi tarvitaan. YTL:n koesuorituskopioissa ladataan fontteja digabi.github.io:sta.
  private val fontSrc = "font-src 'self' data: fonts.gstatic.com fonts.googleapis.com digabi.github.io"

  // Raameissa ja YTL:n koesuorituksissa on data-imageja, siksi tarvitaan
  private val imgSrc = "img-src 'self' data: browser-update.org"

  private val connectSrc = "connect-src 'self'"

  private val formAction = "form-action 'self'"

  private val childSrc = "child-src 'none'"

  private def frameAncestors(allowFrameAncestors: Boolean): String =
    allowFrameAncestors match {
      case true => "frame-ancestors 'self'"
      case _ => "frame-ancestors 'none'"
    }

  private def frameSrc(allowFrameSrcSelf: Boolean): String =
    allowFrameSrcSelf match {
      case true => "frame-src 'self'"
      case _ => "frame-src 'none'"
    }

  private val manifestSrc = "manifest-src 'none'"

  private val mediaSrc = "media-src 'none'"

  private val workerSrc = "worker-src 'none'"

  // require-trusted-types-for olisi hyvä olla, mutta käyttö on selaimissa tuettu vasta Chromessa ja Edgessä.
  // Käyttöönotto vaatisi myös muutoksia fronttikoodiin.
  private val requireTrustedTypesFor = "require-trusted-types-for 'script'"

  def headers(allowFrameAncestors: Boolean, allowFrameSrcSelf: Boolean, mode: FrontendValvontaMode, unsafeAllowInlineStyles: Boolean, unsafeAllowBaseUri: Boolean, nonce: String): Map[String, String] = {
    if (mode != FrontendValvontaMode.DISABLED) {
      val key = mode match {
        case FrontendValvontaMode.REPORT_ONLY => s"Content-Security-Policy-Report-Only"
        case FrontendValvontaMode.ENABLED => s"Content-Security-Policy"
      }
      Map(
        key -> createString(allowFrameAncestors, allowFrameSrcSelf, unsafeAllowInlineStyles, unsafeAllowBaseUri, nonce),
        "Report-To" -> s"""{ "group": "${cspEndPointGroup}", "max_age": 10886400, "endpoints": [ { "url": "${uriForReportTo}" } ] }"""
      )
    } else {
      Map.empty
    }
  }

  private def createString(allowRunningInFrame: Boolean, allowFrameSrcSelf: Boolean, unsafeAllowInlineStyles: Boolean, unsafeAllowBaseUri: Boolean, nonce: String): String =
    List(
      defaultSrc,
      scriptSrc(nonce),
      styleSrc(unsafeAllowInlineStyles, nonce),
      objectSrc,
      baseUri(unsafeAllowBaseUri),
      fontSrc,
      imgSrc,
      connectSrc,
      formAction,
      childSrc,
      frameAncestors(allowRunningInFrame),
      frameSrc(allowFrameSrcSelf),
      manifestSrc,
      mediaSrc,
      workerSrc,
      reportTo,
      reportUri
    ).filter(_.nonEmpty)
      .mkString("; ")
}
