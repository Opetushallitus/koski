package fi.oph.koski.servlet

import org.scalatra.ScalatraServlet

import scala.concurrent.duration.Duration

trait CacheControlSupport extends ScalatraServlet {
  def noCache = {
    addHeaders(Map( "Cache-Control" -> "no-store, no-cache, must-revalidate", "Pragma" -> "no-cache" ))
  }

  def cacheMaxAge(duration: Duration) = {
    addHeaders(Map( "Cache-Control" -> ( "max-age=" + duration.toSeconds ) ))
  }

  private def addHeaders(headers: Map[String, String]) = for ( ( key, value ) <- headers ) response.addHeader( key, value )
}

trait NoCache extends CacheControlSupport {
  before() {
    noCache
  }
}