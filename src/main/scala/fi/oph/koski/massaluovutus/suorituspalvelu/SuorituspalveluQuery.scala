package fi.oph.koski.massaluovutus.suorituspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits.getKoskiOpiskeluoikeusRow
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, KoskiOpiskeluoikeusRow, KoskiTables, QueryMethods}
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Session
import fi.oph.koski.koskiuser.Rooli.{OPHKATSELIJA, OPHPAAKAYTTAJA}
import fi.oph.koski.log.AuditLogMessage.AuditLogMessageField
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus.{SupaOpiskeluoikeus, SupaPoistettuOpiskeluoikeus, SupaPoistettuTaiOlemassaolevaOpiskeluoikeus, SupaVirheellinenOpiskeluoikeus}
import fi.oph.koski.massaluovutus.{MassaluovutusQueryPriority, OpetushallituksenMassaluovutusQueryParameters, QueryResultWriter}
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, KoskiSchema}

import java.sql.Timestamp
import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt

trait SuorituspalveluQuery extends OpetushallituksenMassaluovutusQueryParameters with Logging {
  def getOpiskeluoikeusIds(db: DB): Seq[(Int, Timestamp, String)]

  override def priority: Int = MassaluovutusQueryPriority.high

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: Session with SensitiveDataAllowed): Either[String, Unit] = {
    val opiskeluoikeudetResult = getOpiskeluoikeusIds(application.masterDatabase.db)
    val resultsByOppija = opiskeluoikeudetResult.groupBy(_._3)

    writer.predictFileCount(resultsByOppija.size / 100)
    resultsByOppija.grouped(100).zipWithIndex.foreach { case (oppijaResult, index) =>
      val supaResponses = oppijaResult.flatMap { case (oppija_oid, opiskeluoikeudet) =>
        val latestTimestamp = opiskeluoikeudet.maxBy(_._2.toInstant)._2
        val db = selectDbByLag(application, latestTimestamp)
        val response = opiskeluoikeudet.flatMap(oo => getOpiskeluoikeus(application, db, oo._1))

        if (response.nonEmpty) {
          val responseOpiskeluoikeudet = response.collect { case Right(oo) => oo }
          val responseVirheellisetOpiskeluoikeudet = response.collect { case Left(virheellinenOo) => virheellinenOo }

          val auditLogOidit = responseOpiskeluoikeudet.map(_.oid) ++ responseVirheellisetOpiskeluoikeudet.map(_.oid)
          SuorituspalveluQuery.auditLog(oppija_oid, auditLogOidit)

          Some(
            SupaResponse(
              oppijaOid = oppija_oid,
              kaikkiOidit = application.henkilöRepository.findByOid(oppija_oid).get.kaikkiOidit,
              aikaleima = LocalDateTime.from(latestTimestamp.toLocalDateTime),
              opiskeluoikeudet = responseOpiskeluoikeudet,
              virheellisetOpiskeluoikeudet = Some(responseVirheellisetOpiskeluoikeudet).filter(_.nonEmpty)
            )
          )
        } else {
          None
        }
      }
      if (supaResponses.nonEmpty) {
        writer.putJson(s"$index", supaResponses)
      }
    }
    Right(())
  }

  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean = withKoskiSpecificSession { u =>
    u.hasRole(OPHKATSELIJA) || u.hasRole(OPHPAAKAYTTAJA)
  }

  private def getOpiskeluoikeus(application: KoskiApplication, db: DB, id: Int): Option[Either[SupaVirheellinenOpiskeluoikeus, SupaPoistettuTaiOlemassaolevaOpiskeluoikeus]] = {
    val opiskeluoikeusRow = QueryMethods.runDbSync(
      db,
      sql"""
         SELECT *
         FROM opiskeluoikeus
         WHERE id = $id
      """.as[KoskiOpiskeluoikeusRow]
    ).headOption

    if (opiskeluoikeusRow.exists(_.poistettu)) {
      opiskeluoikeusRow.map(oo => SupaPoistettuOpiskeluoikeus(
        oppijaOid = oo.oppijaOid,
        oid = oo.oid,
        versionumero = Some(oo.versionumero),
        aikaleima = Some(oo.aikaleima.toLocalDateTime)
      )).map(Right.apply)
    } else {
      opiskeluoikeusRow
        .flatMap(toSupaOpiskeluoikeus(application))
        .filter(oo => oo.isLeft || oo.exists(_.suoritukset.nonEmpty))
    }
  }

  private def selectDbByLag(application: KoskiApplication, opiskeluoikeusAikaleima: Timestamp): DB = {
    val safetyLimit = 15.seconds
    val replicaLag = application.masterDatabase.replayLag
    val totalLagSeconds = safetyLimit.toSeconds + replicaLag.toSeconds
    if (opiskeluoikeusAikaleima.toLocalDateTime.plusSeconds(totalLagSeconds).isAfter(LocalDateTime.now())) {
      logger.warn(s"Using master database for query due to replica lag of $replicaLag")
      application.masterDatabase.db
    } else {
      application.replicaDatabase.db
    }
  }

  private def toSupaOpiskeluoikeus(application: KoskiApplication)(row: KoskiOpiskeluoikeusRow): Option[Either[SupaVirheellinenOpiskeluoikeus, SupaOpiskeluoikeus]] = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(row.data, row.oid, row.versionumero, row.aikaleima)
    application.validatingAndResolvingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](KoskiSchema.lenientDeserializationWithoutValidation)(json) match {
      case Right(oo: KoskeenTallennettavaOpiskeluoikeus) =>
        SupaOpiskeluoikeusO(oo, row.oppijaOid).map(Right.apply)
      case Left(errors) =>
        logger.warn(s"Error deserializing oppijan ${row.oppijaOid} opiskeluoikeus ${row.oid}: ${errors}")
        Some(Left(
          SupaVirheellinenOpiskeluoikeus(
            oppijaOid = row.oppijaOid,
            oid = row.oid,
            versionumero = Some(row.versionumero),
            aikaleima = Some(row.aikaleima.toLocalDateTime),
            virheet = errors.errors.map(_.toString())
          )
        ))
    }
  }
}

