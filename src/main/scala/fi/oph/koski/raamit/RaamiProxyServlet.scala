package fi.oph.koski.raamit

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import org.eclipse.jetty.client.{ContentResponse, HttpClient, Request}
import org.eclipse.jetty.http.{HttpCookieStore, HttpField, HttpHeader}
import org.scalatra.ScalatraServlet

import java.net.{URI, URL}
import java.util
import java.util.concurrent.TimeUnit
import java.util.{HashSet, Locale}
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.jdk.CollectionConverters._

/**
 * fi.oph.koski.raamit.RaamiProxyServlet -luokalla proxytetään virkailijan ja oppijan raamea. Tätä toiminnallisuutta ei tule käyttää tuotannossa
 *
 * @deprecated
 */
class RaamiProxyServlet(val proxyHost: String, val proxyPrefix: String, val application: KoskiApplication) extends ScalatraServlet with Logging {
  protected val hopHeaders = Set("connection", "keep-alive", "proxy-authentication", "proxy-connection", "transfer-encoding", "te", "trailer", "upgrade")

  override def service(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val httpClient = new HttpClient()
    httpClient.start()
    // Kytketään GZip pois: https://github.com/eclipse/jetty.project/issues/7681
    httpClient.getContentDecoderFactories().clear()
    // Evästeitä ei saa säilöä kun proxytetaan
    httpClient.setHttpCookieStore(new HttpCookieStore.Empty())

    val rewrittenTarget: String = rewriteTarget(request, proxyPrefix, proxyHost)

    val proxyRequest = httpClient
      .newRequest(rewrittenTarget) // Redirectejä ei seurata kun kyseessä on proxy
      .followRedirects(false)
      .method(request.getMethod)
      .timeout(10, TimeUnit.SECONDS)
    // Kopioidaan pyynnön headerit mukaan
    copyRequestHeaders(request, proxyRequest)
    // Asetetaan HOST-header
    setHostHeader(proxyRequest, proxyHost)

    val contentResponse: ContentResponse = proxyRequest.send()

    copyResponseHeaders(contentResponse, response)
    response.setStatus(contentResponse.getStatus)
    response.getOutputStream.write(contentResponse.getContent)
    httpClient.stop()
  }

  protected def setHostHeader(request: Request, value: String): Request = {
    request.headers(h => {
      h.remove(HttpHeader.HOST)
      h.put(HttpHeader.HOST, new URL(value).getHost)
    })
    request
  }

  protected def rewriteTarget(request: HttpServletRequest, prefix: String, proxyTo: String): String = {
    val path = request.getRequestURI
    if (!path.startsWith(prefix)) {
      Nil
    }
    val uri = new StringBuilder(proxyTo)
    if (proxyTo.endsWith("/")) uri.setLength(uri.length - 1)
    val rest = path.substring(prefix.length)
    if (!rest.isEmpty) {
      if (!rest.startsWith("/")) uri.append("/")
      uri.append(rest)
    }
    val query = request.getQueryString
    if (query != null) {
      val separator = "://"
      if (uri.indexOf("/", uri.indexOf(separator) + separator.length) < 0) uri.append("/")
      uri.append("?").append(query)
    }
    val rewrittenURI = URI.create(uri.toString).normalize
    rewrittenURI.toString
  }

  protected def copyResponseHeaders(proxyResponse: ContentResponse, clientResponse: HttpServletResponse): Unit = {
    proxyResponse.getHeaders.asScala.foreach { field: HttpField =>
      clientResponse.setHeader(field.getName, field.getValue)
    }
  }

  protected def copyRequestHeaders(clientRequest: HttpServletRequest, proxyRequest: Request): Unit = {
    proxyRequest.headers(h => h.clear())
    val headersToRemove = findConnectionHeaders(clientRequest)
    val headerNames = clientRequest.getHeaderNames
    while (headerNames.hasMoreElements) {
      val headerName = headerNames.nextElement
      val lowerHeaderName = headerName.toLowerCase(Locale.ENGLISH)
      if (!HttpHeader.HOST.is(headerName) && !hopHeaders.contains(lowerHeaderName) &&
        (headersToRemove == null || !headersToRemove.contains(lowerHeaderName))) {
        val headerValues = clientRequest.getHeaders(headerName)
        while (headerValues.hasMoreElements) {
          val headerValue = headerValues.nextElement
          if (headerValue != null) proxyRequest.headers(h => h.add(headerName, headerValue))
        }
      }
    }
  }

  protected def findConnectionHeaders(clientRequest: HttpServletRequest): util.Set[String] = {
    // http://tools.ietf.org/html/rfc7230#section-6.1.
    val hopHeaders: HashSet[String] = new util.HashSet[String]
    val connectionHeaders = clientRequest.getHeaders(HttpHeader.CONNECTION.asString)
    while (connectionHeaders.hasMoreElements) {
      val value = connectionHeaders.nextElement
      val values = value.split(",")
      for (name <- values) {
        val _name = name.trim.toLowerCase(Locale.ENGLISH)
        hopHeaders.add(_name)
      }
    }
    hopHeaders
  }
}
