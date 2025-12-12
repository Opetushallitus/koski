package fi.oph.koski.massaluovutus.luokallejaaneet

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonpatch.JsonPatch
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, DatabaseConverters, QueryMethods}
import fi.oph.koski.history.OpiskeluoikeusHistoryPatch
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, Rooli, Session}
import fi.oph.koski.log.KoskiAuditLogMessageField.oppijaHenkiloOid
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_KATSOMINEN
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.massaluovutus.KoskiMassaluovutusQueryParameters
import fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JNothing, JValue}

import java.sql.Timestamp
import java.time.LocalDateTime

@Title("Perusopetuksen luokalle jäämiset")
@Description("Tämä kysely on tarkoitettu opiskeluoikeusversioiden löytäiseksi KOSKI-varannoksi, joissa perusopetuksen opiskeluoikeuteen on merkitty tieto, että oppilas jää luokalle.")
@Description("Vastauksen skeema on saatavana <a href=\"/koski/json-schema-viewer/?schema=luokalle-jaaneet-result.json\">täältä.</a>")
trait MassaluovutusQueryLuokalleJaaneet extends KoskiMassaluovutusQueryParameters with DatabaseConverters with Logging {
  @EnumValues(Set("luokallejaaneet"))
  def `type`: String
  def format: String
  @Description("Kyselyyn otettavan koulutustoimijan tai oppilaitoksen oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  def organisaatioOid: Option[String]

  def forEachResult(application: KoskiApplication)(f: MassaluovutusQueryLuokalleJaaneetResult => Unit)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    val oppilaitosOids = application.organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid.get)
    val oids = haeOpiskeluoikeusOidit(application.raportointiDatabase.db, oppilaitosOids)

    oids.foreach { case (oppijaOid, opiskeluoikeusOid) =>
      application.historyRepository
        .findByOpiskeluoikeusOid(opiskeluoikeusOid)
        .map { patches =>
          val result = patches.foldLeft(LuokalleJääntiAccumulator()) { (acc, diff) => acc.next(diff) }
          if (result.invalidHistory) {
            f(MassaluovutusQueryLuokalleJaaneetResult(LuokalleJääntiMatch.empty(opiskeluoikeusOid), "err", oppijaOid))
          } else {
            result.matches.foreach {
              case (luokka, oo) =>
                val viimeisinOo = application.opiskeluoikeusRepository.findByOid(opiskeluoikeusOid)
                val result = MassaluovutusQueryLuokalleJaaneetResult(oo, luokka, oppijaOid).copy(
                  viimeisinVersionumero = viimeisinOo.map(_.versionumero).toOption,
                )
                f(result)
            }
          }
          if (result.matches.nonEmpty) {
            auditLog(oppijaOid)
          }
        }
    }

    Right(Unit)
  }

  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean = withKoskiSpecificSession { u =>
    u.hasGlobalReadAccess || (
      organisaatioOid.exists(u.organisationOids(AccessType.read).contains)
        && u.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
    )
  }

  private def haeOpiskeluoikeusOidit(raportointiDb: DB, oppilaitosOids: Seq[String]): Seq[(String, String)] =
    QueryMethods.runDbSync(raportointiDb, sql"""
      SELECT
        r_opiskeluoikeus.oppija_oid,
        r_opiskeluoikeus.opiskeluoikeus_oid
      FROM r_opiskeluoikeus
      JOIN r_paatason_suoritus ON r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
      WHERE koulutusmuoto = 'perusopetus'
        AND oppilaitos_oid = any($oppilaitosOids)
      GROUP BY
        r_opiskeluoikeus.oppija_oid,
        r_opiskeluoikeus.opiskeluoikeus_oid
      ORDER BY
        r_opiskeluoikeus.oppija_oid,
        r_opiskeluoikeus.opiskeluoikeus_oid
    """.as[Tuple2[String, String]])

  private def auditLog(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit = {
    AuditLog.log(KoskiAuditLogMessage(
      OPISKELUOIKEUS_KATSOMINEN,
      user,
      Map(oppijaHenkiloOid -> oppijaOid),
    ))
  }
}

case class LuokalleJääntiAccumulator(
  opiskeluoikeus: JsonNode = JsonNodeFactory.instance.objectNode(),
  invalidHistory: Boolean = false,
  matches: Map[String, LuokalleJääntiMatch] = Map(),
) {
  def next(diff: OpiskeluoikeusHistoryPatch): LuokalleJääntiAccumulator = {
    try {
      val oo = JsonPatch.fromJson(JsonMethods.asJsonNode(diff.muutos)).apply(opiskeluoikeus)
      LuokalleJääntiAccumulator(
        oo,
        invalidHistory,
        newMathes(oo, diff),
      )
    } catch {
      case _: Exception => LuokalleJääntiAccumulator(invalidHistory = true)
    }

  }

  private def newMathes(oo: JsonNode, diff: OpiskeluoikeusHistoryPatch): Map[String, LuokalleJääntiMatch] =
    jääLuokalleLuokilla(oo).foldLeft(matches) { (acc, m) =>
      val (luokka, ooJson) = m
      if (acc.contains(luokka)) {
        acc
      } else {
        acc + (luokka -> LuokalleJääntiMatch(ooJson, diff))
      }
    }

  private def jääLuokalleLuokilla(oo: JsonNode): List[(String, JValue)] =
    suoritukset(oo)
      .arr
      .filter { s => JsonSerializer.extract[Option[Boolean]](s \ "jääLuokalle").getOrElse(false) }
      .map { s => (
        JsonSerializer.extract[String](s \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo"),
        JsonMethods.fromJsonNode(oo),
      ) }

  private def suoritukset(oo: JsonNode): JArray = (JsonMethods.fromJsonNode(oo) \ "suoritukset").asInstanceOf[JArray]
}

case class LuokalleJääntiMatch(
  opiskeluoikeus: JValue,
  oid: String,
  aikaleima: Timestamp,
  versio: Int,
  viimeisinVersionumero: Option[Int],
) {
  def perusopetuksenOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    JsonSerializer
      .extract[PerusopetuksenOpiskeluoikeus](opiskeluoikeus)
      .copy(
        oid = Some(oid),
        versionumero = Some(versio),
        aikaleima = Some(aikaleima.toLocalDateTime),
      )
}

object LuokalleJääntiMatch {
  def apply(opiskeluoikeus: JValue, diff: OpiskeluoikeusHistoryPatch): LuokalleJääntiMatch = LuokalleJääntiMatch(
    opiskeluoikeus = opiskeluoikeus,
    oid = diff.opiskeluoikeusOid,
    aikaleima = diff.aikaleima,
    versio = diff.versionumero,
    viimeisinVersionumero = None,
  )

  def empty(oid: String): LuokalleJääntiMatch = LuokalleJääntiMatch(
    opiskeluoikeus = JNothing,
    oid = oid,
    aikaleima = Timestamp.valueOf(LocalDateTime.MIN),
    versio = 0,
    viimeisinVersionumero = None,
  )
}
