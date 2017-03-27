package fi.oph.koski.api

import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.schema._
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s._

trait PutOpiskeluoikeusTestMethods[Oikeus <: Opiskeluoikeus] extends OpiskeluoikeusTestMethods with OpiskeluoikeusData[Oikeus] {
  val koodisto: KoodistoViitePalvelu = MockKoodistoViitePalvelu
  val oppijaPath = "/api/oppija"

  implicit def any2j(o: AnyRef): JValue = Json.toJValue(o)

  def putOpiskeluoikeus[A](opiskeluoikeus: Opiskeluoikeus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOppija(makeOppija(henkilö, List(opiskeluoikeus)), headers)(f)
  }

  def putHenkilö[A](henkilö: Henkilö)(f: => A): Unit = {
    import KoskiSchema.deserializationContext
    putOppija(Json.toJValue(SchemaValidatingExtractor.extract[Oppija](makeOppija()).right.get.copy(henkilö = henkilö)))(f)
  }

  def putOppija[A](oppija: JValue, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    val jsonString = Json.write(oppija, true)
    put("api/oppija", body = jsonString, headers = headers)(f)
  }

  def request[A](path: String, contentType: String, content: String, method: String)(f: => A): Unit = {
    submit(method, path, body = content.getBytes("UTF-8"), headers = authHeaders() ++ jsonContent) (f)
  }

  def createOrUpdate(oppija: Henkilö with Hetullinen, opiskeluoikeus: Opiskeluoikeus, check: => Unit = { verifyResponseStatus(200) }) = {
    putOppija(Json.toJValue(Oppija(oppija, List(opiskeluoikeus)))){
      check
      lastOpiskeluoikeusByHetu(oppija)
    }
  }

  def createOpiskeluoikeus[T <: Opiskeluoikeus](oppija: Henkilö with Hetullinen, opiskeluoikeus: T) = {
    resetFixtures
    createOrUpdate(oppija, opiskeluoikeus).asInstanceOf[T]
  }

  def makeOppija(henkilö: Henkilö = defaultHenkilö, opiskeluOikeudet: List[AnyRef] = List(defaultOpiskeluoikeus)): JValue = toJValue(Map(
    "henkilö" -> henkilö,
    "opiskeluoikeudet" -> opiskeluOikeudet
  ))
}
