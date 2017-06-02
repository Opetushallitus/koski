package fi.oph.koski.elasticsearch

import com.typesafe.config.Config
import fi.oph.koski.http.Http
import fi.oph.koski.log.Logging
import org.http4s.EntityEncoder
import Http._

/**
  * Created by jpaanane on 02/06/2017.
  */
case class ElasticSearch(config: Config) extends Logging {
  private val host = config.getString("elasticsearch.host")
  private val port = config.getInt("elasticsearch.port")
  private val url = s"http://$host:$port"

  val http = Http(url)

  val init_ = synchronized {
    if (host == "localhost") {
      new ElasticSearchRunner("./elasticsearch", port, port + 100).start
    }
    logger.info(s"Using elasticsearch at $host:$port")
  }

  def refreshIndex =
    Http.runTask(http.post(uri"/koski/_refresh", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))

}
