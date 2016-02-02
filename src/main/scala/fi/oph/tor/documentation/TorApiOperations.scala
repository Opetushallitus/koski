package fi.oph.tor.documentation

import fi.oph.tor.http.TorErrorCategory

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
        TorErrorCategory.ok.maybeEmptyList,
        TorErrorCategory.badRequest.queryParam.searchTermTooShort,
        TorErrorCategory.unauthorized
      )
   ),
   ApiOperation(
    "GET", "/tor/api/oppija",
     <div>Palauttaa oppijoiden tiedot annetuilla parametreilla. Sisältää oppijoiden henkilötiedot, opiskeluoikeudet suorituksineen. Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opinto-oikeus, johon käyttäjällä on katseluoikeus.</div>,
     Nil,
     hakuParametrit,
     List(
       TorErrorCategory.ok.maybeEmptyList,
       TorErrorCategory.badRequest.format.pvm,
       TorErrorCategory.badRequest.queryParam.unknown,
       TorErrorCategory.unauthorized
     )
   ),
   ApiOperation(
     "GET", "/tor/api/oppija/validate",
     <div>Etsii oppijat annetuilla parametreilla ja validoi hakutulokset. Validointi suoritetaan tämän hetkisen JSON-scheman ja muiden validointisääntöjen mukaan. Lisäksi validoidaan opinto-oikeuksien versiohistorioiden eheys. Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opinto-oikeus, johon käyttäjällä on katseluoikeus.</div>,
     Nil,
     hakuParametrit,
     List(
       TorErrorCategory.ok.maybeValidationErrorsInContent,
       TorErrorCategory.badRequest.format.pvm,
       TorErrorCategory.badRequest.queryParam.unknown,
       TorErrorCategory.unauthorized // TODO: virhekoodit aina vamat kuin ylemmässä halu-apissa, refactor
     )
   ),
   ApiOperation(
     "GET", "/tor/api/oppija/{oid}",
     <div>Hakee oppijan tiedot ja opiskeluoikeudet suorituksineen.</div>,
     Nil,
     List(PathParameter("oid", "Oppijan tunniste", "1.2.246.562.24.00000000001")),
     List(
       TorErrorCategory.ok.searchOk,
       TorErrorCategory.unauthorized,
       TorErrorCategory.notFound.notFoundOrNoPermission
     )
   ),
   ApiOperation(
     "GET", "/tor/api/oppija/validate/{oid}",
     <div>Validoi oppijan kantaan tallennetun datan oikeellisuuden</div>,
     Nil,
     List(PathParameter("oid", "Oppijan tunniste", "1.2.246.562.24.00000000001")),
     List(
       TorErrorCategory.ok.maybeValidationErrorsInContent,
       TorErrorCategory.unauthorized,
       TorErrorCategory.notFound.notFoundOrNoPermission
     )
   ),
   ApiOperation(
     "GET", "/tor/api/opiskeluoikeus/historia/{opiskeluoikeus_id}",
     <div>Listaa tiettyyn opiskeluoikeuteen kohdistuneet muutokset</div>,
     Nil,
     List(PathParameter("opiskeluoikeus_id", "Opiskeluoikeuden tunniste", "354")),
     List(
       TorErrorCategory.ok.searchOk,
       TorErrorCategory.unauthorized,
       TorErrorCategory.notFound.notFoundOrNoPermission
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
       TorErrorCategory.ok.searchOk,
       TorErrorCategory.unauthorized,
       TorErrorCategory.notFound.notFoundOrNoPermission
     )
   ),
   ApiOperation(
     "PUT", "/tor/api/oppija",
     <div>
       <p>Lisää/päivittää oppijan ja opiskeluoikeuksia. Palauttaa objektin, jossa on henkilön <em>oid</em>, eli henkilön yksilöivä tunniste TOR ja Opintopolku-järjestelmissä. Lisäksi paluuarvossa on lista päivitetyistä/luoduista opiskeluoikeuksista tunnisteineen ja versioineen.</p>
       <p>
         Tallennettava henkilö tunnistetaan joko henkilötunnuksen tai <em>oid</em>in perusteella. Tietojen päivittäminen on huomattavasti
         tehokkaampaa käytettäessä oidia, joten sen käyttöä suositellaan vahvasti. Jos lähdejärjestelmässä ei alun perin ole oideja, on ne mahdollista
         kerätä tätä rajapintaa kutsuttaessa; rajapinta palauttaa aina oppijan oidin.
       </p>
     </div>,
     TorOppijaExamples.examples,
     Nil,
     List(
       TorErrorCategory.ok.createdOrUpdated,
       TorErrorCategory.unauthorized,
       TorErrorCategory.forbidden.organisaatio,
       TorErrorCategory.badRequest.format,
       TorErrorCategory.badRequest.validation,
       TorErrorCategory.conflict.versionumero
     )
   )
 )
}
