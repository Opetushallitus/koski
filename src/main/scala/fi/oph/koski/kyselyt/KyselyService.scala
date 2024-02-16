package fi.oph.koski.kyselyt

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging

import java.net.InetAddress
import java.util.UUID

class KyselyService(application: KoskiApplication) extends Logging {
  val workerId: String = InetAddress.getLocalHost.getHostName
  logger.info(s"Query worker id: $workerId")

  private val repository = new KyselyRepository(
    db = application.masterDatabase.db,
    workerId = workerId,
    extractor = application.validatingAndResolvingExtractor,
  )


  def add(query: QueryParameters)(implicit user: KoskiSpecificSession): Either[HttpStatus, Query] =
    repository.getExisting(query).fold {
      if (query.queryAllowed(application)) {
        Right[HttpStatus, Query](repository.add(query))
      } else {
        Left(KoskiErrorCategory.unauthorized())
      }
    } (Right.apply)

  def get(id: UUID)(implicit user: KoskiSpecificSession): Either[HttpStatus, Query] =
    repository.get(id)
      .filter(_.requestedBy == user.oid)
      .toRight(KoskiErrorCategory.notFound())
}
