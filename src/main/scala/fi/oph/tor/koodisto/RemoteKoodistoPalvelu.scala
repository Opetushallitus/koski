package fi.oph.tor.koodisto

import fi.oph.tor.http.{Http, VirkailijaHttpClient}
import fi.vm.sade.utils.slf4j.Logging
import org.http4s.{Method, Request}

import scalaz.concurrent.Task

class RemoteKoodistoPalvelu(username: String, password: String, virkailijaUrl: String) extends KoodistoPalvelu with Logging {
  val virkalijaClient = new VirkailijaHttpClient(username, password, virkailijaUrl, "/koodisto-service")
  val http = virkalijaClient.httpClient

  def getKoodistoKoodit(koodisto: KoodistoViittaus): Option[List[KoodistoKoodi]] = {
    http(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codeelement/codes/" + koodisto + noCache))(Http.parseJsonOptional[List[KoodistoKoodi]])
  }

  def getKoodisto(koodisto: KoodistoViittaus): Option[Koodisto] = {
    http(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codes/" + koodisto + noCache))(Http.parseJsonOptional[Koodisto])
  }

  def getLatestVersion(koodisto: String): Option[Int] = {
    val latestKoodisto: Option[KoodistoWithLatestVersion] = http(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codes/" + koodisto + noCache))(Http.parseJsonIgnoreError[KoodistoWithLatestVersion])
    latestKoodisto.flatMap { latest => Option(latest.latestKoodistoVersio).map(_.versio) }
  }

  private def noCache = "?noCache=" + System.currentTimeMillis()

  def createKoodisto(koodisto: Koodisto): Unit = {
    post("/koodisto-service/rest/codes", koodisto)
  }


  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi) = {
    post("/koodisto-service/rest/codeelement/" + koodistoUri, koodi)
  }

  def post[T <: AnyRef](path: String, entity: T)(implicit mf: Manifest[T]): Unit = {
    import fi.oph.tor.json.Json._
    import fi.oph.tor.json.Json4sHttp4s._
    val task: Task[Request] = Request(uri = virkalijaClient.virkailijaUriFromString(path), method = Method.POST).withBody(entity)(json4sEncoderOf[T])

    http(task) { case (status, text) if (status < 300) => logger.info("Created " + entity)
    case (status, text) => throw new scala.RuntimeException(status + ": " + text)
    }
  }

}

case class KoodistoWithLatestVersion(latestKoodistoVersio: LatestVersion)
case class LatestVersion(versio: Int)

