package fi.oph.koski.supa

import java.time.LocalDateTime
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.history.JsonPatchException
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.koskiuser.Rooli.{OPHKATSELIJA, OPHPAAKAYTTAJA}
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.suorituspalvelu.{SuorituspalveluQuery, SupaOpiskeluoikeusO}
import fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus.SupaOpiskeluoikeus
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusOid
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.util.Timing
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.scalatra.ContentEncodingSupport

import scala.collection.Seq

class SupaServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with Logging
    with RequiresVirkailijaOrPalvelukäyttäjä
    with ContentEncodingSupport
    with NoCache
    with Timing {

  before() {
    koskiSessionOption match {
      case Some(user) if user.hasRole(OPHKATSELIJA) || user.hasRole(OPHPAAKAYTTAJA) =>
      case _ => haltWithStatus(KoskiErrorCategory.forbidden())
    }
  }

  get("/opiskeluoikeus/:oid/:version") {
    val validOid: Either[HttpStatus, Opiskeluoikeus.Oid] =
      OpiskeluoikeusOid.validateOpiskeluoikeusOid(getStringParam("oid"))
    val version: Int = getIntegerParam("version")

    val opiskeluoikeusHistoriasta: Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = try {
      validOid.flatMap(oid => application.historyRepository.findVersion(oid, version)(session))
    } catch {
      case e: JsonPatchException =>
        logger.warn(e)(s"Supa opiskeluoikeuden historiaversion deserialisointi epäonnistui")
        Left(KoskiErrorCategory.internalError("Historiaversion deserialisointi epäonnistui"))
    }

    val oppijaOidOpiskeluoikeudesta: Either[HttpStatus, String] =
      opiskeluoikeusHistoriasta.flatMap(oo => application.opiskeluoikeusRepository.findByOid(oo.oid.get).map(_.oppijaOid))

    val supaVersioResponse: Either[HttpStatus, SupaOpiskeluoikeudenVersioResponse] = opiskeluoikeusHistoriasta
      .flatMap(oo => oppijaOidOpiskeluoikeudesta.map(oppijaOid => SupaOpiskeluoikeusO(oo, oppijaOid)))
      .flatMap(_.toRight(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()))
      .map(oo => SupaOpiskeluoikeudenVersioResponse(
        oppijaOid = oo.oppijaOid,
        kaikkiOidit = application.henkilöRepository.findByOid(oo.oppijaOid).map(_.kaikkiOidit).getOrElse(List.empty),
        aikaleima = oo.aikaleima.getOrElse(LocalDateTime.now()),
        opiskeluoikeus = oo
      ))

    supaVersioResponse.foreach { response =>
      SuorituspalveluQuery.auditLog(response.oppijaOid, response.opiskeluoikeus.oid, response.opiskeluoikeus.versionumero)
    }

    renderEither[SupaOpiskeluoikeudenVersioResponse](supaVersioResponse)
  }
}

case class SupaOpiskeluoikeudenVersioResponse(
  oppijaOid: String,
  kaikkiOidit: Seq[String],
  aikaleima: LocalDateTime,
  opiskeluoikeus: SupaOpiskeluoikeus
)

object SupaOpiskeluoikeudenVersioResponse {
  lazy val schemaJson = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[SupaOpiskeluoikeudenVersioResponse]).asInstanceOf[ClassSchema])
}
