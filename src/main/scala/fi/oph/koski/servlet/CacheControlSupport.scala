package fi.oph.koski.servlet

import org.scalatra.ScalatraServlet

import scala.concurrent.duration._

trait CacheControlSupport extends ScalatraServlet {
  def getCacheHeaders: Map[String, String]

  before() {
    for ( ( key, value ) <- getCacheHeaders ) response.addHeader( key, value )
  }
}

trait NoCache extends CacheControlSupport {
  def getCacheHeaders = Map( "Cache-Control" -> "no-store, no-cache, must-revalidate", "Pragma" -> "no-cache" )
}

trait Cached extends CacheControlSupport {
  def cacheDuration: Duration
  def getCacheHeaders = Map( "Cache-Control" -> ( "max-age=" + cacheDuration.toSeconds ) )
}

trait Cached24Hours extends Cached {
  def cacheDuration = 24 hours
}

trait CacheForUserSession extends CacheControlSupport {
  def getCacheControlHeaders = Map("Vary" -> "Cookie", "Cache-Control" -> "private")
}