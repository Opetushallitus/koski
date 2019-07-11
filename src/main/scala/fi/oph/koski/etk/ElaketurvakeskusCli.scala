package fi.oph.koski.etk

import java.sql.Timestamp
import java.time.Instant

import fi.oph.koski.json.JsonSerializer.{writeWithRoot => asJsonString}

object ElaketurvakeskusCli {

  var output = print _

  def main(args: Array[String]): Unit = {
    val parsedArgs = ElaketurvakeskusCliArgsParser.parse(args)
    val client = ElaketurvakeskusCliClient(parsedArgs)

    val response = parsedArgs.flatMap {
      case (CsvFile, filepath) => ElaketurvakeskusCsvParser.parse(filepath)
      case (ApiCall, parameters) => client.etkTutkintotiedot(parameters)
      case _ => None
    }.map(ElaketurvakeskusTutkintotiedotFormatter.format)
      .reduce[EtkResponse](concatResponses)

    client.makeAuditLogsForOids(response.tutkinnot.flatMap(_.viite.flatMap(_.oppijaOid)))
    printEtkResponse(response)
  }

  private def concatResponses(a: EtkResponse, b: EtkResponse) = {
    if (a.vuosi != b.vuosi) {
      throw new Error(s"Vuosien ${a.vuosi} ja ${b.vuosi} tutkintotietoja yritettiin yhdistää")
    }

    EtkResponse(
      vuosi = a.vuosi,
      aikaleima = Timestamp.from(Instant.now),
      tutkintojenLkm = a.tutkintojenLkm + b.tutkintojenLkm,
      tutkinnot = a.tutkinnot ::: b.tutkinnot
    )
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
}
