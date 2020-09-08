package fi.oph.koski.kela

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{HenkilöOid, Hetu}
import fi.oph.koski.history.OpiskeluoikeusHistoryPatch
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusOid
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}
import org.json4s.JsonAST.JValue

class KelaServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresKela with NoCache with ObservableSupport {
  val kelaService = new KelaService(application)

  post("/hetu") {
    withJsonBody { json =>
      val hetu = KelaRequest.parse(json)
      val oppija = hetu.flatMap(kelaService.findKelaOppijaByHetu(_)(koskiSession))
      renderEither(oppija)
    }()
  }

  post("/hetut") {
    withJsonBody { json =>
      KelaRequest.parseBulk(json) match {
        case Right(hetut) =>
          streamResponse[JValue](kelaService.streamOppijatByHetu(hetut), koskiSession)
        case Left(status) =>
          haltWithStatus(status)
      }
    }()
  }

  get("/versiohistoria/:opiskeluoikeusOid") {
    renderOption[List[OpiskeluoikeusHistoryPatch]](KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia) {
      kelaService.opiskeluoikeudenHistoria(getStringParam("opiskeluoikeusOid"))
    }
  }

  get("/versiohistoria/:oppijaOid/:opiskeluoikeusOid/:version") {
    val oppija = for {
      oppijaOid <- HenkilöOid.validateHenkilöOid(getStringParam("oppijaOid"))
      opiskeluoikeusOid <- OpiskeluoikeusOid.validateOpiskeluoikeusOid(getStringParam("opiskeluoikeusOid"))
      oppija <- kelaService.findKelaOppijaVersion(oppijaOid, opiskeluoikeusOid, getIntegerParam("version"))
    } yield oppija

    renderEither(oppija)
  }
}

object KelaRequest {
  def parse(json: JValue): Either[HttpStatus, Henkilö.Hetu] = {
    JsonSerializer.validateAndExtract[KelaRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => Hetu.validFormat(req.hetu))
  }

  def parseBulk(json : JValue): Either[HttpStatus, List[Henkilö.Hetu]] = {
    val MaxHetus = 1000

    JsonSerializer.validateAndExtract[KelaBulkRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .filterOrElse(_.hetut.length <= MaxHetus, KoskiErrorCategory.badRequest(s"Liian monta hetua, enintään $MaxHetus sallittu"))
      .flatMap(req => HttpStatus.foldEithers(req.hetut.map(Hetu.validFormat)))
  }
}

case class KelaRequest(hetu: String)
case class KelaBulkRequest(hetut: List[String])
