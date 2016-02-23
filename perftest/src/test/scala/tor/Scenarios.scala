package tor

import java.time.LocalDate
import java.time.format.DateTimeFormatter.{ofPattern => dateFormat}
import java.time.temporal.ChronoUnit._
import java.util.LinkedHashMap
import com.fasterxml.jackson.databind.ObjectMapper
import com.ning.http.client.RequestBuilder
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.Body
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.util.Random.{nextInt => randomInt}

trait TorScenario {
  val username = sys.env("TOR_USER")
  val password = sys.env("TOR_PASS")
}

object Scenarios extends UpdateOppijaScenario with FindOppijaScenario with QueryOppijatScenario with InsertOppijaScenario {
}

trait FindOppijaScenario extends TorScenario {
  private val findHttp: HttpRequestBuilder = http("find by oid").get("/api/oppija/1.2.246.562.24.00000000001").basicAuth(username, password)

  val findOppija = scenario("Find oppija").exec(findHttp)
  val prepareForFind = scenario("Prepare for find").exec(findHttp.silent)
}

trait QueryOppijatScenario extends TorScenario {
  private val queryHttp = http("query oppijat").get("/api/oppija?opiskeluoikeusPäättynytAikaisintaan=2016-01-10&opiskeluoikeusPäättynytViimeistään=2016-01-10").basicAuth(username, password)

  val queryOppijat = scenario("Query oppijat").exec(queryHttp)
  val prepareForQuery = scenario("Prepare for query").exec(queryHttp.silent)
}

trait InsertOrUpdateScenario extends TorScenario {
  def insertOrUpdate(name: String, body: Body, path: String = "/api/oppija") = http(name).put(path).body(body).asJSON.basicAuth(username, password).check(status.in(200))
}

trait UpdateOppijaScenario extends InsertOrUpdateScenario {
  private val updateHttp = insertOrUpdate("update", OppijaWithOpiskeluoikeusWithIncrementingStartdate)

  val updateOppija = scenario("Update oppija").feed(jsonFile("src/test/resources/bodies/oppija.json").circular).exec(updateHttp)
  val prepareForUpdateOppija = scenario("Prepare for update").feed(jsonFile("src/test/resources/bodies/oppija.json").circular).exec(updateHttp.silent)
}

trait InsertOppijaScenario extends InsertOrUpdateScenario {
  private val insertHttp = insertOrUpdate("insert", UusiOppijaBody)
  private val uusiOppijaJson = jsonFile("src/test/resources/bodies/uusioppija.json").circular

  val prepareForInsertOppija = scenario("Prepare for insert").feed(uusiOppijaJson).exec(insertHttp.silent)
  val insertOppija = scenario("Insert oppija").feed(uusiOppijaJson).exec(insertHttp)


  val batchInsertOppija = scenario("Batch insert oppija").feed(uusiOppijaJson).exec(insertOrUpdate("batchInsert", UusiOppijaBatchBody, "/api/oppija/batch"))
}

trait CrappyJavaTypes {
  type Map = java.util.LinkedHashMap[Any, Any]
  type Array = java.util.List[Any]
}

object OppijaWithOpiskeluoikeusWithIncrementingStartdate extends Body with CrappyJavaTypes {
  var dateCounter = LocalDate.parse("2012-09-01")

  private def nextDate = this.synchronized {
    dateCounter = dateCounter.plusDays(1)
    dateCounter.toString
  }

  override def setBody(req: RequestBuilder, session: Session) = {
    val contentMap = session.apply("content").as[Map]
    contentMap.get("opiskeluoikeudet").asInstanceOf[Array].get(0).asInstanceOf[Map].put("alkamispäivä", nextDate)
    val contentBytes = new ObjectMapper().writeValueAsBytes(contentMap)
    req.setBody(contentBytes)
  }
}

abstract class HenkilöGenerator extends Body with CrappyJavaTypes {
  val mapper: ObjectMapper = new ObjectMapper()
  def newHenkilö = {
    val hetu = Hetu.generate(LocalDate.now, LocalDate.now.minusYears(50))

    new Map {{
      put("etunimet", "tor-perf-"+hetu)
      put("kutsumanimi", "tor-perf-"+hetu)
      put("sukunimi", "tor-perf-"+hetu)
      put("hetu", hetu)
    }}
  }

  def addHenkilö(session: Session) = {
    val content = session("content").as[Map].clone.asInstanceOf[Map]
    content.put("henkilö", newHenkilö)
    content
  }
}

object UusiOppijaBody extends HenkilöGenerator {
  override def setBody(req: RequestBuilder, session: Session) = {
    val content = addHenkilö(session)

    req.setBody(new ObjectMapper().writeValueAsBytes(content))
  }
}

object UusiOppijaBatchBody extends HenkilöGenerator {
  import collection.JavaConversions._
  val batchSize = sys.env.getOrElse("BATCH_SIZE", "10").toInt
  override def setBody(req: RequestBuilder, session: Session) = {
    val content = session("content").as[Map]

    session.set("content", List())

    val oppijat = (1 to batchSize).map { num =>
      addHenkilö(session)
    }

    req.setBody(new ObjectMapper().writeValueAsBytes(seqAsJavaList(oppijat)))
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