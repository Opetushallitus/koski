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
  *  HETU_FILE_PATH - file containing list of hetus
  *  KOSKI_USER - username that has luovutuspalvelu access
  *  KOSKI_PASS - password for username
  */
object LuovutuspalveluPerfTester extends KoskidevHttpSpecification {

  def main(args: Array[String]): Unit = {
    println("Starting test")
    val all = LuovutuspalveluApiPerfTester.doTest(List("ammatillinenkoulutus", "lukiokoulutus", "perusopetus"), 3)
    val onlyOne = LuovutuspalveluApiPerfTester.doTest(List("perusopetus"), 3)

    printResult(all)
    printResult(onlyOne)
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

  lazy val client = createClient

  def doTest(opiskeluoikeusTyypit: List[String], amountOfBatches: Int): TestResult = {
    val requests = buildRequests(opiskeluoikeusTyypit, amountOfBatches)
    val startTime = Timestamp.from(Instant.now)
    val responses = makeRequests(requests)
    val endTime = Timestamp.from(Instant.now).getTime - startTime.getTime
    val receivedOpiskeluoikeuksia = responses.map(_.map(_.opiskeluoikeudet.length)).flatten.reduce(_ + _)

    TestResult(endTime, receivedOpiskeluoikeuksia, opiskeluoikeusTyypit)
  }

  private def buildRequests(opiskeluoikeusTyypit: List[String], amountOfBatches: Int): List[HttpPost] = {
    val hetuFilePath = requiredEnv("HETU_FILE_PATH")
    val batchSize = sys.env.getOrElse("HETU_BATCH_SIZE", "500").toInt

    Source.fromFile(hetuFilePath).getLines.toList.grouped(1000).toList.takeRight(amountOfBatches)
      .map(hetut => BulkHetuRequestV1(1, hetut, opiskeluoikeusTyypit))
      .map(request => {
        val post = new HttpPost(baseUrl ++ "/api/luovutuspalvelu/hetut")
        val (cookie, user) = authHeaders(defaultUser).toList.head
        post.setHeader(cookie, user)
        post.setEntity(new StringEntity(JsonSerializer.writeWithRoot(request), ContentType.APPLICATION_JSON))
        post
      })
  }

  private def makeRequests(requests: List[HttpPost]): List[List[LuovutuspalveluResponseV1]] = {
    requests.map(request => {
      val response = client.execute(request)
      val parsedResponse = parseResponse(response)
      println(response.getStatusLine.getStatusCode)
      response.close()
      JsonSerializer.parse[List[LuovutuspalveluResponseV1]](parsedResponse)
    })
  }

  private def parseResponse(response: CloseableHttpResponse): String = {
    Source.fromInputStream(response.getEntity.getContent).mkString
  }
}

