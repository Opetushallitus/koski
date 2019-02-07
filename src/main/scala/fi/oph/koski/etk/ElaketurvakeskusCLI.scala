package fi.oph.koski.etk


import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate}

import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.{JsonFiles}

import scala.io.Source


object ElaketurvakeskusCLI {

  def main(args: Array[String]): Unit = {
    val parsedArgs = argsToTasks(args)

    val tasks = parsedArgs.filter(_.isInstanceOf[Task]).map(_.asInstanceOf[Task])

    val fileName = parsedArgs.find(_.isInstanceOf[OutputFile]).getOrElse(throw new Exception("Maarita haluttu tiedoston nimi -output")).asInstanceOf[OutputFile]

    val tutkintotiedot = tasks.flatMap(_.doIt).reduce[EtkResponse](mergeResponses)

    JsonFiles.writeFile[EtkResponse](fileName.filename, tutkintotiedot)
    println(s"Haettiin yhteensa ${tutkintotiedot.tutkintojenLkm} tutkintoa")
  }

  private def mergeResponses(res1: EtkResponse, res2: EtkResponse) = {
    if (res1.vuosi != res2.vuosi) {
      throw new Exception(s"Vuosien ${res1.vuosi} ja ${res2.vuosi} tutkintojatietoja yritettiin yhdistaa")
    }

    EtkResponse(
      vuosi = res1.vuosi,
      aikaleima = Timestamp.from(Instant.now),
      tutkintojenLkm = res1.tutkinnot.size + res2.tutkinnot.size,
      tutkinnot = res1.tutkinnot ::: res2.tutkinnot
    )
  }

  private def argsToTasks(args: Array[String]) = {
    val parsedArgs = args.toList.sliding(2, 2).map { case List(cmd, arg) => cmd match {
      case "-csv" => VirtaCsv(arg)
      case "-user" => Authentication(arg)
      case "-api" => RaportointikantaRequest(arg)
      case "-output" => OutputFile(arg)
      case _ => throw new Exception(s"Unkown command ${cmd}")
    }
    }.toList

    appendAuthToRequests(parsedArgs)
  }

  private def appendAuthToRequests(args: List[Args]) = {
    args.map { arg =>
      arg match {
        case r: RaportointikantaRequest => RaportointikantaRequest(r.endpoint, r.alku, r.loppu, findAuthentication(args))
        case _ => arg
      }
    }
  }

  private def findAuthentication(tasks: List[Args]) = {
    val auth = tasks.find(_.isInstanceOf[Authentication])
    if (auth.isDefined) {
      auth.get.asInstanceOf[Authentication]
    } else {
      throw new Exception("kayttaja:salasana paria ei loytynyt. -user tunnus:salanasa")
    }
  }
}

private object Csv {

  def parse(filepath: String): Option[EtkResponse] = {
    val csv = Source.fromFile(filepath).getLines().toList
    val headLine = csv.head.split(";")
    val fieldMap = headLine.zipWithIndex.toMap

    val vuosi = csv.drop(1).head.split(";")(0).toInt
    val tutkintotiedot = csv.drop(1).map(toEtkTutkintotieto(_, fieldMap))

    Some(EtkResponse(
      vuosi = vuosi,
      tutkintojenLkm = tutkintotiedot.size,
      tutkinnot = tutkintotiedot,
      aikaleima = Timestamp.from(Instant.now)
    ))
  }

  private def toEtkTutkintotieto(row: String, fieldMap: Map[String, Int]) = {
    val fields = row.split(";")

    def get(field: String): String = fields(fieldMap.get(field).get)

    EtkTutkintotieto(
      henkilö = EtkHenkilö(
        hetu = Some(get("hetu")),
        syntymäaika = Format.date(get("syntymaaika")),
        sukunimi = get("sukunimi"),
        etunimet = get("etunimet")
      ),
      tutkinto = EtkTutkinto(
        tutkinnonTaso = Format.tutkintotaso(get("tutkinnon_taso")),
        alkamispäivä = Format.date(get("OpiskeluoikeudenAlkamispaivamaara")),
        päättymispäivä = Format.dateOption(get("suorituspaivamaara"))
      ),
      viite = None
    )
  }
}

private trait Args

private trait Task {
  def doIt(): Option[EtkResponse]
}

private case class VirtaCsv(filepath: String) extends Args with Task {
  override def doIt(): Option[EtkResponse] = {
    Csv.parse(filepath)
  }
}

private case class OutputFile(filename: String) extends Args

private case class Authentication(username: String, password: String) extends Args

private object Authentication {
  def apply(str: String): Authentication = {
    val Array(username, password) = str.split(":")
    Authentication(username, password)
  }
}

private case class RaportointikantaRequest(endpoint: String, alku: LocalDate, loppu: LocalDate, auth: Authentication = Authentication("","")) extends Args with Task {
  override def doIt(): Option[EtkResponse] = {
    endpoint match  {
      case "ammatillisetperustutkinnot" => RaportointikantaClient(auth.username, auth.password).ammatillisetperustutkinnot(alku, loppu)
      case _ => throw new Exception("API endpointtia ei ole maaritelty")
    }
  }
}

private object RaportointikantaRequest {
  def apply(str: String): RaportointikantaRequest = {
    val Array(endpoint, alku, loppu) = str.split(":")
    val Array(alkuDate, loppuDate) = Array(alku, loppu).map(Date.valueOf(_).toLocalDate)
    RaportointikantaRequest(endpoint, alkuDate, loppuDate)
  }
}

private case class RaportointikantaClient(http: Http) {
  def ammatillisetperustutkinnot(alku: LocalDate, loppu: LocalDate) = {
    val request = EtkTutkintotietoRequest(alku, loppu, alku.getYear)
    runTask(http.post(uri"/elaketurvakeskus/ammatillisetperustutkinnot", request)(json4sEncoderOf[EtkTutkintotietoRequest])(Http.parseJsonOptional[EtkResponse]))
  }
}

private object RaportointikantaClient {
  def apply(username: String, password: String): RaportointikantaClient = {
    val config = ServiceConfig("http://127.0.0.1:8080/koski/api", username, password)
    RaportointikantaClient(VirkailijaHttpClient(config, "127.0.0.1", useCas = false))
  }
}

private object Format {
  def date(str: String): LocalDate = Date.valueOf(str).toLocalDate

  def dateOption(str: String): Option[LocalDate] = Some(date(str))

  def tutkintotaso(str: String): String = str match {
    case "ammatillinenkoulutus" => "ammatillinenperuskoulutus"
    case "1" => "ammattikorkeakoulutututkinto"
    case "2" => "alempikorkeakoulututkinto"
    case "3" => "ylempiammattikorkeakoulututkinto"
    case "4" => "ylempikorkeakoulututkinto"
  }
}
