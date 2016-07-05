package fi.oph.koski.documentation

import fi.oph.koski.http.KoskiErrorCategory

object KoskiApiOperations {
 private val hakuParametrit = List(
   QueryParameter("opiskeluoikeusPäättynytAikaisintaan","Päivämäärä jonka jälkeen opiskeluoikeus on päättynyt","2016-01-01"),
   QueryParameter("opiskeluoikeusPäättynytViimeistään","Päivämäärä jota ennen opiskeluoikeus on päättynyt","2016-12-31"),
   QueryParameter("tutkinnonTila","Opiskeluoikeuden juurisuorituksen tila: VALMIS, KESKEN, KESKEYTYNYT","VALMIS")
 )

 val operations = List(
   ApiOperation(
      "GET", "/koski/api/oppija/search",
      <p> Etsii oppijoita annetulla hakusanalla. Hakutuloksissa vain oppijoiden perustiedot.
            Hakusana voi olla hetu, oppija-oid tai nimen osa. Tuloksiin sisällytetään vain ne oppijat,
            joilla on vähintään yksi opinto-oikeus, johon käyttäjällä on katseluoikeus.</p>,
      Nil,
      List(QueryParameter("query", "Hakusana, joka voi olla hetu, oppija-oid tai nimen osa.", "eero")),
      List(
        KoskiErrorCategory.ok.maybeEmptyList,
        KoskiErrorCategory.badRequest.queryParam.searchTermTooShort,
        KoskiErrorCategory.unauthorized
      )
   ),
   ApiOperation(
    "GET", "/koski/api/oppija",
     <p>Palauttaa oppijoiden tiedot annetuilla parametreilla. Sisältää oppijoiden henkilötiedot,
          opiskeluoikeudet suorituksineen.
          Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opinto-oikeus, johon käyttäjällä on katseluoikeus.</p>,
     Nil,
     hakuParametrit,
     List(
       KoskiErrorCategory.ok.maybeEmptyList,
       KoskiErrorCategory.badRequest.format.pvm,
       KoskiErrorCategory.badRequest.queryParam.unknown,
       KoskiErrorCategory.unauthorized
     )
   ),
   ApiOperation(
     "GET", "/koski/api/oppija/validate",
     <p>Etsii oppijat annetuilla parametreilla ja validoi hakutulokset.
          Validointi suoritetaan tämän hetkisen JSON-scheman ja muiden validointisääntöjen mukaan.
          Lisäksi validoidaan opinto-oikeuksien versiohistorioiden eheys.
          Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opinto-oikeus, johon käyttäjällä on katseluoikeus.</p>,
     Nil,
     hakuParametrit,
     List(
       KoskiErrorCategory.ok.maybeValidationErrorsInContent,
       KoskiErrorCategory.badRequest.format.pvm,
       KoskiErrorCategory.badRequest.queryParam.unknown,
       KoskiErrorCategory.unauthorized
     )
   ),
   ApiOperation(
     "GET", "/koski/api/oppija/{oid}",
     <p>Hakee oppijan tiedot ja opiskeluoikeudet suorituksineen.</p>,
     Nil,
     List(PathParameter("oid", "Oppijan tunniste", "1.2.246.562.24.00000000001")),
     List(
       KoskiErrorCategory.ok.searchOk,
       KoskiErrorCategory.unauthorized,
       KoskiErrorCategory.badRequest.queryParam.virheellinenOid,
       KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia
     )
   ),
   ApiOperation(
     "GET", "/koski/api/oppija/validate/{oid}",
     <p>Validoi oppijan kantaan tallennetun datan oikeellisuuden</p>,
     Nil,
     List(PathParameter("oid", "Oppijan tunniste", "1.2.246.562.24.00000000001")),
     List(
       KoskiErrorCategory.ok.maybeValidationErrorsInContent,
       KoskiErrorCategory.unauthorized,
       KoskiErrorCategory.badRequest.queryParam.virheellinenOid,
       KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia
     )
   ),
   ApiOperation(
     "GET", "/koski/api/opiskeluoikeus/historia/{opiskeluoikeus_id}",
     <p>Listaa tiettyyn opiskeluoikeuteen kohdistuneet muutokset</p>,
     Nil,
     List(PathParameter("opiskeluoikeus_id", "Opiskeluoikeuden tunniste", "354")),
     List(
       KoskiErrorCategory.ok.searchOk,
       KoskiErrorCategory.unauthorized,
       KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia
     )
   ),
   ApiOperation(
     "GET", "/koski/api/opiskeluoikeus/historia/{opiskeluoikeus_id}/{versionumero}",
     <p>Palauttaa opiskeluoikeuden tiedot tietyssä versiossa</p>,
     Nil,
     List(
       PathParameter("opiskeluoikeus_id", "Opiskeluoikeuden tunniste", "354"),
       PathParameter("versionumero", "Opiskeluoikeuden versio", "2")
     ),
     List(
       KoskiErrorCategory.ok.searchOk,
       KoskiErrorCategory.unauthorized,
       KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia,
       KoskiErrorCategory.notFound.versiotaEiLöydy
     )
   ),
   ApiOperation(
     "PUT", "/koski/api/oppija",
     <div>
       <p>Lisää/päivittää oppijan ja opiskeluoikeuksia.
          Palauttaa objektin, jossa on henkilön <em>oid</em>, eli henkilön yksilöivä tunniste Koski ja Opintopolku-järjestelmissä.
         Lisäksi paluuarvossa on lista päivitetyistä/luoduista opiskeluoikeuksista tunnisteineen ja versioineen.</p>
       <p>
         Tallennettava henkilö tunnistetaan joko henkilötunnuksen tai <em>oid</em>in perusteella. Tietojen päivittäminen on huomattavasti
         tehokkaampaa käytettäessä oidia, joten sen käyttöä suositellaan vahvasti. Jos lähdejärjestelmässä ei alun perin ole oideja, on ne mahdollista
         kerätä tätä rajapintaa kutsuttaessa; rajapinta palauttaa aina oppijan oidin.
       </p>
       <p>
         Lisättävä/muokattava opiskeluoikeus tunnistetaan seuraavasti:
         <ol>
           <li>Jos opiskeluoikeudessa on id-kenttä, päivitetään tällä id:llä löytyvää opiskeluoikeutta</li>
           <li>Jos opiskeluoikeudessa on lähdejärjestelmänId-kenttä, päivitetään tällä id:llä löytyvää opiskeluoikeutta, tai tehdään uusi, jollei sellaista löydy.</li>
           <li>Jos opiskeluoikeudessa ei ole kumpaakaan em. kentistä, päivitetään samalla oppilaitos-oidilla ja tyypillä löytyvää opiskeluoikeutta, tai tehdään uusi, jollei sellaista löydy.</li>
         </ol>
         Olemassa olevan opiskeluoikeuden oppilaitosta ja tyyppiä ei koskaan voi vaihtaa.
       </p>
       <p>
         Syötedata validoidaan json-schemaa ja tiettyjä sisäisiä sääntöjä vasten ja päivitys hyväksytään vain, mikäli validointi menee läpi. Ks. paluukoodit alla.
       </p>
     </div>,
     Examples.examples,
     Nil,
     List(
       KoskiErrorCategory.ok.createdOrUpdated,
       KoskiErrorCategory.unauthorized,
       KoskiErrorCategory.forbidden.organisaatio,
       KoskiErrorCategory.badRequest.format,
       KoskiErrorCategory.badRequest.validation,
       KoskiErrorCategory.notFound.oppijaaEiLöydy,
       KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia,
       KoskiErrorCategory.conflict.versionumero,
       KoskiErrorCategory.unsupportedMediaType.jsonOnly
     )
   )
 )
}
