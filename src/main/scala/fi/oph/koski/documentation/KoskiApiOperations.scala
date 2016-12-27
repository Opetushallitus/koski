package fi.oph.koski.documentation

import java.sql.Timestamp

import fi.oph.koski.db.OpiskeluoikeusHistoryRow
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.{Koodistot, MockKoodistoPalvelu}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.opiskeluoikeus.ValidationResult
import org.json4s.JsonAST.JObject

object KoskiApiOperations {
 private val hakuParametrit = List(
   QueryParameter("opiskeluoikeusPäättynytAikaisintaan","Päivämäärä jonka jälkeen opiskeluoikeus on päättynyt",List("2016-01-01")),
   QueryParameter("opiskeluoikeusPäättynytViimeistään","Päivämäärä jota ennen opiskeluoikeus on päättynyt", List("2016-12-31")),
   QueryParameter("opiskeluoikeusAlkanutAikaisintaan","Päivämäärä jonka jälkeen opiskeluoikeus on alkanut",List("2016-01-01")),
   QueryParameter("opiskeluoikeusAlkanutViimeistään","Päivämäärä jota ennen opiskeluoikeus on alkanut", List("2016-12-31")),
   QueryParameter("opiskeluoikeudenTyyppi","Opiskeluoikeuden tyyppi (ks. opiskeluoikeudentyyppi-koodisto)", List("ammatillinenkoulutus")),
   QueryParameter("opiskeluoikeudenTila","Opiskeluoikeuden tila (ks. koskiopiskeluoikeudentila-koodisto)", List("lasna")),
   QueryParameter("suorituksenTyyppi","Juurisuorituksen tyyppi (ks. suorituksentyyppi-koodisto)", List("ammatillinentutkinto")),
   QueryParameter("suorituksenTila","Opiskeluoikeuden juurisuorituksen tila (ks suorituksentila-koodisto)", List("VALMIS")),
   QueryParameter("tutkintohaku","Tekstihaku kohdistuen tutkinnon nimeen, osaamisalaan ja tutkintonimikkeeseen", List("autoalan perustutkinto")),
   QueryParameter("luokkahaku", "Tekstihaku kohdistuen oppilaan nykyiseen/viimeisimpään luokkaan", List("9C")),
   QueryParameter("nimihaku", "Tekstihaku kohdistuen oppilan etunimiin ja sukunimeen", List("virtanen"))
 )

