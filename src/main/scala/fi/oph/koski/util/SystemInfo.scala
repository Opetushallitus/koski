package fi.oph.koski.util

import fi.oph.koski.log.{Logging}
import sys.process._

object SystemInfo extends Logging {

  def logInfo: Unit = {
    try {
      val commands = Seq(
        "ps -Ao pid,pcpu,rss,cmd --sort=rss".#|("tail -10"),
        "ps -Ao pid,pcpu,rss,cmd --sort=pcpu".#|("tail -10"),
        "scripts/filehogs.sh".cat
      )
      for (cmd <- commands) {
        val result = cmd.!!
        logger.info(s"$cmd output: $result")
      }
    } catch {
      case e: Throwable =>
        logger.error(e)(s"Error running external command: ${e.getMessage}")
    }
  }
}
