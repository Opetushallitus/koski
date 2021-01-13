package fi.oph.koski.valvira

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.common.log.{AuditLog, AuditLogMessage, KoskiMessageField, KoskiOperation}
import fi.oph.scalaschema.extraction.ValidationError


class ValviraService(application: KoskiApplication) {

  val repository = new ValviraRepository(application.replicaDatabase.db)

  def getOppijaByHetu(hetu: String)(implicit koskiSession: KoskiSession): Either[HttpStatus, ValviraOppija] = {
    val henkilo = application.opintopolkuHenkilöFacade.findOppijaByHetu(hetu)
    val opiskeluoikeudet = henkilo.map(_.kaikkiOidit).map(repository.opiskeluoikeudetByOppijaOids).getOrElse(Left(KoskiErrorCategory.notFound()))

    opiskeluoikeudet.flatMap { opiskeluoikeudet =>
      AuditLog.log(AuditLogMessage(KoskiOperation.OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(KoskiMessageField.oppijaHenkiloOid -> henkilo.get.oid)))
      Right(ValviraOppija(hetu, opiskeluoikeudet))
    }
  }
}

