package fi.oph.koski.servlet

import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.util.BuildVersion

import java.time.ZonedDateTime
import scala.util.Try

class StatusApiServlet extends KoskiSpecificApiServlet with NoCache with Unauthenticated {
  get("/") {
    render(Map[String, String](
      "serverTime" -> ZonedDateTime.now().toString,
      "commitHash" -> StatusApiServlet.commitHash
    ))
  }
}

object StatusApiServlet {
  private lazy val commitHash = commitHashFromBuildVersion(BuildVersion.read())

  private[servlet] def commitHashFromBuildVersion(buildVersion: => Option[BuildVersion]): String =
    Try(buildVersion.flatMap(_.vcsRevision).map(_.trim).filter(_.nonEmpty))
      .toOption.flatten.getOrElse("unknown")
}
