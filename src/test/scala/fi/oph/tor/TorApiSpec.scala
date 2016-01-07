package fi.oph.tor

import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json.toJValue
import org.json4s.JValue
import org.scalatest.{FunSpec, Matchers}

class TorApiSpec extends FunSpec with Matchers with HttpSpecification {
  override def baseUrl = SharedJetty.baseUrl
  val oppijaPath = "/api/oppija"

  val defaultHenkilö = toJValue(Map(
    "etunimet" -> "Testi",
    "sukunimi" -> "Toivola",
    "kutsumanimi" -> "Testi",
    "hetu" -> "010101-123N"
  ))

  def makeOppija(henkilö: JValue = defaultHenkilö, opiskeluOikeudet: List[JValue] = List(defaultOpiskeluOikeus)) = toJValue(Map(
    "henkilö" -> henkilö,
    "opiskeluoikeudet" -> opiskeluOikeudet
  ))

  val defaultOpiskeluOikeus: JValue = toJValue(Map(
    "oppilaitos" ->  Map("oid" ->  "1"),
    "suoritus" ->  Map(
    "koulutusmoduulitoteutus" ->  Map(
      "koulutusmoduuli" ->  Map(
        "tunniste" ->  Map(
          "koodiarvo" ->  "351301",
          "nimi" ->  "Autoalan perustutkinto",
          "koodistoUri" ->  "koulutus"),
        "perusteenDiaarinumero" ->  "39/011/2014")),
    "toimipiste" ->  Map(
    "oid" ->  "1.2.246.562.10.42456023292",
    "nimi" ->  "Stadin ammattiopisto, Lehtikuusentien toimipaikka"
    )
  )))


  def putOpiskeluOikeusAjax[A](opiskeluOikeus: Map[String, Any])(f: => A) = {
    putOppijaAjax(makeOppija(defaultHenkilö, List(defaultOpiskeluOikeus.merge(Json.toJValue(opiskeluOikeus)))))(f)
  }

  def putOppijaAjax[A](oppija: Map[String, AnyRef])(f: => A): Unit = putOppijaAjax(toJValue(oppija))(f)

  def putOppijaAjax[A](oppija: JValue)(f: => A): Unit = {
    val jsonString = Json.write(makeOppija().merge(oppija), true)
    put("api/oppija", body = jsonString, headers = authHeaders ++ jsonContent)(f)
  }

  def sendAjax[A](path: String, contentType: String, content: String, method: String)(f: => A): Unit = {
    submit(method, path, body = content.getBytes("UTF-8"), headers = authHeaders ++ jsonContent) (f)
  }

  def verifyResponseCode(expectedStatus: Int, expectedText: String = "") {
    body should include(expectedText)
    verifyResponseStatus(expectedStatus)
  }

  SharedJetty.start

  describe("Valideilla tiedoilla") {
    it("palautetaan HTTP 200") { putOpiskeluOikeusAjax(Map()) (verifyResponseCode(200)) }
  }

  describe("Kun opinto-oikeutta yritetään lisätä oppilaitokseen, johon käyttäjällä ei ole pääsyä") {
    it("palautetaan HTTP 403 virhe" ) { putOpiskeluOikeusAjax(Map("oppilaitos" -> Map("oid" -> "1.2.246.562.10.346830761110"))) (verifyResponseCode(
      403, "Ei oikeuksia organisatioon 1.2.246.562.10.346830761110"))
  }}

  describe("Kun opinto-oikeutta yritetään lisätä oppilaitokseen, jota ei löydy organisaatiopalvelusta") {
    it("palautetaan HTTP 400 virhe" ) (putOpiskeluOikeusAjax(Map("oppilaitos" -> Map("oid" -> "tuuba"))) (verifyResponseCode(400, "Organisaatiota tuuba ei löydy organisaatiopalvelusta")))
  }

  describe("Nimenä tyhjä merkkijono") {
    it("palautetaan HTTP 400 virhe" ) (putOppijaAjax(toJValue(Map("henkilö" -> Map("sukunimi" -> ""))))(verifyResponseCode(400)))
  }

  describe("Epäkelpo JSON-dokumentti") {
    it("palautetaan HTTP 400 virhe" ) (sendAjax("api/oppija", "application/json", "not json", "put")(verifyResponseCode(400, "Invalid JSON")))
  }

  describe("Kun yritetään lisätä opinto-oikeus virheelliseen perusteeseen") {
    it("palautetaan HTTP 400 virhe" ) {
      putOpiskeluOikeusAjax(Map("suoritus" -> Map("koulutusmoduulitoteutus" -> Map("koulutusmoduuli" -> Map("perusteenDiaarinumero" -> "39/xxx/2014")))))(
        verifyResponseCode(400, "Tutkinnon peruste on virheellinen: 39/xxx/2014")
      )
    }
  }

  describe("Kun yritetään lisätä opinto-oikeus ilman perustetta") {
    it("palautetaan HTTP 400 virhe" ) {
      putOpiskeluOikeusAjax(Map("suoritus" -> Map(
        "koulutusmoduulitoteutus" -> Map(
          "koulutusmoduuli" -> Map(
            "perusteenDiaarinumero"-> ""
          )
        )
      )))
      {verifyResponseCode(400, "perusteenDiaarinumero")}
    }
  }

