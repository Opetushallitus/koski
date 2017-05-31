package fi.oph.koski.pulssi

import java.time.LocalDateTime.now

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.util.FinnishDateFormat.finnishDateTimeFormat
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
      raportti
    } else {
      renderStatus(KoskiErrorCategory.forbidden("Käyttäjällä ei ole oikeuksia dokumenttiin"))
    }
  }

  private def raportti =
    <html>
      <head>
        {commonHead()}
        <link rel="stylesheet" type="text/css" href="/koski/css/raportti.css"></link>
      </head>
      <body id="raportti">
        <h2>Koski-raportti</h2>
        <p>{finnishDateTimeFormat.format(now)}</p>
        <h3>Oppijat ja opiskeluoikeudet</h3>
        <ul>
          <li class="oppijoiden-määrä">Oppijoiden määrä: <span class="value">{pulssi.oppijoidenMäärä}</span></li>
          <li class="opiskeluoikeuksien-määrä">Opiskeluoikeuksien määrä: <span class="value">{pulssi.opiskeluoikeusTilasto.opiskeluoikeuksienMäärä}</span></li>
          <li>
            Opiskeluoikeuksien määrät koulutusmuodoittain:
            <ul>
              {pulssi.opiskeluoikeusTilasto.koulutusmuotoTilastot.map { tilasto =>
              <li class={"opiskeluoikeuksien-määrä-" + tilasto.koulutusmuotoStr}>
                {tilasto.koulutusmuoto}: <span class="value">{tilasto.opiskeluoikeuksienMäärä}</span>, joista valmistuneita: {tilasto.valmistuneidenMäärä} ({percent(tilasto.valmistuneidenMäärä, tilasto.opiskeluoikeuksienMäärä)}%)
              </li>
            }}
            </ul>
          </li>
        </ul>
        <h3>Koski tiedonsiirrot</h3>
        <ul>
          <li class="siirtäneiden-oppilaitosten-määrä">Siirtäneitä oppilaitoksia: <span class="value">{pulssi.opiskeluoikeusTilasto.siirtäneitäOppilaitoksiaYhteensä}</span></li>
          <li class="oppilaitoksien-määrä">Oppilaitoksia yhteensä : <span class="value">{pulssi.oppilaitosMäärät.yhteensä}</span></li>
          <li class="siirtoprosentti">Siirtoprosentti: <span class="value">{percent(pulssi.opiskeluoikeusTilasto.siirtäneitäOppilaitoksiaYhteensä, pulssi.oppilaitosMäärät.yhteensä)}</span></li>
          <li>
            Koulutusmuodoittain:
            <ul>
              {pulssi.opiskeluoikeusTilasto.koulutusmuotoTilastot.flatMap { tilasto =>
                pulssi.oppilaitosMäärät.koulutusmuodoittain.get(tilasto.koulutusmuoto).map { oppilaitoksia =>
                  <li class={"siirtäneiden-oppilaitosten-määrä-" + tilasto.koulutusmuotoStr}>{tilasto.koulutusmuoto} : <span class="value">{tilasto.siirtäneitäOppilaitoksia}</span> ({percent(tilasto.siirtäneitäOppilaitoksia, oppilaitoksia)} %)</li>
              }
            }}
            </ul>
          </li>
        </ul>
        <h3>Koski käyttöoikeudet</h3>
        <ul>
          <li class="käyttöoikeuksien-määrä">Käyttöoikeuksien määrä: <span class="value">{pulssi.käyttöoikeudet.kokonaismäärä}</span></li>
          <li>
            Käyttöoikeuksien määrät ryhmittäin:
            <ul>
              {pulssi.käyttöoikeudet.ryhmienMäärät.map { case (ryhmä, määrä) =>
                <li class={"käyttöoikeusien-määrä-" + ryhmä}>{ryhmä}: <span class="value">{määrä}</span></li>
              }}
            </ul>
          </li>
        </ul>
        <h3>Metriikka viimeisen 30 päivän ajalta</h3>
        <ul>
          <li class="tiedonsiirtovirheiden-määrä">Tiedonsiirtovirheet: <span class="value">{pulssi.metrics.epäonnistuneetSiirrot}</span></li>
          <li class="käyttökatkojen-määrä">Käyttökatkojen määrä: <span class="value">{pulssi.metrics.katkot}</span></li>
          <li class="hälytysten-määrä">Hälytysten määrä: <span class="value">{pulssi.metrics.hälytykset}</span></li>
          <li class="virheiden-määrä">Lokitettujen virheiden määrä: <span class="value">{pulssi.metrics.virheet}</span></li>
        </ul>
      </body>
    </html>

  private def pulssi = application.koskiPulssi
  private def percent(x: Int, y: Int) = round(1)(x.toDouble / y.toDouble * 100)
}
