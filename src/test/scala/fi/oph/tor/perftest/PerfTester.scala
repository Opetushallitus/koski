package fi.oph.tor.perftest

import fi.oph.tor.db.{Futures, GlobalExecutionContext}
import fi.vm.sade.utils.Timer
import scala.collection.immutable.IndexedSeq
import scala.concurrent.Future
import scala.concurrent.duration._

object PerfTester extends App with TestApp with GlobalExecutionContext with Futures {
  val blockSize = 100
  val blockCount = 1
  Timer.timed("Haettu " + (blockSize * blockCount) + " suoritusta") {

    val futures: IndexedSeq[Future[Int]] = for (i <- 0 to blockCount-1) yield {
      fetch(i * blockSize, blockSize).map(_ => blockSize)
    }

    println(await(Future.reduce(futures)((a, b) =>
      a + b
    ), 5 minutes))

  }

  System.exit(0)

  def fetch(start: Int, count: Int) = {
    Future(
      for (i <- 0 to count-1) {
        fetchSuoritukset(oid(start + i))
        //fetchSuoritukset("1.2.246.562.24." + "%016d".formatLocal(java.util.Locale.US, 0 + i))
      }
    )
  }

  def oid(index: Int) = {
    "1.2.246.562.24.0000000000" + (index + 1)
    //PersonOids.personOids(index)
  }

  def fetchSuoritukset(oppijaId: String): Unit = {
    get("api/oppija/" + oppijaId, headers = (authHeaders + ("Content-type" -> "application/json"))) {
      if(response.status != 200) {
        println(response.body)
      }
    }
  }
}