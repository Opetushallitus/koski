package fi.oph.koski.ytl

import java.sql.Timestamp
import java.time.LocalDateTime

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{KoskiTables, OpiskeluoikeusRow}
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.{JsonSerializer, SensitiveDataAllowed}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryContext, OpiskeluoikeusQueryFilter, QueryOppijaHenkilö}
import fi.oph.koski.schema.{Henkilö, KoskiSchema}
import org.json4s.JsonAST.JValue
import org.json4s.MappingException
import rx.lang.scala.Observable

class YtlService(application: KoskiApplication) extends Logging {

  private lazy val opiskeluoikeudenTyyppiFilter =
    OpiskeluoikeusQueryFilter.OneOfOpiskeluoikeudenTyypit(
      YtlSchema.schemassaTuetutOpiskeluoikeustyypit.map(
        tyyppi => OpiskeluoikeusQueryFilter.OpiskeluoikeudenTyyppi(
          application.koodistoViitePalvelu.validateRequired("opiskeluoikeudentyyppi", tyyppi)
        )
      )
    )

  def streamOppijat(oidit: Seq[String], hetut: Seq[String])(implicit user: KoskiSpecificSession): Observable[JValue] = {
    val henkilöt =
      application.opintopolkuHenkilöFacade.findOppijatNoSlaveOids(oidit) ++
        application.opintopolkuHenkilöFacade.findOppijatByHetusNoSlaveOids(hetut)
    val oidToHenkilo: Map[Henkilö.Oid, OppijaHenkilö] = henkilöt.map(h => h.oid -> h).toMap
    val masterOids = henkilöt.map(_.oid)
    val queryFilters = List(
      opiskeluoikeudenTyyppiFilter,
      OpiskeluoikeusQueryFilter.OppijaOidHaku(masterOids ++ application.henkilöCache.resolveLinkedOids(masterOids))
    )

    OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, queryFilters, None)
      .map((t: (QueryOppijaHenkilö, List[OpiskeluoikeusRow])) => {
        val hlö = oidToHenkilo(t._1.oid)
        YtlOppija(
          henkilö = YtlHenkilö(hlö, hlö.äidinkieli.flatMap(k => application.koodistoViitePalvelu.validate("kieli", k.toUpperCase))),
          opiskeluoikeudet = t._2
            .map(toYtlOpiskeluoikeus).flatMap(_.poistaTiedotJoihinEiKäyttöoikeutta)
        )
      })
      // Poista listalta oppijat, joilla ei ollut yhtään (YTL:ää kiinnostavaa) opiskeluoikeutta
      .filterNot(_.opiskeluoikeudet.isEmpty)
      .doOnEach(auditLogOpiskeluoikeusKatsominen(_)(user))
      .map(JsonSerializer.serializeWithUser(user))
  }

  private def toYtlOpiskeluoikeus(row: OpiskeluoikeusRow)(implicit user: SensitiveDataAllowed): YtlOpiskeluoikeus = {
    deserializeYtlOpiskeluoikeus(row.data, row.oid, row.versionumero, row.aikaleima) match {
      case Right(oo) => oo
      case Left(errors) =>
        throw new MappingException(s"Error deserializing YTL opiskeluoikeus ${row.oid} for oppija ${row.oppijaOid}: ${errors}")
    }
  }

  private def deserializeYtlOpiskeluoikeus(data: JValue, oid: String, versionumero: Int, aikaleima: Timestamp): Either[HttpStatus, YtlOpiskeluoikeus] = {
    val json = KoskiTables.OpiskeluoikeusTable.readAsJValue(data, oid, versionumero, aikaleima)

    application.validatingAndResolvingExtractor.extract[YtlOpiskeluoikeus](
      KoskiSchema.lenientDeserializationWithIgnoringNonValidatingListItems
    )(json)
  }

  private def auditLogOpiskeluoikeusKatsominen(oppija: YtlOppija)(koskiSession: KoskiSpecificSession): Unit =
    AuditLog.log(KoskiAuditLogMessage(KoskiOperation.OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oppija.henkilö.oid)))
}

case class OidVersionTimestamp(oid: String, versionumero: Int, aikaleima: LocalDateTime)
