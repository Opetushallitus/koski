package fi.oph.tor.schema

object TorApiOperations {
 private val hakuParametrit = List(
   QueryParameter("opiskeluoikeusPäättynytAikaisintaan","Päivämäärä jonka jälkeen opiskeluoikeus on päättynyt","2016-01-01"),
   QueryParameter("opiskeluoikeusPäättynytViimeistään","Päivämäärä jota ennen opiskeluoikeus on päättynyt","2016-12-31"),
   QueryParameter("tutkinnonTila","Opiskeluoikeuden juurisuorituksen tila: VALMIS, KESKEN, KESKEYTYNYT","VALMIS")
 )
 val operations = List(
   ApiOperation(
      "GET", "/tor/api/oppija/search",
      <div>Etsii oppijoita annetulla hakusanalla. Hakutuloksissa vain oppijoiden perustiedot. Hakusana voi olla hetu, oppija-oid tai nimen osa. Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opinto-oikeus, johon käyttäjällä on katseluoikeus.</div>,
      Nil,
      List(QueryParameter("query", "Hakusana, joka voi olla hetu, oppija-oid tai nimen osa.", "eero")),
      List(
        (200, "OK, jos haku onnistuu. Myös silloin kun ei löydy yhtään tulosta."),
        (400, "BAD REQUEST, jos hakusana puuttuu"),
        (401,"UNAUTHORIZED, jos käyttäjä ei ole tunnistautunut")
      )
   ),
   ApiOperation(
    "GET", "/tor/api/oppija",
     <div>Palauttaa oppijoiden tiedot annetuilla parametreilla. Sisältää oppijoiden henkilötiedot, opiskeluoikeudet suorituksineen. Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opinto-oikeus, johon käyttäjällä on katseluoikeus.</div>,
     Nil,
     hakuParametrit,
     List(
       (200, "OK, jos haku onnistuu"),
       (400, "BAD REQUEST, jos hakuparametria ei tueta, tai hakuparametri on virheellinen."),
       (401, "UNAUTHORIZED, jos käyttäjä ei ole tunnistautunut")
     )
   ),
   ApiOperation(
     "GET", "/tor/api/oppija/validate",
     <div>Etsii oppijat annetuilla parametreilla ja validoi hakutulokset. Validointi suoritetaan tämän hetkisen JSON-scheman ja muiden validointisääntöjen mukaan. Lisäksi validoidaan opinto-oikeuksien versiohistorioiden eheys. Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opinto-oikeus, johon käyttäjällä on katseluoikeus.</div>,
     Nil,
     hakuParametrit,
     List(
       (200, "OK, jos haku onnistuu. Mahdolliset validointivirheet palautuu json-vastauksessa."),
       (400, "BAD REQUEST, jos hakuparametria ei tueta, tai hakuparametri on virheellinen."),
       (401, "UNAUTHORIZED, jos käyttäjä ei ole tunnistautunut")
     )
   ),
   ApiOperation(
     "GET", "/tor/api/oppija/{oid}",
     <div>Hakee oppijan tiedot ja opiskeluoikeudet suorituksineen.</div>,
     Nil,
     List(PathParameter("oid", "Oppijan tunniste", "1.2.246.562.24.00000000001")),
     List(
       (200, "OK, jos haku onnistuu."),
       (401, "UNAUTHORIZED, jos käyttäjä ei ole tunnistautunut"),
       (404, "NOT FOUND, jos oppijaa ei löydy tai käyttäjällä ei ole oikeuksia oppijan tietojen katseluun.")
     )
   ),
   ApiOperation(
     "GET", "/tor/api/oppija/validate/{oid}",
     <div>Validoi oppijan kantaan tallennetun datan oikeellisuuden</div>,
     Nil,
     List(PathParameter("oid", "Oppijan tunniste", "1.2.246.562.24.00000000001")),
     List(
       (200, "OK, jos haku onnistuu. Mahdolliset validointivirheet palautuu json-vastauksessa."),
       (401, "UNAUTHORIZED, jos käyttäjä ei ole tunnistautunut"),
       (404, "NOT FOUND, jos oppijaa ei löydy tai käyttäjällä ei ole oikeuksia oppijan tietojen katseluun.")
     )
   ),
   ApiOperation(
     "GET", "/tor/api/opiskeluoikeus/historia/{opiskeluoikeus_id}",
     <div>Listaa tiettyyn opiskeluoikeuteen kohdistuneet muutokset</div>,
     Nil,
     List(PathParameter("opiskeluoikeus_id", "Opiskeluoikeuden tunniste", "354")),
     List(
       (200, "OK, jos haku onnistuu."),
       (401, "UNAUTHORIZED, jos käyttäjä ei ole tunnistautunut."),
       (404, "NOT FOUND, opiskeluoikeutta ei löydy.")
     )
   ),
   ApiOperation(
     "GET", "/tor/api/opiskeluoikeus/historia/{opiskeluoikeus_id}/{versionumero}",
     <div>Palauttaa opiskeluoikeuden tiedot tietyssä versiossa</div>,
     Nil,
     List(
       PathParameter("opiskeluoikeus_id", "Opiskeluoikeuden tunniste", "354"),
       PathParameter("versionumero", "Opiskeluoikeuden versio", "2")
     ),
     List(
       (200, "OK, jos haku onnistuu."),
       (401, "UNAUTHORIZED, jos käyttäjä ei ole tunnistautunut."),
       (404, "NOT FOUND, opiskeluoikeutta ei löydy tai versiota ei löydy")
     )
   ),
   ApiOperation(
     "PUT", "/tor/api/oppija",
     <div>
       <p>Lisää/päivittää oppijan ja opiskeluoikeuksia. Palauttaa henkilön <em>oid</em>-arvon, eli henkilön yksilöivän tunnisteen TOR ja Opintopolku-järjestelmissä.</p>
       <p>
         Tallennettava henkilö tunnistetaan joko henkilötunnuksen tai <em>oid</em>in perusteella. Tietojen päivittäminen on huomattavasti
         tehokkaampaa käytettäessä oidia, joten sen käyttöä suositellaan vahvasti. Jos lähdejärjestelmässä ei alun perin ole oideja, on ne mahdollista
         kerätä tätä rajapintaa kutsuttaessa; rajapinta palauttaa aina oppijan oidin.
       </p>
     </div>,
     TorOppijaExamples.examples,
     Nil,
     List(
       (200,"OK jos lisäys/päivitys onnistuu"),
       (401,"UNAUTHORIZED, jos käyttäjä ei ole tunnistautunut"),
       (403,"FORBIDDEN, jos käyttäjällä ei ole tarvittavia oikeuksia tiedon päivittämiseen"),
       (400,"BAD REQUEST, jos syöte ei ole validi")
     )
   )
 )
}
