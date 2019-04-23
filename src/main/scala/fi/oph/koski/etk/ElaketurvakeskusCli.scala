package fi.oph.koski.etk


import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate}

import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer.{writeWithRoot => asJsonString}

import scala.annotation.tailrec
import scala.io.Source


object ElaketurvakeskusCli {
  var output = print _

  def main(args: Array[String]): Unit = {
    val etkResponse = argsToTasks(args)
      .flatMap(_.toEtkResponse)
      .reduce[EtkResponse](mergeResponses)

    printEtkResponse(etkResponse)
  }

  private def printEtkResponse(response: EtkResponse): Unit = {
    val result =
      s"""|{
          | "vuosi": ${response.vuosi},
          | "tutkintojenLkm": ${response.tutkintojenLkm},
          | "aikaleima": "${response.aikaleima}",
          | "tutkinnot": [
          |${makeTutkinnotString(response.tutkinnot)}
          | ]
          |}""".stripMargin

    output(result)
  }

  private def makeTutkinnotString(tutkinnot: List[EtkTutkintotieto]): String = {
    @tailrec
    def rec(tutkinnot: List[EtkTutkintotieto], accumulator: String = ""): String = {
      tutkinnot match {
        case Nil => accumulator
        case t :: Nil => accumulator + s"\t\t${asJsonString(t)}"
        case t :: ts => rec(ts, accumulator + s"\t\t${asJsonString(t)},\n")
      }
    }
    rec(tutkinnot)
  }

  private def mergeResponses(res1: EtkResponse, res2: EtkResponse) = {
    if (res1.vuosi != res2.vuosi) {
      throw new Exception(s"Vuosien ${res1.vuosi} ja ${res2.vuosi} tutkintotietoja yritettiin yhdistää")
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
      case "-port" => KoskiPort(arg)
      case _ => throw new Exception(s"Unkown command ${cmd}")
    }
    }.toList

    appendAuthToRequests(parsedArgs).filter(_.isInstanceOf[Task]).map(_.asInstanceOf[Task])
  }

  private def appendAuthToRequests(args: List[Args]) = {
    args.map {
      case r: RaportointikantaRequest => RaportointikantaRequest(r.endpoint, r.alku, r.loppu, findAuthentication(args), findPortOrDefault(args))
      case a: Args => a
    }
  }

  private def findAuthentication(tasks: List[Args]): Authentication= {
    tasks.find(_.isInstanceOf[Authentication]) match {
      case Some(s) => s.asInstanceOf[Authentication]
      case _ => throw new Exception("määritä -user tunnus:salasana")
    }
  }

  private def findPortOrDefault(tasks: List[Args]): KoskiPort = {
    tasks.find(_.isInstanceOf[KoskiPort]) match {
      case Some(s) => s.asInstanceOf[KoskiPort]
      case None => KoskiPort("8080")
    }
  }
}

private object Csv {

  def parse(filepath: String): Option[EtkResponse] = {
    val csv = Source.fromFile(filepath).getLines().toList
    val headLine = csv.head.split(";")
    val fieldMap = headLine.zipWithIndex.toMap

    val vuosi = csv.drop(1).head.split(";")(0).toInt
    val tutkintotiedot = csv.drop(1).map(toEtkTutkintotieto(_, fieldMap, headLine.size))

    Some(EtkResponse(
      vuosi = vuosi,
      tutkintojenLkm = tutkintotiedot.size,
      tutkinnot = tutkintotiedot,
      aikaleima = Timestamp.from(Instant.now)
    ))
  }

  private def toEtkTutkintotieto(row: String, fieldMap: Map[String, Int], expectedFieldCount: Int) = {
    val fields = row.split(";")

    if (fields.size != expectedFieldCount) {
      throw new Exception(s"Riviltä puuttuu kenttiä: ${row}")
    }

    def get(field: String): String = fields(fieldMap.get(field).getOrElse(throw new Exception(s"csv tiedostosta puuttuu kenttä ${field}")))

    EtkTutkintotieto(
      henkilö = EtkHenkilö(
        hetu = Some(get("hetu")),
        syntymäaika = Some(LocalDate.parse(get("syntymaaika"))),
        sukunimi = get("sukunimi"),
        etunimet = get("etunimet")
      ),
      tutkinto = EtkTutkinto(
        tutkinnonTaso = Format.tutkintotaso(get("tutkinnon_taso")),
        alkamispäivä = LocalDate.parse(get("OpiskeluoikeudenAlkamispaivamaara")),
        päättymispäivä = Some(LocalDate.parse(get("suorituspaivamaara")))
      ),
      viite = None
    )
  }
}

private trait Args

private trait Task {
  def toEtkResponse(): Option[EtkResponse]
}

private case class VirtaCsv(filepath: String) extends Args with Task {
  override def toEtkResponse(): Option[EtkResponse] = {
    Csv.parse(filepath)
  }
}

private case class KoskiPort(str: String) extends Args

private case class Authentication(username: String, password: String) extends Args

private object Authentication {
  def apply(str: String): Authentication = {
    val Array(username, password) = str.split(":")
    Authentication(username, password)
  }
}

private case class RaportointikantaRequest(endpoint: String, alku: LocalDate, loppu: LocalDate, auth: Authentication = Authentication("",""), koskiport: KoskiPort = KoskiPort("8080")) extends Args with Task {
  override def toEtkResponse(): Option[EtkResponse] = {
    endpoint match  {
      case "ammatillisetperustutkinnot" => RaportointikantaClient(auth.username, auth.password, koskiport.str).ammatillisetperustutkinnot(alku, loppu)
      case _ => throw new Exception("API endpointtia ei ole määritelty")
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
      .map(response => {
        EtkResponse(
          vuosi = response.vuosi,
          aikaleima = response.aikaleima,
          tutkintojenLkm = response.tutkintojenLkm,
          tutkinnot = response.tutkinnot.map(Format.tutkintotieto(_))
        )
      })
  }
}

private object RaportointikantaClient {
  def apply(username: String, password: String, koskiPort: String): RaportointikantaClient = {
    val config = ServiceConfig(s"http://127.0.0.1:${koskiPort}/koski/api", username, password)
    RaportointikantaClient(VirkailijaHttpClient(config, "127.0.0.1", useCas = false))
  }
}

private object Format {
  def tutkintotaso(str: String): String = str match {
    case "ammatillinenkoulutus" => "ammatillinenperuskoulutus"
    case "1" => "ammattikorkeakoulutututkinto"
    case "2" => "alempikorkeakoulututkinto"
    case "3" => "ylempiammattikorkeakoulututkinto"
    case "4" => "ylempikorkeakoulututkinto"
  }

  def tutkintotieto(tt: EtkTutkintotieto) = {
    EtkTutkintotieto(
      henkilö = tt.henkilö,
      tutkinto = EtkTutkinto(
        tutkinnonTaso = Format.tutkintotaso(tt.tutkinto.tutkinnonTaso),
        alkamispäivä = tt.tutkinto.alkamispäivä,
        päättymispäivä = tt.tutkinto.päättymispäivä
      ),
      viite = tt.viite
    )
  }
}
