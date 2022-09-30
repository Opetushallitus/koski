package fi.oph.koski.eperusteet

import cats.effect.IO
import fi.oph.koski.cache.{CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.http.{Http, HttpStatusException}
import fi.oph.koski.http.Http._
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi

import java.time.LocalDate
import scala.concurrent.duration.DurationInt


class RemoteEPerusteetRepository(ePerusteetRoot: String, ePerusteetWebBaseUrl: String)(implicit cacheInvalidator: CacheManager) extends EPerusteetRepository {
  private val http: Http = Http(ePerusteetRoot, "eperusteet")

  override protected def webBaseUrl: String = ePerusteetWebBaseUrl
  val externalPerusteetApi = "/api/external/perusteet"
  val externalPerusteApi = "/api/external/peruste"
  val externalPerusteetApiPoistuneillaJaSivukoolla = s"$externalPerusteetApi?poistuneet=true&sivukoko=100"
  val koulutusVientiQuery = "koulutusvienti=true"

  def findPerusteet(query: String): List[EPerusteRakenne] = {
    val program: IO[List[EPerusteOsaRakenne]] = for {
      (perusteetIlmanKoulutusvientiä) <- http.get(uri"$externalPerusteetApiPoistuneillaJaSivukoolla&nimi=${query}")(Http.parseJson[EPerusteOsaRakenteet])
      (perusteetKoulutusviennillä) <- http.get(uri"$externalPerusteetApiPoistuneillaJaSivukoolla&nimi=${query}&$koulutusVientiQuery")(Http.parseJson[EPerusteOsaRakenteet])
    } yield(perusteetIlmanKoulutusvientiä.data ++ perusteetKoulutusviennillä.data.map(_.copy(koulutusvienti = Some(true))))

    runIO(program).sortBy(_.koulutusvienti)
  }

  def findPerusteetByKoulutustyyppi(koulutustyypit: Set[Koulutustyyppi]): List[EPerusteRakenne] = if (koulutustyypit.isEmpty) {
    Nil
  } else {
    val program: IO[List[EPerusteOsaRakenne]] = for {
      (perusteetIlmanKoulutusvientiä) <- http.get(s"$externalPerusteetApiPoistuneillaJaSivukoolla&${koulutustyypit.map(k => s"koulutustyyppi=koulutustyyppi_${k.koodiarvo}").mkString("&")}".toUri)(Http.parseJson[EPerusteOsaRakenteet])
      (perusteetKoulutusviennillä) <- http.get(s"$externalPerusteetApiPoistuneillaJaSivukoolla&$koulutusVientiQuery&${koulutustyypit.map(k => s"koulutustyyppi=koulutustyyppi_${k.koodiarvo}").mkString("&")}".toUri)(Http.parseJson[EPerusteOsaRakenteet])
    } yield(perusteetIlmanKoulutusvientiä.data ++ perusteetKoulutusviennillä.data.map(_.copy(koulutusvienti = Some(true))))

    runIO(program)
  }

  def findTarkatRakenteet(diaariNumero: String, päivä: Option[LocalDate]): List[EPerusteTarkkaRakenne] = {
    findPerusteenYksilöintitiedot(diaariNumero, päivä)
      .map(e => rakenneCache(e.id.toString).getOrElse(throw new HttpStatusException(404, "Tutkinnon rakennetta ei löytynyt ePerusteista", "GET", s"$externalPerusteetApi/${e.id}")))
  }

  private val rakenneCache = KeyValueCache[String, Option[EPerusteKokoRakenne]](
    ExpiringCache("EPerusteetRepository.rakenne", 2.minutes, 50),
    id => runIO(http.get(uri"$externalPerusteApi/${id}")(Http.parseJsonOptional[EPerusteKokoRakenne]))
  )

  def findKaikkiRakenteet(diaarinumero: String): List[EPerusteRakenne] = osaRakenneCache(diaarinumero)

  def findKaikkiPerusteenYksilöintitiedot(diaariNumero: String): List[EPerusteTunniste] =
    osaRakenneCache(diaariNumero).map(_.toEPerusteTunniste)

  private val osaRakenneCache = KeyValueCache[String, List[EPerusteOsaRakenne]](
    ExpiringCache("EPerusteetRepository.osarakenne", 15.minutes, 250),
    diaariNumero => fetchKaikkiRakenteet(diaariNumero)
  )

  private def fetchKaikkiRakenteet(diaarinumero: String): List[EPerusteOsaRakenne] = {
    val program: IO[List[EPerusteOsaRakenne]] = for {
      (perusteetIlmanKoulutusvientiä) <- http.get(uri"$externalPerusteetApiPoistuneillaJaSivukoolla&diaarinumero=${diaarinumero}")(Http.parseJson[EPerusteOsaRakenteet])
      (perusteetKoulutusviennillä) <- http.get(uri"$externalPerusteetApiPoistuneillaJaSivukoolla&$koulutusVientiQuery&diaarinumero=${diaarinumero}")(Http.parseJson[EPerusteOsaRakenteet])
    } yield(perusteetIlmanKoulutusvientiä.data ++ perusteetKoulutusviennillä.data.map(_.copy(koulutusvienti = Some(true))))

    runIO(program)
  }
}
