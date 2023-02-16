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

  def findPerusteet(query: String): List[EPerusteRakenne] = {
    val program: IO[List[EPerusteOsaRakenne]] = for {
      (perusteetIlmanKoulutusvientiä) <- http.get(uri"/api/external/perusteet?poistuneet=true&sivukoko=100&nimi=${query}")(Http.parseJson[EPerusteOsaRakenteet])
      (perusteetKoulutusviennillä) <- http.get(uri"/api/external/perusteet?poistuneet=true&sivukoko=100&nimi=${query}&koulutusvienti=true")(Http.parseJson[EPerusteOsaRakenteet])
    } yield(perusteetIlmanKoulutusvientiä.data ++ perusteetKoulutusviennillä.data.map(_.copy(koulutusvienti = Some(true))))

    runIO(program).sortBy(_.koulutusvienti)
  }

  def findPerusteentiedot(id: String): EPerusteKokoRakenne = {
    val program: IO[EPerusteKokoRakenne] = for {
      (peruste) <- http.get(uri"/api/external/peruste/${id}")(Http.parseJson[EPerusteKokoRakenne])
    } yield (peruste)

    runIO(program)
  }

  def findPerusteetByKoulutustyyppi(koulutustyypit: Set[Koulutustyyppi]): List[EPerusteRakenne] = if (koulutustyypit.isEmpty) {
    Nil
  } else {
    val program: IO[List[EPerusteOsaRakenne]] = for {
      (perusteetIlmanKoulutusvientiä) <- http.get(s"/api/external/perusteet?poistuneet=true&sivukoko=100&${koulutustyypit.map(k => s"koulutustyyppi=koulutustyyppi_${k.koodiarvo}").mkString("&")}".toUri)(Http.parseJson[EPerusteOsaRakenteet])
      (perusteetKoulutusviennillä) <- http.get(s"/api/external/perusteet?poistuneet=true&koulutusvienti=true&sivukoko=100&${koulutustyypit.map(k => s"koulutustyyppi=koulutustyyppi_${k.koodiarvo}").mkString("&")}".toUri)(Http.parseJson[EPerusteOsaRakenteet])
    } yield(perusteetIlmanKoulutusvientiä.data ++ perusteetKoulutusviennillä.data.map(_.copy(koulutusvienti = Some(true))))

    runIO(program)
  }

  def findTarkatRakenteet(diaariNumero: String, päivä: Option[LocalDate]): List[EPerusteTarkkaRakenne] = {
    findPerusteenYksilöintitiedot(diaariNumero, päivä)
      .map(e => rakenneCache(e.id.toString).getOrElse(throw new HttpStatusException(404, "Tutkinnon rakennetta ei löytynyt ePerusteista", "GET", s"/api/external/perusteet/${e.id}")))
  }

  private val rakenneCache = KeyValueCache[String, Option[EPerusteKokoRakenne]](
    ExpiringCache("EPerusteetRepository.rakenne", 2.minutes, 50),
    id => runIO(http.get(uri"/api/external/peruste/${id}")(Http.parseJsonOptional[EPerusteKokoRakenne]))
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
      (perusteetIlmanKoulutusvientiä) <- http.get(uri"/api/external/perusteet?poistuneet=true&sivukoko=100&diaarinumero=${diaarinumero}")(Http.parseJson[EPerusteOsaRakenteet])
      (perusteetKoulutusviennillä) <- http.get(uri"/api/external/perusteet?poistuneet=true&sivukoko=100&koulutusvienti=true&diaarinumero=${diaarinumero}")(Http.parseJson[EPerusteOsaRakenteet])
    } yield(perusteetIlmanKoulutusvientiä.data ++ perusteetKoulutusviennillä.data.map(_.copy(koulutusvienti = Some(true))))

    runIO(program)
  }
}
