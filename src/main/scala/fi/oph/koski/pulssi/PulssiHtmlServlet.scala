package fi.oph.koski.pulssi

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.servlet.HtmlServlet
import org.scalatra.ScalatraServlet

class PulssiHtmlServlet(val application: KoskiApplication) extends ScalatraServlet with HtmlServlet {
  get("/") {
    htmlIndex("koski-pulssi.js")
  }

  get("/raportti") {
    if (!isAuthenticated) {
      redirectToLogin
    }
    if (koskiSessionOption.exists(_.hasGlobalReadAccess)) {
      <html>
        {htmlHead()}
        <body>
          <h3>Oppijat ja opiskeluoikeudet</h3>
          <ul>
            <li>
              Oppijoiden määrä: {pulssi.sisäisetOpiskeluoikeustiedot.getOrElse("oppijoidenMäärä", 0)}
            </li>
            <li>
              Opiskeluoikeuksien määrä: {pulssi.opiskeluoikeudet.getOrElse("opiskeluoikeuksienMäärä", 0)}
            </li>
            <li>
              Opiskeluoikeuksien määrät koulutusmuodoittain:
              <ul>
                {
                pulssi.opiskeluoikeudet.getOrElse("määrätKoulutusmuodoittain", 0).asInstanceOf[List[Map[String, Any]]].map { koulutusmuoto =>
                  <li>
                    {koulutusmuoto.getOrElse("nimi", "")}: {koulutusmuoto.getOrElse("opiskeluoikeuksienMäärä", 0)}
                  </li>
                }}
              </ul>
            </li>
          </ul>
          <h3>Koski tiedonsiirrot</h3>
          <ul>
            <li>
            </li>
          </ul>
          <h3>Koski käyttöoikeudet</h3>
          <ul>
            <li>
              Käyttöoikeuksien määrä: {pulssi.käyttöoikeudet.getOrElse("kokonaismäärä", 0)}
            </li>
            <li>
              Käyttöoikeuksien määrät ryhmittäin:
              <ul>
              {pulssi.käyttöoikeudet.getOrElse("käyttöoikeusmäärät", Map()).asInstanceOf[Map[String, Int]].map { case (ryhmä, määrä) =>
                <li>
                  {ryhmä}: {määrä}
                </li>
              }}
              </ul>
            </li>
          </ul>
          <h3>Metriikka viimeisen 30 päivän ajalta</h3>
          <ul>
            <li>
              Tiedonsiirtovirheet: {pulssi.sisäinenMetriikka.getOrElse("tiedonsiirtovirheet", 0)}
            </li>
            <li>
              Käyttökatkojen määrä: {pulssi.sisäinenMetriikka.getOrElse("katkot", 0)}
            </li>
            <li>
              Hälytysten määrä: {pulssi.sisäinenMetriikka.getOrElse("hälytykset", 0)}
            </li>
            <li>
              Lokitettujen virheiden määrä: {pulssi.sisäinenMetriikka.getOrElse("sovellusvirheet", 0)}
            </li>
          </ul>
        </body>
      </html>
    } else {
      renderStatus(KoskiErrorCategory.notFound())
    }
  }

  private def pulssi = application.koskiPulssi
}
