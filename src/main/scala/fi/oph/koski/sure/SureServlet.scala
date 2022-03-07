package fi.oph.koski.sure

import java.sql.Timestamp
import java.time.{Instant, OffsetDateTime}
import java.time.format.DateTimeParseException

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.{JsonSerializer, SensitiveAndRedundantDataFilter}
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.OppijaOidHaku
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, InvalidRequestException, NoCache, ObservableSupport}
import fi.oph.koski.util.Timing
import org.json4s.JValue
import org.scalatra.ContentEncodingSupport

class SureServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with Logging
    with ObservableSupport
    with RequiresVirkailijaOrPalvelukäyttäjä
    with ContentEncodingSupport
    with NoCache
    with Timing {

  // palauttaa annettujen oppija-oidien kaikki (Koskeen tallennetut, ei mitätöidyt) opiskeluoikeudet.
  // mikäli jollekin OIDille ei löydy yhtään opiskeluoikeutta, tämä ei ole virhe (ja ko. OID puuttuu vastauksesta)
  post("/oids") {
    val MaxOids = 1000
    withJsonBody { parsedJson =>
      val oids = JsonSerializer.extract[List[String]](parsedJson)
      if (oids.size > MaxOids) {
        haltWithStatus(KoskiErrorCategory.badRequest.queryParam(s"Liian monta oidia, enintään $MaxOids sallittu."))
      }
      oids.map(HenkilöOid.validateHenkilöOid).collectFirst { case Left(status) => status } match {
        case None =>
          val serialize = SensitiveAndRedundantDataFilter(session).rowSerializer
          val observable = OpiskeluoikeusQueryContext(request)(session, application).queryWithoutHenkilötiedotRaw(
            filters = List(
              OppijaOidHaku(oids)
            ),
            paginationSettings = None,
            queryForAuditLog = "oids=" + oids.take(2).mkString(",") + ",...(" + oids.size + ")"
          )
          streamResponse[JValue](observable.map(t => serialize(OidHenkilö(t._1), t._2)), session)
        case Some(status) => haltWithStatus(status)
      }
    }()
  }

  // Palauttaa listan oppijoista (oppija-oidit) joiden Koskeen tallennetut tiedot ovat muuttuneet.
  //
  // Tätä rajapintaa voi käyttää kahdella tavalla:
  // - Haetaan kertaluontoisesti kaikki oppija-OIDit. Tällöin kutsutaan APIa ensin halutulla timestamp-parametrilla,
  //   ja sen jälkeen haetaan lisää sivuja cursor-parametrilla kunnes kaikki datat on haettu (vastauksessa mayHaveMore=false).
  // - Pollataan muutoksia pidemmältä ajalta. Tällöin kutsutaan APIa kerran halutulla timestamp-parametrilla,
  //   ja talletetaan cursor. Jatkossa kutsutaan esim. kerran tunnissa cursor-parametrilla (kunnes mayHaveMore=false), ja
  //   talletetaan päivittynyt cursor-arvo.
  //
  // Kutsujan tulee huomioida seuraava:
  // - Vastaus sisältää vain oppija-OIDit - itse datat voi hakea esim. ylläolevalla "oids" rajapinnalla.
  // - Yhdessä vastauksessa on enintään limit-parametrin (oletus ja maksimi 5000) OIDia.
  //   Vastauksen "mayHaveMore" kertoo onko seuraava sivu dataa saatavilla heti (tätä ei pidä
  //   päätellä results-listan pituudesta).
  // - API tulkitsee myös opiskeluoikeuden mitätöinnin muutokseksi. Tästä seuraa että API voi palauttaa
  //   oppija-OIDin jolle ei löydy mitään opiskeluoikeuksia oids-rajapinnalla.
  // - API palauttaa joissain tapauksissa palauttaa samat OID:it useaan kertaan (samassa vastauksessa tai
  //   eri vastauksissa), vaikka oppija olisi muuttunut vain kerran. Erityisesti aivan tuoreissa
  //   muutoksissa peräkkäiset vastaukset ovat n. 20 sekuntia "päällekkäin". Tarkempi selitys miksi
  //   näin tehdään on aika pitkä (jos kiinnostaa niin lue alla), mutta tästä seuraa
  //   että järkevä väli kutsuilla (jos mayHaveMore=false) on pikemminkin useita minuutteja.
  //
  // Tässä suhteellisen yksinkertaiselta näyttävässä APIssa on epätriviaalia koodia konepellin alla:
  //
  // Varsinkin "haetaan kertaluontoisesti kaikki oppija-OIDit" käyttötapauksessa muuttuneita OIDeja voi olla
  // miljoonia, joten rajapinta tarvitsee sivutuksen. Monissa muissa Kosken rajapinnoissa käytetty
  // LIMIT/OFFSET sivutus ei toimi tässä, koska järjestävä kenttä (aikaleima) päivittyy koko ajan,
  // ja sivujen rajat olisivat eri kutsuissa eri kohdissa (ja rajapinta ei palauttaisi luotettavasti
  // kaikkia muutoksia).
  //
  // Pelkkä aikaleimakaan ei riitä sivutukseen, koska kannassa voi olla suuria määriä (huomattavasti sivukokoa enemmän)
  // rivejä joilla on täsmälleen sama aikaleima. Tällaisia syntyy esim. jos ajetaan kantamigraatiossa
  // UPDATE ilman että disabloidaan triggeri ensin (triggerin käyttämä current_timestamp/now on
  // Postgresissa transaktion alkuaika, joten kaikki UPDATEn päivittämät rivit saavat saman aikaleiman,
  // vaikka UPDATEn ajo kestäisi kauan).
  //
  // Näistä johtuen sivutus ja toistuvien kutsujen "mihin asti kutsuja on saanut muutokset" tieto
  // on toteutettu "kursorilla". Kutsujan kannalta kyseessä on mielivaltainen stringi joka saadaan
  // vastauksen "nextCursor" kentässä ja kopioidaan seuraavan pyynnön "cursor" query parametriin.
  // Sisäisesti tämä on toteutettu (aikaleima, id) parina.
  //
  // Asiaa monimutkaistaa entisestään se, että opiskeluoikeus-taulun aikaleima (jota päivitetään SQL triggerillä)
  // kertoo transaktion alkuajan, ei transaktion commit-aikaa. Tästä seuraa että se ei ole aina kasvava! Eli
  // jos kutsuja on hakenut kaikki rivit aikaleimaan T saakka, niin on mahdollista että hieman myöhemmin
  // kantaan ilmestyy/päivittyy rivi, jonka aikaleima on < T (eli kyseisen opiskeluoikeuden INSERT/UPDATE
  // transaktio oli alkanut ennen T:tä, mutta se kommitoitiin vasta T:n jälkeen). Toisin sanoen jos
  // SQL-query olisi yksinkertaisesti "select .. where aikaleima >= edellisen vastauksen viimeinen aikaleima",
  // niin on mahdollista että API:n kutsujalta jäisi muutoksia näkemättä.
  //
  // Mikään ei myöskään varsinaisesti takaa ettei koneiden kello ikinä menisi millisekuntiakaan taaksepäin (mutta
  // edellisen kappaleen ongelma esiintyisi vaikka käytettäisiin SEQUENCEa muutosten numerointiin).
  //
  // Jotta vältetään että kutsujalta jää muutoksia näkemättä, tuoreiden muutosten kohdalla kursoria
  // ei siirretä koskaan nykyhetkeen saakka, vaan se jätetään 20 sekuntia menneisyyteen.
  // Seuraavassa kutsussa siis palautetaan samat rivit (+väliaikana mahdollisesti tuohon aikaikkunaan
  // ilmestyneet rivit) uudestaan. Tästä seuraa se, että kutsujan ei kannata pollata muutoksia
  // liian usein (muuten vastauksissa tulee paljon ylimääräistä).
  //
  // API siis olettaa että kantaan ei ilmesty rivejä joiden aikaleima on yli 20 sekuntia menneisyydessä
  // (joko siksi että transaktio kestää pitkään, tai kello siirtyy yli 20 sekuntia taaksepäin).
  // Yleisesti ottaen API ei pysty havaitsemaan jos tämä oletus ei pidäkään paikkaansa (vain yksi
  // tietty tapaus voidaan havaita), vaan silloin kutsujalta jää muutoksia saamatta.
  //
  // Tämä arvo 20 sekuntia ei ole perusteellisen selvityksen tulos, vaan tämänhetkinen arvaus
  // siitä mikä voisi olla sopiva "turvamarginaali". Voi olla että sitä pitää muuttaa tulevaisuudessa.
  // Sekä tämä API-kutsu että sitä yleensä seuraava "oids" kutsu käyttävät primary-kantaa, jotta
  // tässä ei tarvitse huomioida vielä replikointiviivettäkin (joka on normaalisti toki pieni,
  // mutta voi hetkellisesti olla yli 30 s, esim. monimutkaisen kyselyn tai muun kuormapiikin seurauksena).

  get("/muuttuneet-oppijat") {
    val MinPageSize = 2
    val MaxPageSize = 5000
    val DefaultPageSize = MaxPageSize
    val DefaultRecentPageOverlapSeconds = 20

    if (!session.hasGlobalReadAccess) {
      // Toteutuksessa käytetään OpiskeluOikeudet (eikä OpiskeluOikeudetWithAccessCheck) koska halutaan myös mitätöidyt
      haltWithStatus(KoskiErrorCategory.forbidden())
    }

    val pageSize: Int = getOptionalIntegerParam("pageSize").getOrElse(DefaultPageSize)
    if ((pageSize < MinPageSize) || (pageSize > MaxPageSize)) {
      throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam(s"pageSize pitää olla välillä $MinPageSize-$MaxPageSize"))
    }

    val recentPageOverlapSeconds: Int = getOptionalIntegerParam("recentPageOverlapTestsOnly").getOrElse(DefaultRecentPageOverlapSeconds)
    if ((recentPageOverlapSeconds < 0) || (recentPageOverlapSeconds > DefaultRecentPageOverlapSeconds)) {
      throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam(s"recentPageOverlapTestsOnly on vain testeille"))
    }

    (params.get("timestamp"), params.get("cursor")) match {
      case (Some(timestamp), None) =>
        val nextCursor = createCursor(parseTimestamp(timestamp), 0)
        MuuttuneetOppijatResponse(Seq.empty, mayHaveMore = true, nextCursor)

      case (None, Some(cursor)) =>
        val (cursorTimestamp, cursorId) = parseCursor(cursor)
        val currentTimestamp: Timestamp = application.opiskeluoikeusQueryRepository.serverCurrentTimestamp
        val recentPageTimestamp: Timestamp = Timestamp.from(currentTimestamp.toInstant.minusSeconds(recentPageOverlapSeconds))
        val rows = application.opiskeluoikeusQueryRepository.muuttuneetOpiskeluoikeudetWithoutAccessCheck(cursorTimestamp, cursorId, pageSize)
        val mayHaveMore = rows.size >= pageSize
        val nextCursor = if (rows.isEmpty) {
          cursor
        } else {
          val latestSufficientlyOldRow = rows.reverseIterator.find(_.aikaleima.before(recentPageTimestamp))
          if (latestSufficientlyOldRow.isDefined) {
            createCursor(latestSufficientlyOldRow.get.aikaleima, latestSufficientlyOldRow.get.id)
          } else {
            val oldestRow = rows.head
            if ((oldestRow.aikaleima.getTime - currentTimestamp.getTime) > (recentPageOverlapSeconds * 1000)) {
              // Kello on siirtynyt taaksepäin reilusti. Parempi että ihminen selvittää mistä on kyse.
              // (Huom, tämä on vain yksi erikoistapaus, yleisesti ottaen emme huomaa jos
              // oletus että kantaan ei ilmesty >20 s vanhempaa dataa ei pidä paikkaansa.)
              val msg = s"Opiskeluoikeuden ${oldestRow.id} aikaleima ${oldestRow.aikaleima} on yli $recentPageOverlapSeconds s tulevaisuudessa"
              logger.error(msg)
              throw new InvalidRequestException(KoskiErrorCategory.internalError(msg))
            } else {
              // Tämä on ihan normaali tilanne - kaikki nämä rivit ovat niin tuoreita että ne pitää
              // palauttaa myös seuraavalla kutsukerralla. Yleensä tässä tilanteessa rivejä
              // on vain pieni määrä - siltä varalta että koko sivu tuli täyteen, viivästetään
              // vastausta hetki, jotta kutsuja ei hae samoja rivejä uudestaan aivan heti.
              if (mayHaveMore) {
                // Jos recentPageOverLapSeconds on 20s, niin odotellaan tässä vaikka 2s
                Thread.sleep(recentPageOverlapSeconds * 1000 / 10)
              }
              cursor
            }
          }
        }
        MuuttuneetOppijatResponse(
          rows.map(_.oppijaOid),
          mayHaveMore = mayHaveMore,
          nextCursor = nextCursor
        )

      case _ =>
        throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Pitää antaa joko timestamp tai cursor"))
    }
  }

  private def parseTimestamp(s: String): Timestamp = {
    try {
      Timestamp.from(OffsetDateTime.parse(s).toInstant)
    } catch {
      case e: DateTimeParseException => throw new InvalidRequestException(KoskiErrorCategory.badRequest.format.pvm())
    }
  }

  // Kursorissa on prefix "v1," jotta tuetaan formaatin vaihtamista jatkossa
  private def createCursor(t: Timestamp, id: Int): String = {
    "v1," + t.toInstant.toString + "," + id
  }
  private def parseCursor(s: String): (Timestamp, Int) = {
    if (s.startsWith("v1,")) {
      val parts = s.split(",")
      (Timestamp.from(Instant.parse(parts(1))), parts(2).toInt)
    } else {
      throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Virheellinen cursor"))
    }
  }
}

private[sure] case class MuuttuneetOppijatResponse(result: Seq[String], mayHaveMore: Boolean, nextCursor: String)
