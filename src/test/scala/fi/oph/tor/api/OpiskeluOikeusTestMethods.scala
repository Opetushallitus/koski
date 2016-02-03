package fi.oph.tor.api

import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._
import fi.oph.tor.koodisto.{KoodistoViitePalvelu, MockKoodistoPalvelu}
import fi.oph.tor.schema.{FullHenkilö, OpiskeluOikeus, Suoritus, TorOppija}
import org.json4s._
import org.scalatest.Matchers

trait OpiskeluOikeusTestMethods extends HttpSpecification with Matchers with OpiskeluOikeusData {
  val koodisto: KoodistoViitePalvelu = KoodistoViitePalvelu(MockKoodistoPalvelu)
  val oppijaPath = "/api/oppija"

  implicit def m2j(map: Map[String, Any]): JValue = Json.toJValue(map)

  def putTutkinnonOsaSuoritus[A](tutkinnonOsaSuoritus: Suoritus)(f: => A) = {
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
        "osasuoritukset" -> List(Json.toJValue(tutkinnonOsaSuoritus))
      )
    )))
    putOpiskeluOikeus(opiskeluOikeus)(f)
  }

  def putOpiskeluOikeus[A](opiskeluOikeus: JValue, henkilö: JValue = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOppija(makeOppija(henkilö, List(defaultOpiskeluOikeus.merge(opiskeluOikeus))), headers)(f)
  }

  def putOppija[A](oppija: Map[String, AnyRef])(f: => A): Unit = {
    val json: JValue = makeOppija().merge(toJValue(oppija))
    putOppija(json)(f)
  }

  def putOppija[A](oppija: JValue, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    val jsonString = Json.write(oppija, true)
    put("api/oppija", body = jsonString, headers = headers)(f)
  }

  def request[A](path: String, contentType: String, content: String, method: String)(f: => A): Unit = {
    submit(method, path, body = content.getBytes("UTF-8"), headers = authHeaders() ++ jsonContent) (f)
  }

  def createOrUpdate(oppija: FullHenkilö, opiskeluOikeus: OpiskeluOikeus, check: => Unit = { verifyResponseStatus(200) }) = {
    putOppija(Json.toJValue(TorOppija(oppija, List(opiskeluOikeus))))(check)
    lastOpiskeluOikeus(oppija.oid)
  }

  def createOpiskeluOikeus(oppija: FullHenkilö, opiskeluOikeus: OpiskeluOikeus) = {
    resetFixtures
    createOrUpdate(oppija, opiskeluOikeus)
    lastOpiskeluOikeus(oppija.oid)
  }

  def lastOpiskeluOikeus(oppijaOid: String) = {
    authGet("api/oppija/" + oppijaOid) {
      verifyResponseStatus(200)
      Json.read[TorOppija](body).opiskeluoikeudet.last
    }
  }
}
