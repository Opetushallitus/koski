package fi.oph.koski.eperusteet

import fi.oph.koski.cache.{CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http._
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi

import scala.concurrent.duration._

class RemoteEPerusteetRepository(ePerusteetRoot: String, ePerusteetWebBaseUrl: String)(implicit cacheInvalidator: CacheManager) extends EPerusteetRepository {
  private val http: Http = Http(ePerusteetRoot)

  override protected def webBaseUrl: String = ePerusteetWebBaseUrl

  def findPerusteet(query: String): List[EPeruste] = {
    runTask(http.get(uri"/api/perusteet?sivukoko=100&nimi=${query}")(Http.parseJson[EPerusteet])).data
  }

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste] = {
    runTask(http.get(uri"/api/perusteet?diaarinumero=${diaarinumero}")(Http.parseJson[EPerusteet])).data
  }

  def findPerusteetByKoulutustyyppi(koulutustyypit: Set[Koulutustyyppi]): List[EPeruste] = if (koulutustyypit.isEmpty) {
    Nil
  } else {
    val url = s"/api/perusteet?${koulutustyypit.map(k => s"koulutustyyppi=koulutustyyppi_${k.koodiarvo}").mkString("&")}"
    runTask(http.get(ParameterizedUriWrapper(uriFromString(url), url))(Http.parseJson[EPerusteet])).data
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    findPerusteenYksilöintitiedot(diaariNumero)
      .map(e => runTask(http.get(uri"/api/perusteet/${e.id}/kaikki")(Http.parseJson[EPerusteRakenne])))
  }

  def findPerusteenYksilöintitiedot(diaariNumero: String): Option[EPerusteTunniste] = yksilöintitiedotCache(diaariNumero)

  private val yksilöintitiedotCache = KeyValueCache[String, Option[EPerusteTunniste]](
    ExpiringCache("EPerusteetRepository.yksilöintitiedot", 1 hour, 1000),
    diaariNumero => runTask(http.get(uri"/api/perusteet/diaari?diaarinumero=$diaariNumero")(Http.parseJsonOptional[EPerusteTunniste]))
  )
}
