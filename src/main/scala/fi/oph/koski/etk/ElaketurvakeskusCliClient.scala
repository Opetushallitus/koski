package fi.oph.koski.etk

import java.time.LocalDate

import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.log.{KoskiOperation, LocalAuditLogRequest}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf

case class ElaketurvakeskusCliClient(private val http: Http) {

  def etkTutkintotiedot(parameters: String): Option[EtkResponse] = {
    val Array(endpoint, alkuStr, loppuStr) = parameters.split(":")
    val alku = LocalDate.parse(alkuStr)
    val loppu = LocalDate.parse(loppuStr)
    val request = EtkTutkintotietoRequest(alku, loppu, alku.getYear)

    runTask(http.post(uri"/elaketurvakeskus/$endpoint", request)(json4sEncoderOf[EtkTutkintotietoRequest])(Http.parseJsonOptional[EtkResponse]))
  }

  def makeAuditLogsForOids(oids: Seq[String]): Unit = {
    val request = LocalAuditLogRequest(oids, KoskiOperation.OPISKELUOIKEUS_HAKU.toString)
    runTask(http.post(uri"/auditlog", request)(json4sEncoderOf[LocalAuditLogRequest])(expectSuccess))
  }
}

object ElaketurvakeskusCliClient {

  private val koskiDefaultPort = "8080"
  private val localhost = "127.0.0.1"

  def apply(args: Map[Argument, String]): ElaketurvakeskusCliClient = {
    val (username, password, port) = parseArgs(args)
    new ElaketurvakeskusCliClient(createClient(username, password, port))
  }

  private def parseArgs(args: Map[Argument, String]) = {
    val Array(username, password) = args.get(CliUser).map(_.split(":")).getOrElse(throw new Error("määritä -user tunnus:salasana"))
    val port = args.getOrElse(KoskiPort, koskiDefaultPort)
    (username, password, port)
  }

  private def createClient(username: String, password: String, port: String) = {
    val config = ServiceConfig(s"http://$localhost:$port/koski/api", username, password)
    VirkailijaHttpClient(config, localhost, useCas = false)
  }
}
