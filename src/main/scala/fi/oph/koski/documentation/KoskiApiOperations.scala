package fi.oph.koski.documentation

import java.sql.Timestamp

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.history.OpiskeluoikeusHistory
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer.serializeWithRoot
import fi.oph.koski.koodisto.{Koodistot, MockKoodistoPalvelu}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.opiskeluoikeus.ValidationResult
import org.json4s.JsonAST.JObject

object KoskiApiOperations extends ApiGroup {
  object koodisto extends ApiGroup {
    val getByKoodistoUri = add(ApiOperation(
      "GET", "/koski/api/koodisto/{nimi}/{versio}",
      "Palauttaa koodiston koodiarvot JSON-muodossa",
      <p></p>,
      Nil,
      List(
        PathParameter("nimi", "Koodiston nimi", Koodistot.koodistot),
        PathParameter("versio", "Koodiston versio", List("latest"))
      ),
      List(
        KoskiErrorCategory.ok.searchOk.copy(exampleResponse = serializeWithRoot(MockKoodistoPalvelu().getLatestVersion("koskiopiskeluoikeudentila").flatMap(MockKoodistoPalvelu().getKoodistoKoodit))),
        KoskiErrorCategory.notFound.koodistoaEiLöydy
      )
    ))
  }
  add(koodisto)

  object henkilö extends ApiGroup {
    val search = add(ApiOperation(
      "GET", "/koski/api/henkilo/search",
      "Etsii henkilöitä annetulla hakusanalla.",
      <p> Hakutuloksissa vain oppijoiden perustiedot.
        Hakusana voi olla hetu, oppija-oid tai nimen osa. Tuloksiin sisällytetään vain ne oppijat,
        joilla on vähintään yksi opiskeluoikeus, johon käyttäjällä on katseluoikeus.</p>,
      Nil,
      List(QueryParameter("query", "Hakusana, joka voi olla hetu, oppija-oid tai nimen osa.", List("eero"))),
      List(
        KoskiErrorCategory.ok.maybeEmptyList.copy(exampleResponse = serializeWithRoot(List(MockOppijat.eero.henkilö))),
        KoskiErrorCategory.badRequest.queryParam.searchTermTooShort,
        KoskiErrorCategory.unauthorized
      )
    ))
    val byHetu = add(ApiOperation(
      "GET", "/koski/api/henkilo/hetu/{hetu}",
      "Palauttaa henkilötiedot hetulla",
      <p></p>,
      Nil,
      List(
        PathParameter("hetu", "Henkilötunnus", List("010101-123N"))
      ),
      List(
        KoskiErrorCategory.ok.maybeEmptyList.copy(exampleResponse = serializeWithRoot(List(MockOppijat.eero.henkilö))),
        KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu
      )
    ))
  }
  add(henkilö)

  object oppija extends ApiGroup {
    val query = add(ApiOperation(
      "GET", "/koski/api/oppija",
      "Palauttaa oppijoiden tiedot annetuilla parametreilla.",
      <p>Sisältää oppijoiden henkilötiedot,
        opiskeluoikeudet suorituksineen.
        Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opiskeluoikeus, johon käyttäjällä on katseluoikeus.</p>,
      Nil,
      hakuParametrit,
      List(
        KoskiErrorCategory.ok.maybeEmptyList.copy(exampleResponse = serializeWithRoot(List(AmmatillinenOldExamples.uusi))),
        KoskiErrorCategory.badRequest.format.pvm,
        KoskiErrorCategory.badRequest.queryParam.unknown,
        KoskiErrorCategory.unauthorized
      )
    ))
    val getByOid = add(ApiOperation(
      "GET", "/koski/api/oppija/{oid}",
      "Hakee oppijan tiedot ja opiskeluoikeudet suorituksineen.",
      <p></p>,
      Nil,
      List(PathParameter("oid", "Oppijan tunniste", List("1.2.246.562.24.00000000001"))),
      List(
        KoskiErrorCategory.ok.searchOk.copy(exampleResponse = serializeWithRoot(AmmatillinenOldExamples.uusi)),
        KoskiErrorCategory.unauthorized,
        KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid,
        KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia
      )
    ))

