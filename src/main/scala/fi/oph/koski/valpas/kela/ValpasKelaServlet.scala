package fi.oph.koski.valpas.kela

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.log.ValpasAuditLog.auditLogOppivelvollisuusrekisteriLuovutus
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasKelaSession
import org.json4s.JsonAST.JValue

class ValpasKelaServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasKelaSession {
  private lazy val valpasKelaService = new ValpasKelaService(application)

  post("/hetu") {
    withJsonBody { json =>
      val hetu = ValpasKelaRequest.parse(json)
      val oppija = hetu.flatMap(valpasKelaService.findValpasKelaOppijaByHetu(_))
        .tap(o => auditLogOppivelvollisuusrekisteriLuovutus(o.henkilö.oid))

      renderEither[ValpasKelaOppija](oppija)
    }()
  }
}

object ValpasKelaRequest {
  def parse(json: JValue): Either[HttpStatus, Henkilö.Hetu] = {
    JsonSerializer.validateAndExtract[ValpasKelaRequest](json)
      .left.map(errors => ValpasErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => Hetu.validFormat(req.hetu))
  }

  def parseBulk(json : JValue): Either[HttpStatus, Seq[Henkilö.Hetu]] = {
    val MaxHetus = 1000

    JsonSerializer.validateAndExtract[ValpasKelaBulkRequest](json)
      .left.map(errors => ValpasErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .filterOrElse(_.hetut.length <= MaxHetus, ValpasErrorCategory.badRequest(s"Liian monta hetua, enintään $MaxHetus sallittu"))
      .flatMap(req => HttpStatus.foldEithers(req.hetut.map(Hetu.validFormat)))
  }
}

case class ValpasKelaRequest(hetu: String)
case class ValpasKelaBulkRequest(hetut: List[String])
