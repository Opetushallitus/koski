package fi.oph.koski.supa

import java.time.LocalDateTime
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.suorituspalvelu.{SuorituspalveluQuery, SupaOpiskeluoikeusO}
import fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus.SupaOpiskeluoikeus
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

  get("/:oid/:version") {
    val oid = getStringParam("oid")
    val version = getIntegerParam("version")

    val opiskeluoikeusHistoriasta: Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = application.historyRepository.findVersion(oid, version)(session)
    val oppijaOidOpiskeluoikeudesta = opiskeluoikeusHistoriasta.flatMap(oo => application.opiskeluoikeusRepository.findByOid(oo.oid.get).map(_.oppijaOid))

    val supaVersioResponse = opiskeluoikeusHistoriasta
      .flatMap(oo => oppijaOidOpiskeluoikeudesta.map(oppijaOid => SupaOpiskeluoikeusO(oo, oppijaOid)))
      .flatMap(_.toRight(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta " + oid + " ei löydy tai käyttäjällä ei ole oikeutta sen katseluun")))
      .map(oo => SupaVersioResponse(
        oppijaOid = oo.oppijaOid,
        kaikkiOidit = application.henkilöRepository.findByOid(oo.oppijaOid).map(_.kaikkiOidit).getOrElse(List.empty),
        aikaleima = oo.aikaleima.getOrElse(LocalDateTime.now()),
        opiskeluoikeus = oo
      ))

    supaVersioResponse.foreach { response =>
      SuorituspalveluQuery.auditLog(response.oppijaOid, response.opiskeluoikeus.oid, response.opiskeluoikeus.versionumero)
    }

    renderEither[SupaVersioResponse](supaVersioResponse)
  }
}

case class SupaVersioResponse(
  oppijaOid: String,
  kaikkiOidit: Seq[String],
  aikaleima: LocalDateTime,
  opiskeluoikeus: SupaOpiskeluoikeus
)

object SupaVersioResponse {
  lazy val schemaJson = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[SupaVersioResponse]).asInstanceOf[ClassSchema])
}
