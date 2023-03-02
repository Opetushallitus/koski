package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiOpiskeluoikeusRow
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OppijaHenkilö, SuppeatOppijaHenkilöTiedot}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField, KoskiOperation}
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryContext, OpiskeluoikeusQueryFilter}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.{OneOfOpiskeluoikeudenTyypit, OppijaOidHaku}
import fi.oph.koski.schema._
import org.json4s.JValue
import rx.lang.scala.Observable

class LuovutuspalveluService(application: KoskiApplication) {
  def findOppijaByOid(req: OidRequestV1)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, LuovutuspalveluResponseV1] = {
    val (useVirta, useYtr) = resolveVirtaAndYtrUsage(req)
    application.oppijaFacade.findOppija(req.oid, findMasterIfSlaveOid = true, useVirta = useVirta, useYtr = useYtr)
      .flatMap(_.warningsToLeft)
      .flatMap(buildResponse(_, req))
  }

  def findOppijaByHetu(req: HetuRequestV1)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, LuovutuspalveluResponseV1] = {
    val (useVirta, useYtr) = resolveVirtaAndYtrUsage(req)
    application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(req.hetu, useVirta = useVirta, useYtr = useYtr)
      .flatMap(_.warningsToLeft)
      .flatMap(buildResponse(_, req))
  }

  def queryOppijatByHetu(req: BulkHetuRequestV1)(implicit koskiSession: KoskiSpecificSession): Observable[JValue] = {
    val henkilot: Seq[OppijaHenkilö] = application.opintopolkuHenkilöFacade.findOppijatByHetusNoSlaveOids(req.hetut)
    val oidToHenkilo: Map[String, OppijaHenkilö] = henkilot.map(h => h.oid -> h).toMap
    val user = koskiSession // take current session so it can be used in observable

    auditLogOpiskeluoikeusKatsominen(henkilot)
    val masterOids = henkilot.map(_.oid)
    val filters = List(OppijaOidHaku(masterOids ++ application.henkilöCache.resolveLinkedOids(masterOids)), opiskeluoikeusTyyppiQueryFilters(req.opiskeluoikeudenTyypit))
    streamingQuery(filters).map { t =>
      JsonSerializer.serializeWithUser(user)(buildResponse(oidToHenkilo(t._1.oid), t._2))
    }
  }

  private def streamingQuery(filters: List[OpiskeluoikeusQueryFilter])(implicit koskiSession: KoskiSpecificSession) =
    OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, None)

  private def auditLogOpiskeluoikeusKatsominen(henkilöt: Seq[OppijaHenkilö])(implicit koskiSession: KoskiSpecificSession): Unit = henkilöt
    .map(h => KoskiAuditLogMessage(KoskiOperation.OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> h.oid)))
    .foreach(AuditLog.log)

  private def opiskeluoikeusTyyppiQueryFilters(opiskeluoikeusTyypit: List[String]): OneOfOpiskeluoikeudenTyypit =
    OneOfOpiskeluoikeudenTyypit(opiskeluoikeusTyypit.map(t => OpiskeluoikeusQueryFilter.OpiskeluoikeudenTyyppi(Koodistokoodiviite(t, "opiskeluoikeudentyyppi"))))

  private def buildResponse(oppija: Oppija, req: LuovutuspalveluRequest)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, LuovutuspalveluResponseV1] = {
    val palautettavatOpiskeluoikeudet = oppija.opiskeluoikeudet.filter(oo => req.opiskeluoikeudenTyypit.contains(oo.tyyppi.koodiarvo))
    if (palautettavatOpiskeluoikeudet.isEmpty) {
      Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
    } else {
      Right(LuovutuspalveluResponseV1(henkilö = toLuovutuspalveluHenkilöV1(oppija.henkilö), opiskeluoikeudet = palautettavatOpiskeluoikeudet))
    }
  }

  private def buildResponse(h: OppijaHenkilö, oo: List[KoskiOpiskeluoikeusRow])(implicit koskiSession: KoskiSpecificSession): LuovutuspalveluResponseV1 =
    LuovutuspalveluResponseV1(
      LuovutuspalveluHenkilöV1(h.oid, h.hetu, h.syntymäaika, h.turvakielto),
      oo.map(_.toOpiskeluoikeusUnsafe))

  private def toLuovutuspalveluHenkilöV1(henkilö: Henkilö): LuovutuspalveluHenkilöV1 = henkilö match {
    case th: TäydellisetHenkilötiedot => LuovutuspalveluHenkilöV1(th.oid, th.hetu, th.syntymäaika, th.turvakielto.getOrElse(false))
    case _ => throw new RuntimeException("expected TäydellisetHenkilötiedot")
  }

  private def resolveVirtaAndYtrUsage(req: LuovutuspalveluRequest) = {
    val useVirta = req.opiskeluoikeudenTyypit.contains(OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
    val useYtr = req.opiskeluoikeudenTyypit.contains(OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
    (useVirta, useYtr)
  }
}
