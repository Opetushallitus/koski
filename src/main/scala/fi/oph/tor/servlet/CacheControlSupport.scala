package fi.oph.tor.servlet

import org.scalatra.ScalatraServlet

trait CacheControlSupport extends ScalatraServlet {
  private val noCacheHeaders = Map( "Cache-Control" -> "no-store, no-cache, must-revalidate", "Pragma" -> "no-cache" )

  def noCache = {
    for ( ( key, value ) <- noCacheHeaders ) response.addHeader( key, value )
  }
}

trait NoCache extends CacheControlSupport {
  before() {
    noCache
  }
}