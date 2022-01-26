package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasYtlSession
import fi.oph.koski.ytl.YtlRequest

class ValpasYtlServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasYtlSession {

  post("/oppijat") {
    // TODO: Disabloi tuotannossa toistaiseksi varmuuden vuoksi. Poista, kun valmista.
    if (application.config.getString("opintopolku.virkailija.url") == "https://virkailija.opintopolku.fi") {
      haltWithStatus(ValpasErrorCategory.notImplemented())
    }

    withJsonBody { json =>
      YtlRequest.parseBulk(json) match {
        case Right((oidit, hetut)) =>
          "Hello world"
        case Left(status) =>
          haltWithStatus(status)
      }
    }()
  }
}
