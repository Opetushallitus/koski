package fi.oph.koski.eperusteet

import fi.oph.koski.cache.{CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.http.{Http, HttpStatusException}
import fi.oph.koski.http.Http._
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi

import scala.concurrent.duration.DurationInt

class RemoteEPerusteetRepository(ePerusteetRoot: String, ePerusteetWebBaseUrl: String)(implicit cacheInvalidator: CacheManager) extends EPerusteetRepository {
  private val http: Http = Http(ePerusteetRoot, "eperusteet")

  override protected def webBaseUrl: String = ePerusteetWebBaseUrl

  def findPerusteet(query: String): List[EPeruste] = {
    runIO(http.get(uri"/api/perusteet?sivukoko=100&nimi=${query}")(Http.parseJson[EPerusteet])).data
  }

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste] = {
    runIO(http.get(uri"/api/perusteet?diaarinumero=${diaarinumero}")(Http.parseJson[EPerusteet])).data
  }

  def findPerusteetByKoulutustyyppi(koulutustyypit: Set[Koulutustyyppi]): List[EPeruste] = if (koulutustyypit.isEmpty) {
    Nil
  } else {
    val url = s"/api/perusteet?${koulutustyypit.map(k => s"koulutustyyppi=koulutustyyppi_${k.koodiarvo}").mkString("&")}".toUri
    runIO(http.get(url)(Http.parseJson[EPerusteet])).data
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    findPerusteenYksilöintitiedot(diaariNumero)
      .map(e => rakenneCache(e.id.toString).getOrElse(throw new HttpStatusException(404, "Tutkinnon rakennetta ei löytynyt ePerusteista", "GET", s"/api/perusteet/${e.id}/kaikki")))
  }

  private val rakenneCache = KeyValueCache[String, Option[EPerusteRakenne]](
    ExpiringCache("EPerusteetRepository.rakenne", 2.minutes, 50),
    id => runIO(http.get(uri"/api/perusteet/${id}/kaikki")(Http.parseJsonOptional[EPerusteRakenne]))
  )

  def findRakenteet(diaarinumero: String): List[EPerusteRakenne] = {
    runIO(http.get(uri"/api/perusteet?diaarinumero=${diaarinumero}")(Http.parseJson[EPerusteRakenteet])).data
  }

  def findUusinRakenne(diaarinumero: String): Option[EPerusteRakenne] = {
    val rakenteet = findRakenteet(diaarinumero)
    if (rakenteet.nonEmpty) {
      Some(rakenteet.maxBy(_.luotu))
    } else {
      None
    }
  }

  def findPerusteenYksilöintitiedot(diaariNumero: String): Option[EPerusteTunniste] = yksilöintitiedotCache(diaariNumero)

  private val yksilöintitiedotCache = KeyValueCache[String, Option[EPerusteTunniste]](
    ExpiringCache("EPerusteetRepository.yksilöintitiedot", 1.hour, 1000),
    diaariNumero => runIO(http.get(uri"/api/perusteet/diaari?diaarinumero=$diaariNumero")(Http.parseJsonOptional[EPerusteTunniste]))
  )
}
