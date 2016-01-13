package fi.oph.tor.api

import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._
import org.json4s._
import org.scalatest.Matchers

trait OpiskeluOikeusTestMethods extends HttpSpecification with Matchers {
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

  val defaultTutkinnonOsaSuoritus = toJValue(Map(
    "koulutusmoduulitoteutus" ->  Map(
      "koulutusmoduuli" ->  Map(
        "tunniste" ->  Map("koodiarvo" -> "100023", "nimi" -> "Markkinointi ja asiakaspalvelu", "koodistoUri" -> "tutkinnonosat", "koodistoVersio" -> 1),
        "pakollinen" -> true,
        "laajuus" ->  Map("arvo" -> 11, "yksikkö" -> Map("koodiarvo" -> "6", "koodistoUri" -> "opintojenlaajuusyksikko"))
      )
    ),
    "toimipiste" ->  Map("oid" -> "1.2.246.562.10.42456023292", "nimi" -> "Stadin ammattiopisto, Lehtikuusentien toimipaikka"),
    "arviointi" -> List(Map("arvosana" -> Map("koodiarvo" -> "2", "koodistoUri" -> "arviointiasteikkoammatillinent1k3")))
  ))

  def putTutkinnonOsaSuoritusAjax[A](tutkinnonOsaSuoritus: Map[String, Any])(f: => A) = {
    val opiskeluOikeus = defaultOpiskeluOikeus.merge(toJValue(Map(
      "suoritus" -> Map(
        "koulutusmoduulitoteutus" -> Map(
          "suoritustapa" -> Map(
            "tunniste"  -> Map(
              "koodiarvo"  -> "naytto",
              "nimi"  -> "Näyttö",
              "koodistoUri" -> "suoritustapa",
              "koodistoVersio" -> 1
            )
          )
        ),
        "osasuoritukset" -> List(defaultTutkinnonOsaSuoritus.merge(toJValue(tutkinnonOsaSuoritus)))
      )
    )))

  }


  def putOpiskeluOikeus[A](opiskeluOikeus: Map[String, Any], henkilö: JValue = defaultHenkilö)(f: => A) = {
    putOppija(makeOppija(henkilö, List(defaultOpiskeluOikeus.merge(Json.toJValue(opiskeluOikeus)))))(f)
  }

  def putOppija[A](oppija: Map[String, AnyRef])(f: => A): Unit = {
    val json: JValue = makeOppija().merge(toJValue(oppija))
    putOppija(json)(f)
  }

  def putOppija[A](oppija: JValue)(f: => A): A = {
    val jsonString = Json.write(oppija, true)
    put("api/oppija", body = jsonString, headers = authHeaders ++ jsonContent)(f)
  }

  def sendAjax[A](path: String, contentType: String, content: String, method: String)(f: => A): Unit = {
    submit(method, path, body = content.getBytes("UTF-8"), headers = authHeaders ++ jsonContent) (f)
  }

  def verifyResponseCode(expectedStatus: Int, expectedText: String = "") = {
    body should include(expectedText)
    verifyResponseStatus(expectedStatus)
  }
}
