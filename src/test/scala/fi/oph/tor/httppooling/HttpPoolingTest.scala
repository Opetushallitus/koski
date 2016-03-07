package fi.oph.tor.httppooling

import fi.oph.tor.http.Http
import fi.oph.tor.http.Http.runTask

object HttpPoolingTest extends App {
  private val http: Http = Http("http://virkailija.tordev.tor.oph.reaktor.fi/authentication-henkiloui/html/index.html#/henkilolistaus2/")
  println(runTask(http("")(Http.statusCode)))
  Thread.sleep(200)
  println(runTask(http("")(Http.statusCode)))
}