object SuorituspalveluQuery {
  def suoritustenTyypit: List[String] = List(
    "perusopetuksenoppiaineenoppimaara",
    "aikuistenperusopetuksenoppimaara",
    "ammatillinentutkintoosittainen",
    "ammatillinentutkinto",
    "telma",
    "diatutkintovaihe",
    "ebtutkinto",
    "ibtutkinto",
    "internationalschooldiplomavuosiluokka",
    "nuortenperusopetuksenoppiaineenoppimaara",
    "perusopetuksenoppimaara",
    "perusopetuksenvuosiluokka",
    "tuvakoulutuksensuoritus",
    "vstoppivelvollisillesuunnattukoulutus",
    "vstvapaatavoitteinenkoulutus",
    "lukionoppiaineenoppimaara",
    "lukionaineopinnot",
    "lukionoppimaara",
    "perusopetukseenvalmistavaopetus",
  )

  // Yhden oppijan opiskeluoikeus-oidit voi olla niin monta (esim. runsaasti linkitetyt henkilöt), että ne eivät
  // mahtuisi yhteen audit-lokiviestiin ilman että LogConfiguration.logMessageMaxLength (5000 merkkiä) ylittyy ja
  // AuditLogger heittää poikkeuksen (joka kaataisi koko massaluovutuksen). Pilkotaan oidit korkeintaan tämän
  // kokoisiin ryhmiin, jolloin yksi viesti pysyy reilusti rajan alla (100 oidia ~2800 merkkiä).
  private[suorituspalvelu] val auditLogOiditPerViesti = 100

  def auditLog(oppijaOid: String, opiskeluoikeusOidit: Seq[String], versionumero: Option[Int] = None)(implicit user: Session): Unit =
    if (opiskeluoikeusOidit.isEmpty) {
      logAuditMessage(oppijaOid, None, versionumero)
    } else {
      opiskeluoikeusOidit
        .grouped(auditLogOiditPerViesti)
        .foreach(ryhmä => logAuditMessage(oppijaOid, Some(ryhmä.mkString(",")), versionumero))
    }

  private def logAuditMessage(oppijaOid: String, opiskeluoikeusOidit: Option[String], versionumero: Option[Int])(implicit user: Session): Unit = {
    val base: AuditLogMessage.ExtraFields = Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oppijaOid)
    val withOidit = opiskeluoikeusOidit match {
      case Some(oidit) => base + (KoskiAuditLogMessageField.opiskeluoikeusOid -> oidit)
      case None => base
    }
    val fields = withOidit ++ versionumero.map(v =>
      Map(KoskiAuditLogMessageField.opiskeluoikeusVersio -> v.toString)
    ).getOrElse(Map.empty[AuditLogMessageField, String])
    AuditLog.log(
      KoskiAuditLogMessage(
        KoskiOperation.SUORITUSPALVELU_OPISKELUOIKEUS_HAKU,
        user,
        fields
      )
    )
  }
}
