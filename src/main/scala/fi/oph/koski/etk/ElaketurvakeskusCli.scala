package fi.oph.koski.etk

import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate.{parse => date}

import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer.{writeWithRoot => asJsonString}

object ElaketurvakeskusCli {

  val localhost =  "127.0.0.1"
  val koskiDefaultPort = "8080"

  var output = print _

  def main(args: Array[String]): Unit = {
    val parsedArgs = parseArgs(args)
    val response = toEtkResponses(parsedArgs).reduce[EtkResponse](mergeResponses)
    val formatted = response.copy(tutkinnot = formatTutkinnonTasot(response.tutkinnot))
    printEtkResponse(formatted)
  }

  private def parseArgs(arguments: Array[String]) = {
    def parse(result: Map[Argument, String], args: List[String]): Map[Argument, String] = args match {
      case Nil => result
      case "-csv" :: string :: tail => parse(result + (CsvFile -> string), tail)
      case "-api" :: string :: tail => parse(result + (ApiCall -> string), tail)
      case "-user" :: string :: tail => parse(result + (CliUser -> string), tail)
      case "-port" :: string :: tail => parse(result + (KoskiPort -> string), tail)
      case unknown@_ => throw new Error(s"Unknown argument $unknown")
    }

    parse(Map(), arguments.toList)
  }

  private def printEtkResponse(response: EtkResponse): Unit = {
    val result =
      s"""|{
          | "vuosi": ${response.vuosi},
          | "tutkintojenLkm": ${response.tutkintojenLkm},
          | "aikaleima": "${response.aikaleima}",
          | "tutkinnot": [
          |${makeTutkinnotJson(response.tutkinnot)}
          | ]
          |}""".stripMargin

    output(result)
  }

  private def makeTutkinnotJson(tutkinnot: List[EtkTutkintotieto]): String = {
    val tutkintoStrings = tutkinnot.map(asJsonString(_))
    val sb = new StringBuilder

    sb.append(s"\t\t${tutkintoStrings.head}")
    tutkintoStrings.tail.foreach(str => sb.append(s",\n\t\t${str}"))
    sb.toString
  }

  private def mergeResponses(a: EtkResponse, b: EtkResponse) = {
    if (a.vuosi != b.vuosi) { throw new Exception(s"Vuosien ${a.vuosi} ja ${b.vuosi} tutkintotietoja yritettiin yhdistää") }

    EtkResponse(
      vuosi = a.vuosi,
      aikaleima = Timestamp.from(Instant.now),
      tutkintojenLkm = a.tutkintojenLkm + b.tutkintojenLkm,
      tutkinnot = a.tutkinnot ::: b.tutkinnot
    )
  }

  private def toEtkResponses(arguments: Map[Argument, String]) = {
    arguments.flatMap { case (argument, parameter) => argument match {
      case CsvFile => readEtkResponseFromCsvFile(parameter)
      case ApiCall => fetchEtkResponseFromApi(parameter, arguments.get(CliUser), arguments.get(KoskiPort))
      case _ => None
    }}
  }

  private def readEtkResponseFromCsvFile(filepath: String): Option[EtkResponse] = {
    ElaketurvakeskusCsvParser.parse(filepath)
  }

  private def fetchEtkResponseFromApi(apiParameters: String, userAndPassword: Option[String], port: Option[String]): Option[EtkResponse] = {
    val (endpoint, alku, loppu) = parseApiParameters(apiParameters)
    val http = createClient(endpoint, userAndPassword, port)
    val request = EtkTutkintotietoRequest(alku, loppu, vuosi = alku.getYear)

    runTask(http.post(uri"/$endpoint", request)(json4sEncoderOf[EtkTutkintotietoRequest])(Http.parseJsonOptional[EtkResponse]))
  }

  private def parseApiParameters(parameters: String) = {
    val Array(endpoint, alkuStr, loppuStr) = parameters.split(":")
    (endpoint, date(alkuStr), date(loppuStr))
  }

  private def createClient(endpoint: String, userAndPassword: Option[String], port: Option[String]) = {
    val Array(username, password) = userAndPassword.map(_.split(":")).getOrElse(throw new Exception("määritä -user tunnus:salasana voidaksesi tehdä api kutsun"))
    val koskiPort = port.getOrElse(koskiDefaultPort)
    val config = ServiceConfig(s"http://$localhost:$koskiPort/koski/api/elaketurvakeskus", username, password)

    VirkailijaHttpClient(config, localhost, useCas = false)
  }

  private def formatTutkinnonTasot(tutkintotiedot: List[EtkTutkintotieto]) = {
    tutkintotiedot.map(tutkintotieto =>
      tutkintotieto.copy(tutkinto = formatTutkinnonTaso(tutkintotieto.tutkinto)
      )
    )
  }

  private def formatTutkinnonTaso(tutkinto: EtkTutkinto) = {
    tutkinto.copy(tutkinnonTaso = format(tutkinto.tutkinnonTaso))
  }

  private def format(taso: Option[String]): Option[String] = taso match {
    case Some(t) => format(t)
    case _ => None
  }

  private def format(taso: String) = taso match {
    case "ammatillinenkoulutus" => Some("ammatillinenperustutkinto")
    case "1" => Some("ammattikorkeakoulututkinto")
    case "2" => Some("alempikorkeakoulututkinto")
    case "3" => Some("ylempiammattikorkeakoulututkinto")
    case "4" => Some("ylempikorkeakoulututkinto")
    case "" => None
    case _ => throw new Exception(s"tutkintotason koodia vastaavaa koodia ei löytynyt. Koodi:${taso}. Rivi: ${1}")
  }
}

sealed abstract trait Argument

case object CsvFile extends Argument
case object ApiCall extends Argument
case object CliUser extends Argument
case object KoskiPort extends Argument
