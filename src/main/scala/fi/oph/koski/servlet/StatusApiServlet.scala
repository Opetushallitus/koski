package fi.oph.koski.servlet

import fi.oph.koski.koskiuser.Unauthenticated

import java.io.InputStream
import java.time.ZonedDateTime
import java.util.Properties
import scala.util.Using

class StatusApiServlet extends KoskiSpecificApiServlet with NoCache with Unauthenticated {
  get("/") {
    render(Map[String, String](
      "server time" -> ZonedDateTime.now().toString,
      "gitCommitHash" -> StatusApiServlet.gitCommitHash
    ))
  }
}

object StatusApiServlet {
  private lazy val gitCommitHash = readGitCommitHash(getClass.getResourceAsStream("/buildversion.txt"))

  private[servlet] def readGitCommitHash(stream: => InputStream): String = {
    Using(stream) { input =>
      val properties = new Properties()
      properties.load(input)
      Option(properties.getProperty("vcsRevision")).map(_.trim).filter(_.nonEmpty).getOrElse("unknown")
    }.getOrElse("unknown")
  }
}
