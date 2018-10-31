package fi.oph.koski.luovutuspalvelu

import java.sql.Timestamp
import java.time.Instant

import fi.oph.koski.integrationtest.KoskidevHttpSpecification

import scala.io.Source
import fi.oph.koski.json.JsonSerializer
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.entity.{ContentType, StringEntity}

/**
  * Required env variables:
  *  HETU_FILE_PATH - file containing list of hetus
  *  KOSKI_USER - username that has luovutuspalvelu access
  *  KOSKI_PASS - password for username
  */
object LuovutuspalveluPerfTester extends KoskidevHttpSpecification {

  lazy val client = createClient

  def main(args: Array[String]): Unit = {
    doTest
  }

  def doTest: Unit = {
    val requests = buildRequests

    val startTime = Timestamp.from(Instant.now)

    val responses = makeRequests(requests)

    val endTime = Timestamp.from(Instant.now)

    responses.foreach(response => println(s"got ${response.length} opiskeluoikeutta"))
    println(s"Sended ${requests.length} requests")
    println(s"Time was: ${(endTime.getTime - startTime.getTime) / 1000} s")
  }

  def buildRequests: List[HttpPost] = {
    val hetuFilePath = requiredEnv("HETU_FILE_PATH")
    val batchSize = sys.env.getOrElse("HETU_BATCH_SIZE", "1000").toInt
    val opiskeluoikeusTyypit = List("perusopetus", "ammatillinenkoulutus", "lukiokoulutus")

    Source.fromFile(hetuFilePath).getLines.toList.grouped(1000).toList.take(2)
      .map(hetut => BulkHetuRequestV1(1, hetut, opiskeluoikeusTyypit))
      .map(request => {
        val post = new HttpPost(baseUrl ++ "/api/luovutuspalvelu/hetut")
        val (cookie, user) = authHeaders(defaultUser).toList.head
        post.setHeader(cookie, user)
        post.setEntity(new StringEntity(JsonSerializer.writeWithRoot(request), ContentType.APPLICATION_JSON))
        post
      })
  }

  def makeRequests(requests: List[HttpPost]): List[List[HetuResponseV1]] = {
    requests.map(request => {
      val response = client.execute(request)
      val parsedResponse = parseResponse(response)
      println(response.getStatusLine.getStatusCode)
      response.close()
      JsonSerializer.parse[List[HetuResponseV1]](parsedResponse)
    })
  }

  def parseResponse(response: CloseableHttpResponse): String = {
    Source.fromInputStream(response.getEntity.getContent).mkString
  }
}

