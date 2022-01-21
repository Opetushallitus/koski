package fi.oph.koski.ytl

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{HenkilöOid, Hetu}
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser._
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache, ObservableSupport}
import org.json4s.JsonAST.JValue

class YtlServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresYtl with NoCache with ObservableSupport {
  val ytlService = new YtlService(application)

  post("/oppijat") {
    // TODO: Disabloi tuotannossa toistaiseksi varmuuden vuoksi. Poista, kun valmista.
    if (application.config.getString("opintopolku.virkailija.url") == "https://virkailija.opintopolku.fi") {
      haltWithStatus(KoskiErrorCategory.notImplemented())
    }

    withJsonBody { json =>
      YtlRequest.parseBulk(json) match {
        case Right((oidit, hetut)) =>
          streamResponse[JValue](ytlService.streamOppijat(oidit, hetut, session), session)
        case Left(status) =>
          haltWithStatus(status)
      }
    }()
  }
}

object YtlRequest {
  def parseBulk(json : JValue): Either[HttpStatus, (Seq[Henkilö.Oid], Seq[Henkilö.Hetu])] = {
    val MaxOppijat = 1000

    JsonSerializer.validateAndExtract[YtlBulkRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .filterOrElse(input =>
        input.hetut.getOrElse(List.empty).length + input.oidit.getOrElse(List.empty).length <= MaxOppijat, KoskiErrorCategory.badRequest(s"Liian monta oppijaa, enintään $MaxOppijat sallittu"))
      .flatMap(req => {
        for {
          oidit <- HttpStatus.foldEithers(req.oidit.getOrElse(List.empty).map(HenkilöOid.validateHenkilöOid))
          hetut <- HttpStatus.foldEithers(req.hetut.getOrElse(List.empty).map(Hetu.validFormat))
        } yield (oidit, hetut)
      })
  }
}

case class YtlBulkRequest(
  oidit: Option[List[String]],
  hetut: Option[List[String]]
)
