package fi.oph.koski.valvira

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, AuditLogMessageField, KoskiOperation}
import fi.oph.scalaschema.extraction.ValidationError


class ValviraService(application: KoskiApplication) {

  val repository = new ValviraRepository(application.replicaDatabase.db)

  def getOppijaByHetu(hetu: String)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, ValviraOppija] = {
    val henkilo = application.opintopolkuHenkilöFacade.findOppijaByHetu(hetu)
    val opiskeluoikeudet = henkilo.map(_.kaikkiOidit).map(repository.opiskeluoikeudetByOppijaOids).getOrElse(Left(KoskiErrorCategory.notFound()))

    opiskeluoikeudet.flatMap { opiskeluoikeudet =>
      AuditLog.log(KoskiAuditLogMessage(KoskiOperation.OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(AuditLogMessageField.oppijaHenkiloOid -> henkilo.get.oid)))
      Right(ValviraOppija(hetu, opiskeluoikeudet))
    }
  }
}

