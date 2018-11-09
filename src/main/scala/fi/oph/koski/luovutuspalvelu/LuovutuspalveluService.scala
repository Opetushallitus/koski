package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.{AuditLog, AuditLogMessage, KoskiMessageField, KoskiOperation}
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryContext, OpiskeluoikeusQueryFilter}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.{OneOfOpiskeluoikeudenTyypit, OppijaOidHaku}
import fi.oph.koski.schema.{Henkilö, Koodistokoodiviite, OpiskeluoikeudenTyyppi, TäydellisetHenkilötiedot}
import org.json4s.JValue
import rx.lang.scala.Observable

class LuovutuspalveluService(application: KoskiApplication) {
  def findOppijaByHetu(req: HetuRequestV1)(implicit koskiSession: KoskiSession): Either[HttpStatus, HetuResponseV1] = {
    val (useVirta, useYtr) = resolveVirtaAndYtrUsage(req)
    for {
      oppijaWithWarnings <- application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(req.hetu, useVirta = useVirta, useYtr = useYtr)
      oppija <- oppijaWithWarnings.warningsToLeft
      palautettavatOpiskeluoikeudet = oppija.opiskeluoikeudet.filter(oo => req.opiskeluoikeudenTyypit.contains(oo.tyyppi.koodiarvo))
      _ <- if (palautettavatOpiskeluoikeudet.isEmpty) Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia()) else Right(())
    } yield {
      HetuResponseV1(
        henkilö = buildLuovutuspalveluHenkilöV1(oppija.henkilö),
        opiskeluoikeudet = palautettavatOpiskeluoikeudet
      )
    }
  }

  def queryOppijatByHetu(req: BulkHetuRequestV1)(implicit koskiSession: KoskiSession): Observable[JValue] = {
    val henkilot: List[OppijaHenkilö] = application.opintopolkuHenkilöFacade.findOppijatByHetusNoSlaveOids(req.hetut)
    val oidToHenkilo: Map[String, OppijaHenkilö] = henkilot.map(h => h.oid -> h).toMap
    val user = koskiSession // take current session so it can be used in observable

    auditLogOpiskeluoikeusKatsominen(henkilot)
    val filters = List(OppijaOidHaku(henkilot.map(_.oid)), opiskeluoikeusTyyppiQueryFilters(req.opiskeluoikeudenTyypit))
    streamingQuery(filters).map { t =>
      JsonSerializer.serializeWithUser(user)(buildHetuResponseV1(oidToHenkilo(t._1), t._2))
    }
  }

  private def streamingQuery(filters: List[OpiskeluoikeusQueryFilter])(implicit koskiSession: KoskiSession) =
    OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, None)

  private def auditLogOpiskeluoikeusKatsominen(henkilöt: List[OppijaHenkilö])(implicit koskiSession: KoskiSession): Unit = henkilöt
    .map(h => AuditLogMessage(KoskiOperation.OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(KoskiMessageField.oppijaHenkiloOid -> h.oid)))
    .foreach(AuditLog.log)

  private def buildLuovutuspalveluHenkilöV1(henkilö: Henkilö): LuovutuspalveluHenkilöV1 = {
    henkilö match {
      case th: TäydellisetHenkilötiedot => LuovutuspalveluHenkilöV1(th.oid, th.hetu, th.syntymäaika, th.turvakielto.getOrElse(false))
      case _ => throw new RuntimeException("expected TäydellisetHenkilötiedot")
    }
  }

  private def opiskeluoikeusTyyppiQueryFilters(opiskeluoikeusTyypit: List[String]): OneOfOpiskeluoikeudenTyypit =
    OneOfOpiskeluoikeudenTyypit(opiskeluoikeusTyypit.map(t => OpiskeluoikeusQueryFilter.OpiskeluoikeudenTyyppi(Koodistokoodiviite(t, "opiskeluoikeudentyyppi"))))

  private def buildHetuResponseV1(h: OppijaHenkilö, oo: List[OpiskeluoikeusRow]): HetuResponseV1 =
    HetuResponseV1(
      LuovutuspalveluHenkilöV1(h.oid, h.hetu, h.syntymäaika, h.turvakielto),
      oo.map(_.toOpiskeluoikeus))

  private def resolveVirtaAndYtrUsage(req: LuovutuspalveluRequest) = {
    val useVirta = req.opiskeluoikeudenTyypit.contains(OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
    val useYtr = req.opiskeluoikeudenTyypit.contains(OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
    (useVirta, useYtr)
  }

}