  describe("Hetun ollessa") {
    describe("muodoltaan virheellinen") {
      it("palautetaan HTTP 400 virhe" ) (putOppijaAjax(Map("henkilö" -> Map("hetu" -> "010101-123123")))(verifyResponseCode(400, "Virheellinen muoto hetulla: 010101-123123")))
    }
    describe("muodoltaan oikea, mutta väärä tarkistusmerkki") {
      it("palautetaan HTTP 400 virhe" ) (putOppijaAjax(Map("henkilö" -> Map("hetu" -> "010101-123P")))(verifyResponseCode(400, "Virheellinen tarkistusmerkki hetussa: 010101-123P")))
    }
    describe("päivämäärältään tulevaisuudessa") {
      it("palautetaan HTTP 400 virhe" ) (putOppijaAjax(Map("henkilö" -> Map("hetu" -> "141299A903C")))(verifyResponseCode(400, "Syntymäpäivä hetussa: 141299A903C on tulevaisuudessa")))
    }
    describe("päivämäärältään virheellinen") {
      it("palautetaan HTTP 400 virhe" ) (putOppijaAjax(Map("henkilö" -> Map("hetu" -> "300215-123T")))(verifyResponseCode(400, "Virheellinen syntymäpäivä hetulla: 300215-123T")))
    }
    describe("validi") {
      it("palautetaan HTTP 200" ) (putOppijaAjax(Map("henkilö" -> Map("hetu" -> "010101-123N")))(verifyResponseCode(200)))
    }
  }

  describe("Opiskeluoikeuden päivämäärät") {
    describe("Päivämäärät kunnossa") {
      it("palautetaan HTTP 200" ) (putOpiskeluOikeusAjax(Map(
        "alkamispäivä" -> "2015-08-01",
        "päättymispäivä" -> "2016-05-31",
        "arvioituPäättymispäivä" -> "2018-05-31"
      ))(verifyResponseCode(200)))
    }
    describe("Päivämääräformaatti virheellinen") {
      it("palautetaan HTTP 400" ) (putOpiskeluOikeusAjax(Map(
        "alkamispäivä" -> "2015.01-12"
      ))(verifyResponseCode(400, "Virheellinen päivämäärä: 2015.01-12")))
    }
    describe("Päivämäärä virheellinen") {
      it("palautetaan HTTP 400" ) (putOpiskeluOikeusAjax(Map(
        "alkamispäivä" -> "2015-01-32"
      ))(verifyResponseCode(400, "Virheellinen päivämäärä: 2015-01-32")))
    }
    describe("Väärä päivämääräjärjestys") {
      it("alkamispäivä > päättymispäivä" ) (putOpiskeluOikeusAjax(Map(
        "alkamispäivä" -> "2015-08-01",
        "päättymispäivä" -> "2014-05-31"
      ))(verifyResponseCode(400, "alkamispäivä (2015-08-01) oltava sama tai aiempi kuin päättymispäivä(2014-05-31)")))

      it("alkamispäivä > arvioituPäättymispäivä" ) (putOpiskeluOikeusAjax(Map(
        "alkamispäivä" -> "2015-08-01",
        "arvioituPäättymispäivä" -> "2014-05-31"
      ))(verifyResponseCode(400, "alkamispäivä (2015-08-01) oltava sama tai aiempi kuin arvioituPäättymispäivä(2014-05-31)")))
    }
  }

  describe("Suorituksen päivämäärät") {
    describe("Päivämäärät kunnossa") {
      it("palautetaan HTTP 200" ) (putOpiskeluOikeusAjax(Map("suoritus" -> Map("alkamispäivä" -> "2015-08-01", "arviointi" -> List(Map("päivä" -> "2016-05-31", "arvosana" -> Map("koodiarvo" -> "2", "koodistoUri" -> "arviointiasteikkoammatillinent1k3"))), "vahvistus" -> Map("päivä" -> "2016-05-31"))))(verifyResponseCode(200)))
    }

    describe("alkamispäivä > arviointi.päivä") {
      it("palautetaan HTTP 200" ) (putOpiskeluOikeusAjax(Map("suoritus" -> Map("alkamispäivä" -> "2017-08-01", "arviointi" -> List(Map("päivä" -> "2016-05-31", "arvosana" -> Map("koodiarvo" -> "2", "koodistoUri" -> "arviointiasteikkoammatillinent1k3"))))))(verifyResponseCode(400, "suoritus.alkamispäivä (2017-08-01) oltava sama tai aiempi kuin suoritus.arviointi.päivä(2016-05-31)")))
    }

    describe("arviointi.päivä > vahvistus.päivä") {
      it("palautetaan HTTP 200" ) (putOpiskeluOikeusAjax(Map("suoritus" -> Map("arviointi" -> List(Map("päivä" -> "2016-05-31", "arvosana" -> Map("koodiarvo" -> "2", "koodistoUri" -> "arviointiasteikkoammatillinent1k3"))), "vahvistus" -> Map("päivä" -> "2016-05-30"))))(verifyResponseCode(400, "suoritus.arviointi.päivä (2016-05-31) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(2016-05-30)")))
    }
  }
}
