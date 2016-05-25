package tor

import java.time.LocalDate
import java.time.format.DateTimeFormatter.{ofPattern => dateFormat}
import java.time.temporal.ChronoUnit._

import com.ning.http.client.RequestBuilder
import fi.oph.koski.documentation.{AmmatillinenFullExample, ExamplesAmmatillinen}
import fi.oph.koski.json.Json
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, Oppija, UusiHenkilö}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.Body
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.util.Random.{nextInt => randomInt}

trait KoskiScenario {
  val username = sys.env("TOR_USER")
  val password = sys.env("TOR_PASS")
  val uusiOppijaFeeder = Array(Map("content" -> ExamplesAmmatillinen.uusi)).circular
  val oppijaFeeder = Array(Map("content" -> AmmatillinenFullExample.full)).circular
}

object Scenarios extends UpdateOppijaScenario with FindOppijaScenario with QueryOppijatScenario with InsertOppijaScenario {
}

trait FindOppijaScenario extends KoskiScenario {
  private val findHttp: HttpRequestBuilder = http("find by oid").get("/api/oppija/1.2.246.562.24.00000000001").basicAuth(username, password)

  val findOppija = scenario("Find oppija").exec(findHttp)
  val prepareForFind = scenario("Prepare for find").exec(findHttp.silent)
}

trait QueryOppijatScenario extends KoskiScenario {
  private val queryHttp = http("query oppijat").get("/api/oppija?opiskeluoikeusPäättynytAikaisintaan=2016-01-10&opiskeluoikeusPäättynytViimeistään=2016-01-10").basicAuth(username, password)

  val queryOppijat = scenario("Query oppijat").exec(queryHttp)
  val prepareForQuery = scenario("Prepare for query").exec(queryHttp.silent)
}

trait InsertOrUpdateScenario extends KoskiScenario {
  def insertOrUpdate(name: String, body: Body, path: String = "/api/oppija") = http(name).put(path).body(body).asJSON.basicAuth(username, password).check(status.in(200))
}

trait UpdateOppijaScenario extends InsertOrUpdateScenario {
  private val updateHttp = insertOrUpdate("update", OppijaWithOpiskeluoikeusWithIncrementingStartdate)

  val updateOppija = scenario("Update oppija").feed(oppijaFeeder).exec(updateHttp)
  val prepareForUpdateOppija = scenario("Prepare for update").feed(oppijaFeeder).exec(updateHttp.silent)
}

trait InsertOppijaScenario extends InsertOrUpdateScenario {
  private val insertHttp = insertOrUpdate("insert", UusiOppijaBody)

  val prepareForInsertOppija = scenario("Prepare for insert").feed(uusiOppijaFeeder).exec(insertHttp.silent)
  val insertOppija = scenario("Insert oppija").feed(uusiOppijaFeeder).exec(insertHttp)
  val batchInsertOppija = scenario("Batch insert oppija").feed(uusiOppijaFeeder).exec(insertOrUpdate("batchInsert", UusiOppijaBatchBody, "/api/oppija/batch"))
}

object OppijaWithOpiskeluoikeusWithIncrementingStartdate extends Body {
  var dateCounter = LocalDate.parse("2012-09-01")

  private def nextDate = this.synchronized {
    dateCounter = dateCounter.plusDays(1)
    dateCounter
  }

  override def setBody(req: RequestBuilder, session: Session) = {
    val oppija = session("content").as[Oppija]

    val opiskeluoikeudet: List[AmmatillinenOpiskeluoikeus] = oppija.opiskeluoikeudet.asInstanceOf[List[AmmatillinenOpiskeluoikeus]]
    req.setBody(Json.write(oppija.copy(opiskeluoikeudet = opiskeluoikeudet.updated(0, opiskeluoikeudet.head.copy(alkamispäivä = Some(nextDate))))).getBytes)
  }
}

abstract class HenkilöGenerator extends Body {

  def addHenkilö(session: Session) = {
    val hetu = Hetu.generate(LocalDate.now, LocalDate.now.minusYears(50))
    val nimi = "tor-perf-" + hetu
    session("content").as[Oppija].copy(henkilö = UusiHenkilö(hetu,  nimi, nimi, nimi))
  }

}

object UusiOppijaBody extends HenkilöGenerator {
  override def setBody(req: RequestBuilder, session: Session) = {
    req.setBody(Json.write(addHenkilö(session)).getBytes)
  }
}

object UusiOppijaBatchBody extends HenkilöGenerator {
  val batchSize = sys.env.getOrElse("BATCH_SIZE", "10").toInt

  override def setBody(req: RequestBuilder, session: Session) = {
    val oppijat = (1 to batchSize).map { num =>
      addHenkilö(session)
    }
    req.setBody(Json.write(oppijat).getBytes)
  }
}

object Hetu {
  val checkChars = List('0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','H','J','K','L','M','N','P','R','S','T','U','V','W','X','Y')

  def generate(bornBefore: LocalDate, bornAfter: LocalDate) = {
    val birthday = bornBefore.minusDays(randomInt(DAYS.between(bornAfter, bornBefore).toInt))
    val birthdayString = birthday.format(dateFormat("ddMMyy"))
    val separator = (birthday.getYear / 100) match {
      case 20 => 'A'
      case 19 => '-'
      case 18 => '+'
      case _  => throw new IllegalArgumentException("Unsupported birthday range")
    }
    val identifier = (900 to 999)(randomInt(100))
    val checkChar = checkChars((birthdayString + identifier).toInt % 31)

    birthdayString + separator + identifier + checkChar
  }
}