 val operations = List(
   ApiOperation(
     "GET", "/koski/api/koodisto/{nimi}/{versio}",
     "Palauttaa koodiston koodiarvot JSON-muodossa",
     <p></p>,
     Nil,
     List(
       PathParameter("nimi", "Koodiston nimi", Koodistot.koodistot),
       PathParameter("versio", "Koodiston versio", List("latest"))
     ),
     List(
       KoskiErrorCategory.ok.searchOk.copy(exampleResponse = MockKoodistoPalvelu().getLatestVersion("koskiopiskeluoikeudentila").flatMap(MockKoodistoPalvelu().getKoodistoKoodit)),
       KoskiErrorCategory.notFound.koodistoaEiLöydy
     )
   ),
   ApiOperation(
      "GET", "/koski/api/henkilo/search",
     "Etsii henkilöitä annetulla hakusanalla.",
      <p> Hakutuloksissa vain oppijoiden perustiedot.
            Hakusana voi olla hetu, oppija-oid tai nimen osa. Tuloksiin sisällytetään vain ne oppijat,
            joilla on vähintään yksi opiskeluoikeus, johon käyttäjällä on katseluoikeus.</p>,
      Nil,
      List(QueryParameter("query", "Hakusana, joka voi olla hetu, oppija-oid tai nimen osa.", List("eero"))),
      List(
        KoskiErrorCategory.ok.maybeEmptyList.copy(exampleResponse = List(MockOppijat.eero.toHenkilötiedotJaOid)),
        KoskiErrorCategory.badRequest.queryParam.searchTermTooShort,
        KoskiErrorCategory.unauthorized
      )
   ),
   ApiOperation(
    "GET", "/koski/api/oppija",
     "Palauttaa oppijoiden tiedot annetuilla parametreilla.",
     <p>Sisältää oppijoiden henkilötiedot,
          opiskeluoikeudet suorituksineen.
          Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opiskeluoikeus, johon käyttäjällä on katseluoikeus.</p>,
     Nil,
     hakuParametrit,
     List(
       KoskiErrorCategory.ok.maybeEmptyList.copy(exampleResponse = List(AmmatillinenOldExamples.uusi)),
       KoskiErrorCategory.badRequest.format.pvm,
       KoskiErrorCategory.badRequest.queryParam.unknown,
       KoskiErrorCategory.unauthorized
     )
   ),
   ApiOperation(
     "GET", "/koski/api/oppija/{oid}",
     "Hakee oppijan tiedot ja opiskeluoikeudet suorituksineen.",
     <p></p>,
     Nil,
     List(PathParameter("oid", "Oppijan tunniste", List("1.2.246.562.24.00000000001"))),
     List(
       KoskiErrorCategory.ok.searchOk.copy(exampleResponse = AmmatillinenOldExamples.uusi),
       KoskiErrorCategory.unauthorized,
       KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid,
       KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia
     )
   ),
   ApiOperation(
     "GET", "/koski/api/opiskeluoikeus/validate",
     "Etsii opiskeluoikeudet annetuilla parametreilla ja validoi hakutulokset.",
     <p>Validointi suoritetaan tämän hetkisen JSON-scheman ja muiden validointisääntöjen mukaan.
       Lisäksi validoidaan opinto-oikeuksien versiohistorioiden eheys.
       Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opiskeluoikeus, johon käyttäjällä on katseluoikeus.</p>,
     Nil,
     List(QueryParameter("errorsOnly", "Haetaanko vain virheelliset opiskeluoikeudet", List("false")), QueryParameter("history", "Validoidaanko myös versiohistoria", List("false")), QueryParameter("henkilö", "Validoidaanko henkilö", List("false"))) ++ hakuParametrit,
     List(
       KoskiErrorCategory.ok.maybeValidationErrorsInContent.copy(exampleResponse = List(ValidationResult(MockOppijat.eero.oid, 8942345, List()))),
       KoskiErrorCategory.badRequest.format.pvm,
       KoskiErrorCategory.badRequest.queryParam.unknown,
       KoskiErrorCategory.unauthorized
     )
   ),
   ApiOperation(
     "GET", "/koski/api/opiskeluoikeus/validate/{id}",
     "Validoi opiskeluoikeuden datan oikeellisuuden",
     <p>Validoi opiskeluoikeuden datan oikeellisuuden</p>,
     Nil,
     List(PathParameter("id", "Opiskeluoikeuden id", List("8942345"))),
     List(
       KoskiErrorCategory.ok.maybeValidationErrorsInContent.copy(exampleResponse = ValidationResult(MockOppijat.eero.oid, 8942345, List())),
       KoskiErrorCategory.unauthorized,
       KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid,
       KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia
     )
   ),
   ApiOperation(
     "GET", "/koski/api/opiskeluoikeus/historia/{opiskeluoikeus_id}",
     "Listaa tiettyyn opiskeluoikeuteen kohdistuneet muutokset",
     <p></p>,
     Nil,
     List(PathParameter("opiskeluoikeus_id", "Opiskeluoikeuden tunniste", List("354"))),
     List(
       KoskiErrorCategory.ok.searchOk.copy(exampleResponse = List(OpiskeluoikeusHistoryRow(8942345, 1, new Timestamp(System.currentTimeMillis()), MockUsers.kalle.oid, JObject()))),
       KoskiErrorCategory.unauthorized,
       KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia
     )
   ),
   ApiOperation(
     "GET", "/koski/api/opiskeluoikeus/historia/{opiskeluoikeus_id}/{versionumero}",
     "Palauttaa opiskeluoikeuden tiedot tietyssä versiossa",
     <p></p>,
     Nil,
     List(
       PathParameter("opiskeluoikeus_id", "Opiskeluoikeuden tunniste", List("354")),
       PathParameter("versionumero", "Opiskeluoikeuden versio", List("2"))
     ),
     List(
       KoskiErrorCategory.ok.searchOk.copy(exampleResponse = AmmatillinenOldExamples.uusi),
       KoskiErrorCategory.unauthorized,
       KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia,
       KoskiErrorCategory.notFound.versiotaEiLöydy
     )
   ),
   ApiOperation(
     "PUT", "/koski/api/oppija",
     "Lisää/päivittää oppijan ja opiskeluoikeuksia.",
     <div>
       <p>Palauttaa objektin, jossa on henkilön <em>oid</em>, eli henkilön yksilöivä tunniste Koski ja Opintopolku-järjestelmissä.
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
