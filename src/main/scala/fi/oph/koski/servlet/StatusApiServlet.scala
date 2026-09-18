package fi.oph.koski.servlet

import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.util.BuildVersion

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
  private lazy val gitCommitHash = gitCommitHashFromMetadata(BuildVersion.read())

  private[servlet] def gitCommitHashFromMetadata(metadata: => Option[BuildVersion]): String =
    Try(metadata.flatMap(_.vcsRevision).map(_.trim).filter(_.nonEmpty))
      .toOption.flatten.getOrElse("unknown")
}
