package fi.oph.koski.servlet

import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.util.BuildVersion

import java.io.InputStream
import java.time.ZonedDateTime
import scala.util.Try

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

  private[servlet] def readGitCommitHash(stream: => InputStream): String =
    Try(BuildVersion.read(stream).flatMap(_.vcsRevision).map(_.trim).filter(_.nonEmpty))
      .toOption.flatten.getOrElse("unknown")
}
