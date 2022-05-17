package fi.oph.koski.raamit

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.api.{Request, Response, Result}
import org.eclipse.jetty.http.{HttpHeader}
import org.eclipse.jetty.util.{HttpCookieStore}
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.scalatra.ScalatraServlet

import java.net.{URI, URL}
import java.nio.ByteBuffer
import java.util
import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.atomic.AtomicReference
import java.util.{HashSet, Locale}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.util.control.Breaks._

/**
 * fi.oph.koski.raamit.RaamiProxyServlet -luokalla proxytetään virkailijan ja oppijan raamea. Tätä toiminnallisuutta ei tule käyttää tuotannossa
 *
 * @deprecated
 */
class RaamiProxyServlet(val proxyHost: String, val proxyPrefix: String, val application: KoskiApplication) extends ScalatraServlet with Logging {
  protected val hopHeaders = Set("connection", "keep-alive", "proxy-authentication", "proxy-connection", "transfer-encoding", "te", "trailer", "upgrade")

  override def service(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val httpClient = new HttpClient(new SslContextFactory.Client)
    httpClient.start()
    // Kytketään GZip pois: https://github.com/eclipse/jetty.project/issues/7681
    httpClient.getContentDecoderFactories().clear();
    // Evästeitä ei saa säilöä kun proxytetaan
    httpClient.setCookieStore(new HttpCookieStore.Empty());

    val rewrittenTarget: String = rewriteTarget(request, proxyPrefix, proxyHost)

    val responseRef = new AtomicReference[Response]
    val latch = new CountDownLatch(1)

    val proxyRequest = httpClient
      .newRequest(rewrittenTarget) // Redirectejä ei seurata kun kyseessä on proxy
      .followRedirects(false)
      .method(request.getMethod)
      .timeout(5, TimeUnit.SECONDS)
    // Kopioidaan pyynnön headerit mukaan
    copyRequestHeaders(request, proxyRequest)
    // Asetetaan HOST-header
    setHostHeader(proxyRequest, proxyHost)

    var responseBuffer = Array[Byte]()
    // Puskurin maksimikoko on 8MB
    val maxBufferSize = 8000000

    // Async pyyntö
    proxyRequest.send(new Response.Listener() {
      override def onComplete(result: Result): Unit = {
        if (result.isSucceeded) {
          responseRef.set(result.getResponse)
          latch.countDown()
        } else {
          logger.error(result.getFailure)(s"RaamiProxyServlet request failed for ${rewrittenTarget}")
        }
      }

      override def onContent(res: Response, content: ByteBuffer): Unit = {
        val newBufferSize = responseBuffer.length + content.remaining
        if (newBufferSize > maxBufferSize) {
          logger.error(new IllegalArgumentException(s"RaamiProxyServlet buffer capacity exceeded for entity ${request.getRequestURI}"))(s"RaamiProxyServlet buffer capacity exceeded for entity ${request.getRequestURI}")
          throw new IllegalArgumentException(s"RaamiProxyServlet buffer capacity exceeded for entity ${request.getRequestURI}")
        }
        val newBuffer = new Array[Byte](newBufferSize)
        System.arraycopy(responseBuffer, 0, newBuffer, 0, responseBuffer.length)
        content.get(newBuffer, responseBuffer.length, content.remaining)
        responseBuffer = newBuffer
      }
    })

    def result = latch.await(10, TimeUnit.SECONDS)

    if (!result) {
      logger.error(new Exception(s"Timeout of 10 seconds exceeded in RaamiProxyServlet when requesting ${rewrittenTarget}"))(s"Timeout of 10 seconds exceeded in RaamiProxyServlet when requesting ${rewrittenTarget}")
    }

    val httpResponse = responseRef.get();
    copyResponseHeaders(httpResponse, response)
    response.setStatus(httpResponse.getStatus)
    response.getOutputStream().write(responseBuffer)
    httpClient.stop()
  }

  protected def setHostHeader(request: Request, value: String): Request = {
    // Jetty.HttpClient ei osaa korvata headeria, ellei sitä aseta ensin tyhjäksi
    request.header(HttpHeader.HOST, null)
    request.header(HttpHeader.HOST, new URL(value).getHost)
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

  protected def copyResponseHeaders(proxyResponse: Response, clientResponse: HttpServletResponse): Unit = {
    val headerNames = proxyResponse.getHeaders.getFieldNames
    while ( {
      headerNames.hasMoreElements
    }) {
      val headerName = headerNames.nextElement
      val headerValues = proxyResponse.getHeaders.getValues(headerName)
      while ( {
        headerValues.hasMoreElements
      }) {
        val headerValue = headerValues.nextElement
        if (headerValue != null) clientResponse.setHeader(headerName, headerValue)
      }
    }
  }

  protected def copyRequestHeaders(clientRequest: HttpServletRequest, proxyRequest: Request): Unit = {
    proxyRequest.getHeaders.clear()
    val headersToRemove = findConnectionHeaders(clientRequest)
    val headerNames = clientRequest.getHeaderNames
    while ( {
      headerNames.hasMoreElements
    }) {
      val headerName = headerNames.nextElement
      val lowerHeaderName = headerName.toLowerCase(Locale.ENGLISH)
      breakable {
        if (HttpHeader.HOST.is(headerName)) break // Ohitetaan HOST-header
      }
      breakable {
        if (hopHeaders.contains(lowerHeaderName)) break
      }
      breakable {
        if (headersToRemove != null && headersToRemove.contains(lowerHeaderName)) break
      }
      val headerValues = clientRequest.getHeaders(headerName)
      while ( {
        headerValues.hasMoreElements
      }) {
        val headerValue = headerValues.nextElement
        if (headerValue != null) proxyRequest.header(headerName, headerValue)
      }
    }
  }

  protected def findConnectionHeaders(clientRequest: HttpServletRequest): util.Set[String] = {
    // http://tools.ietf.org/html/rfc7230#section-6.1.
    val hopHeaders: HashSet[String] = new util.HashSet[String]
    val connectionHeaders = clientRequest.getHeaders(HttpHeader.CONNECTION.asString)
    while ( {
      connectionHeaders.hasMoreElements
    }) {
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
