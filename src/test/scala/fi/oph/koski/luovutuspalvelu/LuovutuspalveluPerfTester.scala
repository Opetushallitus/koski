package fi.oph.koski.luovutuspalvelu

import java.sql.Timestamp
import java.time.Instant

import fi.oph.koski.integrationtest.KoskidevHttpSpecification

import scala.io.Source
import fi.oph.koski.json.JsonSerializer
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.entity.{ContentType, StringEntity}

case class TestResult(seconds: Long, amountOfReceivedOpiskeluoikeudet: Int, opiskeluoikeudenTyypit: List[String])

/**
  * Required env variables:
  *  KOSKI_USER - username that has luovutuspalvelu access
  *  KOSKI_PASS - password for username
  *  KOSKI_BASE_URL
  */
object LuovutuspalveluPerfTester extends KoskidevHttpSpecification {
  def main(args: Array[String]): Unit = {
    println("Starting test")
    val all = LuovutuspalveluApiPerfTester.doTest(List("ammatillinenkoulutus", "lukiokoulutus", "perusopetus"))

    printResult(all)
  }

  def printResult(result: TestResult): Unit = {
    println("----------------")
    println(s"Tyypit ${result.opiskeluoikeudenTyypit}")
    println(s"opiskeluoikeuksia oli vastauksissa ${result.amountOfReceivedOpiskeluoikeudet}")
    println(s"Aikaa kului ${result.seconds / 1000} s")
    println("----------------")
  }
}

protected object LuovutuspalveluApiPerfTester extends KoskidevHttpSpecification {
  val amountOfBatches = 3
  def hetuFilePath: String = ???
  lazy val client = createClient

  def doTest(opiskeluoikeusTyypit: List[String]): TestResult = {
    val requests = buildRequests(opiskeluoikeusTyypit, amountOfBatches)
    val startTime = Timestamp.from(Instant.now)
    val responses = performRequests(requests)
    val endTime = Timestamp.from(Instant.now).getTime - startTime.getTime
    val receivedOpiskeluoikeuksia = responses.map(_.opiskeluoikeudet.length).sum

    TestResult(endTime, receivedOpiskeluoikeuksia, opiskeluoikeusTyypit)
  }

  private def buildRequests(opiskeluoikeusTyypit: List[String], amountOfBatches: Int): List[HttpPost] = {
    Source.fromFile(hetuFilePath).getLines.toList.grouped(1000).toList.takeRight(amountOfBatches)
      .map(hetut => BulkHetuRequestV1(1, hetut, opiskeluoikeusTyypit))
      .map(request => {
        println(request.hetut.size)
        val post = new HttpPost(baseUrl ++ "/api/luovutuspalvelu/hetut")
        val (cookie, user) = authHeaders(defaultUser).toList.head
        post.setHeader(cookie, user)
        post.setEntity(new StringEntity(JsonSerializer.writeWithRoot(request), ContentType.APPLICATION_JSON))
        post
      })
  }

  private def performRequests(requests: List[HttpPost]): List[LuovutuspalveluResponseV1] = {
    requests.flatMap(request => {
      val response = client.execute(request)
      val parsedResponse = parseResponse(response)
      val code = response.getStatusLine.getStatusCode
      println(code)
      response.close()
      if (code == 200) {
        JsonSerializer.parse[List[LuovutuspalveluResponseV1]](parsedResponse)
      } else {
        Nil
      }
    })
  }

  private def parseResponse(response: CloseableHttpResponse): String = {
    Source.fromInputStream(response.getEntity.getContent).mkString
  }
}

