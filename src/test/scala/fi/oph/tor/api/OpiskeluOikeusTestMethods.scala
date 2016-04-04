package fi.oph.tor.api

import fi.oph.tor.json.Json
import fi.oph.tor.koodisto.{KoodistoViitePalvelu, MockKoodistoPalvelu}
import fi.oph.tor.schema._
import org.json4s._
import org.scalatest.Matchers

trait OpiskeluOikeusTestMethods extends LocalJettyHttpSpecification with Matchers with OpiskeluOikeusData {
  val koodisto: KoodistoViitePalvelu = KoodistoViitePalvelu(MockKoodistoPalvelu)
  val oppijaPath = "/api/oppija"

  implicit def any2j(o: AnyRef): JValue = Json.toJValue(o)

  def putTutkinnonOsaSuoritus[A, K <: Koulutusmoduuli](tutkinnonOsaSuoritus: AmmatillinenTutkinnonosaSuoritus[K], tutkinnonSuoritustapa: Option[Suoritustapa])(f: => A) = {
    val oo = opiskeluoikeus().copy(suoritus = tutkintoSuoritus.copy(suoritustapa = tutkinnonSuoritustapa, osasuoritukset = Some(List(tutkinnonOsaSuoritus))))

    putOpiskeluOikeus(oo)(f)
  }

  def putOpiskeluOikeus[A](opiskeluOikeus: JValue, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOppija(makeOppija(henkilö, List(Json.toJValue(opiskeluoikeus()).merge(opiskeluOikeus))), headers)(f)
  }

  def putHenkilö[A](henkilö: Henkilö)(f: => A): Unit = {
    putOppija(Json.toJValue(Json.fromJValue[TorOppija](makeOppija()).copy(henkilö = henkilö)))(f)
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
