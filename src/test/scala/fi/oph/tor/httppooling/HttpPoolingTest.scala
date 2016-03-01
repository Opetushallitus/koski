package fi.oph.tor.httppooling

import fi.oph.tor.http.Http

object HttpPoolingTest extends App {
  private val http: Http = Http("http://virkailija.tordev.tor.oph.reaktor.fi/authentication-henkiloui/html/index.html#/henkilolistaus2/")
  println(http("")(Http.statusCode).run)
  Thread.sleep(200)
  println(http("")(Http.statusCode).run)
}
