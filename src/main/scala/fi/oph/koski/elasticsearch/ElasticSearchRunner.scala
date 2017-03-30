package fi.oph.koski.elasticsearch

import java.io.File

import fi.oph.koski.http.Http
import fi.oph.koski.log.Logging
import fi.oph.koski.util.{PortChecker, Wait}
import org.json4s._

class ElasticSearchRunner(dataDirName: String, httpPort: Int, tcpPort: Int) extends Logging {
  import sys.process._

  lazy val dataPath = new File(dataDirName).toPath

  private var serverProcess: Option[Process] = None

  // automatically checks if already running, prevents starting multiple instances
  def start = ElasticSearchRunner.synchronized {

    if (!serverProcess.isDefined && PortChecker.isFreeLocalPort(httpPort)) {
      import Http._
      import fi.oph.koski.json.Json.jsonFormats
      val url = s"http://localhost:$httpPort"
      val elasticSearchHttp = Http(url)

      def clusterHealthOk = {
        val healthResponse: JValue = Http.runTask(elasticSearchHttp.get(uri"/_cluster/health")(Http.parseJson[JValue]))
        val healthCode = (healthResponse \ "status").extract[String]
        List("green", "yellow").contains(healthCode)
      }

      logger.info(s"Starting Elasticsearch server on ports HTTP $httpPort and TCP $tcpPort")
      val cmd = s"elasticsearch -E http.port=$httpPort -E transport.tcp.port=$tcpPort -E path.conf=$dataDirName -E path.data=$dataDirName/data -E path.logs=$dataDirName/log"
      logger.info("Elasticsearch command: " + cmd)
      serverProcess = Some(cmd.run)
      PortChecker.waitUntilReservedLocalPort(httpPort)
      PortChecker.waitUntilReservedLocalPort(tcpPort)
      Wait.until(clusterHealthOk)
      sys.addShutdownHook {
        stop
      }
    }
    this
  }

  def stop() = {
    serverProcess.foreach(_.destroy())
    serverProcess = None
  }


}

private object ElasticSearchRunner