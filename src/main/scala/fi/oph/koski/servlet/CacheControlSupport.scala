package fi.oph.koski.servlet

import org.scalatra.ScalatraServlet

import scala.concurrent.duration._

trait CacheControlSupport extends ScalatraServlet {
  def noCache = {
    addHeaders(Map( "Cache-Control" -> "no-store, no-cache, must-revalidate", "Pragma" -> "no-cache" ))
  }

  def cacheForUserSession = {
    addHeaders(Map("Vary" -> "Cookie", "Cache-Control" -> "private"))
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

trait UserSessionCached extends Cached {
  before() {
    cacheForUserSession
  }
}

trait Cached extends CacheControlSupport {
  def cacheDuration: Duration
  before() {
    cacheMaxAge(cacheDuration)
  }
}

trait Cached24Hours extends Cached {
  def cacheDuration = 24 hours
}