    val put = add(ApiOperation(
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
            <li>Jos opiskeluoikeudessa on oid-kenttä, päivitetään tällä oid:llä löytyvää opiskeluoikeutta</li>
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

    val post = add(ApiOperation(
      "POST", "/koski/api/oppija",
      "Lisää oppijalle opiskeluoikeuksia.",
      <div>
        <p>Palauttaa objektin, jossa on henkilön <em>oid</em>, eli henkilön yksilöivä tunniste Koski ja Opintopolku-järjestelmissä.
          Lisäksi paluuarvossa on lista päivitetyistä/luoduista opiskeluoikeuksista tunnisteineen ja versioineen.</p>

        <p>
          Syötedata validoidaan json-schemaa ja tiettyjä sisäisiä sääntöjä vasten ja päivitys hyväksytään vain, mikäli validointi menee läpi. Ks. paluukoodit alla.
        </p>
      </div>,
      Examples.examples,
      Nil,
      List(
        KoskiErrorCategory.ok.createdOrUpdated,
        KoskiErrorCategory.conflict.exists,
        KoskiErrorCategory.unauthorized,
        KoskiErrorCategory.forbidden.organisaatio,
        KoskiErrorCategory.badRequest.format,
        KoskiErrorCategory.badRequest.validation,
        KoskiErrorCategory.notFound.oppijaaEiLöydy,
        KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia,
        KoskiErrorCategory.unsupportedMediaType.jsonOnly
      )
    )
    )
  }
  add(oppija)

  object opiskeluoikeus extends ApiGroup {
    val getById = add(ApiOperation(
      "GET", "/koski/api/opiskeluoikeus/{oid}",
      "Palauttaa opiskeluoikeuden tiedot",
      <p></p>,
      Nil,
      List(PathParameter("oid", "Opiskeluoikeuden oid", List("1.2.246.562.15.82898400641"))),
      List(
        KoskiErrorCategory.ok.searchOk.copy(exampleResponse = serializeWithRoot(AmmatillinenOldExamples.uusi.opiskeluoikeudet(0))),
        KoskiErrorCategory.unauthorized,
        KoskiErrorCategory.badRequest.format.number,
        KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia
      )
    ))

    val validateById = add(ApiOperation(
      "GET", "/koski/api/opiskeluoikeus/validate/{oid}",
      "Validoi opiskeluoikeuden datan oikeellisuuden",
      <p>Validoi opiskeluoikeuden datan oikeellisuuden</p>,
      Nil,
      List(PathParameter("oid", "Opiskeluoikeuden oid", List("1.2.246.562.15.82898400641"))),
      List(
        KoskiErrorCategory.ok.maybeValidationErrorsInContent.copy(exampleResponse = serializeWithRoot(ValidationResult(MockOppijat.eero.oid, "1.2.246.562.15.82898400641", List()))),
        KoskiErrorCategory.unauthorized,
        KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid,
        KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia
      )
    ))

    val historyById = add(ApiOperation(
      "GET", "/koski/api/opiskeluoikeus/historia/{opiskeluoikeus_oid}",
      "Listaa tiettyyn opiskeluoikeuteen kohdistuneet muutokset",
      <p></p>,
      Nil,
      List(PathParameter("opiskeluoikeus_oid", "Opiskeluoikeuden tunniste", List("1.2.246.562.15.82898400641"))),
      List(
        KoskiErrorCategory.ok.searchOk.copy(exampleResponse = serializeWithRoot(List(OpiskeluoikeusHistory("1.2.246.562.15.82898400641", 1, new Timestamp(System.currentTimeMillis()), MockUsers.kalle.oid, JObject())))),
        KoskiErrorCategory.unauthorized,
        KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia
      )
    ))

    val historyVersion = add(ApiOperation(
      "GET", "/koski/api/opiskeluoikeus/historia/{opiskeluoikeus_oid}/{versionumero}",
      "Palauttaa opiskeluoikeuden tiedot tietyssä versiossa",
      <p></p>,
      Nil,
      List(
        PathParameter("opiskeluoikeus_oid", "Opiskeluoikeuden tunniste", List("1.2.246.562.15.82898400641")),
        PathParameter("versionumero", "Opiskeluoikeuden versio", List("2"))
      ),
      List(
        KoskiErrorCategory.ok.searchOk.copy(exampleResponse = serializeWithRoot(AmmatillinenOldExamples.uusi)),
        KoskiErrorCategory.unauthorized,
        KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia,
        KoskiErrorCategory.notFound.versiotaEiLöydy
      )
    ))
  }
  add(opiskeluoikeus)

  private lazy val hakuParametrit = List(
    QueryParameter("opiskeluoikeusPäättynytAikaisintaan","Päivämäärä jonka jälkeen opiskeluoikeus on päättynyt",List("2016-01-01")),
    QueryParameter("opiskeluoikeusPäättynytViimeistään","Päivämäärä jota ennen opiskeluoikeus on päättynyt", List("2016-12-31")),
    QueryParameter("opiskeluoikeusAlkanutAikaisintaan","Päivämäärä jonka jälkeen opiskeluoikeus on alkanut",List("2016-01-01")),
    QueryParameter("opiskeluoikeusAlkanutViimeistään","Päivämäärä jota ennen opiskeluoikeus on alkanut", List("2016-12-31")),
    QueryParameter("opiskeluoikeudenTyyppi","Opiskeluoikeuden tyyppi (ks. opiskeluoikeudentyyppi-koodisto)", List("ammatillinenkoulutus")),
    QueryParameter("opiskeluoikeudenTila","Opiskeluoikeuden tila (ks. koskiopiskeluoikeudentila-koodisto)", List("lasna")),
    QueryParameter("suorituksenTyyppi","Juurisuorituksen tyyppi (ks. suorituksentyyppi-koodisto)", List("ammatillinentutkinto")),
    QueryParameter("tutkintohaku","Tekstihaku kohdistuen tutkinnon nimeen, osaamisalaan ja tutkintonimikkeeseen", List("autoalan perustutkinto")),
    QueryParameter("luokkahaku", "Tekstihaku kohdistuen oppilaan nykyiseen/viimeisimpään luokkaan", List("9C")),
    QueryParameter("nimihaku", "Tekstihaku kohdistuen oppilan etunimiin ja sukunimeen", List("virtanen")),
    QueryParameter("muuttunutJälkeen", "Palautetaan vain opiskeluoikeudet, jotka ovat muuttuneet annetun aikaleiman jälkeen", List("2017-11-01T21:00"))
  )
}
