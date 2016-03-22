package fi.oph.tor.perftest

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import fi.oph.tor.http.Http
import fi.oph.tor.http.Http.runTask
import fi.oph.tor.json.Json
import fi.oph.tor.log.Logging
import fi.oph.tor.perftest.PerfTestData.opiskeluoikeudet
import fi.oph.tor.schema.{Henkilö, OpiskeluOikeus, TorOppija}

import scala.collection.mutable
import scala.util.Random.{nextInt => randomInt}

object FixtureCreator extends App with TestApp with Logging {
  // echo "[" > dumpi.txt; grep INSERT opiskeluoikeus.pg | sed -E "s/INSERT INTO opiskeluoikeus \(id, data, oppija_oid\) VALUES \([0-9]+, '//g" | rev | cut -c 34- | rev | sed 's/$/,/' | sed -E '$s/,$//' >> dumpi.txt; echo "]" >> dumpi.txt

  var hetut = new mutable.Stack[String]
  val pool: ExecutorService = Executors.newFixedThreadPool(10)
  val amount = 1000000

  val t0 = System.currentTimeMillis()

  1 to amount foreach { x =>
    val oikeus: OpiskeluOikeus = opiskeluoikeudet(x % opiskeluoikeudet.length)
    val nimi = "Tor-Perf-" + x
    val oppija: TorOppija = TorOppija(Henkilö(nextHetu, nimi, nimi, nimi), List(oikeus))
    val body = Json.write(oppija).getBytes("utf-8")


    pool.execute(new Runnable {
      override def run(): Unit = {
        put("api/oppija", body = body, headers = (authHeaders + ("Content-type" -> "application/json"))) {
          if(response.status != 200) {
            logger.info(oikeus.id + " failed")
            logger.info(response.body)
          }
          logger.info(nimi + " " + response.status)
        }
      }
    })
  }

  pool.shutdown()
  pool.awaitTermination(48, TimeUnit.HOURS)
  private val elapsed: Long = System.currentTimeMillis() - t0
  logger.info("Created " + amount + " opinto-oikeus in " + elapsed + "ms")
  logger.info("Ops/sec " + (amount * 1000 / elapsed))


  def nextHetu = {
    hetut.synchronized {
      if(hetut.isEmpty) {
        hetut.pushAll(runTask(Http("http://www.telepartikkeli.net/tunnusgeneraattori/api")("/generoi/hetu/1000")(Http.parseJson[List[String]])))
      }
      hetut.pop()
    }
  }

}
