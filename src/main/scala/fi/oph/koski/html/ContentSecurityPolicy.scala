package fi.oph.koski.html

object ContentSecurityPolicy {
  private val defaultSrc = "default-src 'none'"

  // Huomaa, että unsafe-inline ja whitelistatut https: http: ovat käytössä vain vanhoilla selaimilla: uusissa
  // ne ignoroidaan ja vain nonce ja strict-dynamic ovat voimassa.
  private def scriptSrc(nonce: String) = s"script-src 'nonce-${nonce}' 'unsafe-inline' 'strict-dynamic' https: http:"

  // 'unsafe-inline': jätetty vanhoja nonceja tukemattomia selaimia varten: uudet selaimet ignoroivat sen.
  private def styleSrc(nonce: String) = s"style-src 'report-sample' 'nonce-${nonce}' 'unsafe-inline' 'self' fonts.googleapis.com"

  private val objectSrc = "object-src 'none'"

  private val baseUri = "base-uri 'none'"

  // TODO: raportointipalvelu
  // TODO: Lisää myös Content-Security-Policy-Report-Only -header, ainakin tuotannossa CSP:n testaamiseksi ennen
  // käyttöönottoa
  // TODO: report-sample mukaan script-src -direktiiviin, voi helpottaa raporttien parsintaa, kun saa pätkän
  // skriptiä aina mukaan
  private val reportUri = "" // report-uri https://your-report-collector.example.com/

  // Raameissa on data-fontti, siksi tarvitaan.
  private val fontSrc = "font-src 'self' data: fonts.gstatic.com fonts.googleapis.com"

  // Raameissa on data-imageja, siksi tarvitaan
  private val imgSrc = "img-src 'self' data: analytiikka.opintopolku.fi"

  private val connectSrc = "connect-src 'self'"

  private val formAction = "form-action 'self'"

  // TODO: tarvitaanko me näitä 3 johonkin? Muuta, jos tarvitaan. Ehkä mocha-testit paikallisesti ajettaessa?
  // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/child-src
  // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/frame-ancestors
  // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/frame-src
  private val childSrc = "child-src 'none'"

  private def frameAncestors(allowFrameAncestors: Boolean): String =
    allowFrameAncestors match {
      case true => "frame-ancestors 'self'"
      case _ => "frame-ancestors 'none'"
    }

  private val frameSrc = "frame-src 'none'"

  private val manifestSrc = "manifest-src 'none'"

  private val mediaSrc = "media-src 'none'"

  private val workerSrc = "worker-src 'none'"

  def headers(allowFrameAncestors: Boolean, nonce: String): Map[String, String] = {
    Map(
      "Content-Security-Policy" -> createString(allowFrameAncestors, nonce)
    )
  }

  private def createString(allowRunningInFrame: Boolean, nonce: String): String =
    List(
      defaultSrc,
      scriptSrc(nonce),
      styleSrc(nonce),
      objectSrc,
      baseUri,
      reportUri,
      fontSrc,
      imgSrc,
      connectSrc,
      formAction,
      childSrc,
      frameAncestors(allowRunningInFrame),
      frameSrc,
      manifestSrc,
      mediaSrc,
      workerSrc
    ).filter(_.nonEmpty)
      .mkString("; ")
}
