package fi.oph.koski.sure

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.{JsonSerializer, SensitiveDataFilter}
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.OppijaOidHaku
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueries, OpiskeluoikeusQueryContext, OpiskeluoikeusQueryFilter}
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.Timing
import org.json4s.JValue
import org.scalatra.ContentEncodingSupport

class SureServlet(implicit val application: KoskiApplication) extends ApiServlet with Logging with GlobalExecutionContext with OpiskeluoikeusQueries with ContentEncodingSupport with NoCache with Timing {

  get("/muuttuneet") {
    val RequiredParameters = Set("muuttunutEnnen", "muuttunutJälkeen")
    val limitedParams = params.filterKeys(RequiredParameters.contains)
    if (limitedParams.keys != RequiredParameters)
      haltWithStatus(KoskiErrorCategory.badRequest.queryParam.missing())
    OpiskeluoikeusQueryFilter.parse(limitedParams.toList)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession) match {
      case Right(filters) =>
        val serialize = SensitiveDataFilter(koskiSession).rowSerializer
        val observable = OpiskeluoikeusQueryContext(request)(koskiSession, application).queryWithoutHenkilötiedotRaw(
          filters, paginationSettings, params.map { case (p,v) => p + "=" + v }.mkString("&")
        )
        streamResponse[JValue](observable.map(t => serialize(OidHenkilö(t._1), t._2)), koskiSession)
      case Left(status) =>
        haltWithStatus(status)
    }
  }

  post("/oids") {
    withJsonBody { parsedJson =>
      val oids = JsonSerializer.extract[List[String]](parsedJson)
      oids.map(HenkilöOid.validateHenkilöOid).collectFirst { case Left(status) => status } match {
        case None =>
          val serialize = SensitiveDataFilter(koskiSession).rowSerializer
          val observable = OpiskeluoikeusQueryContext(request)(koskiSession, application).queryWithoutHenkilötiedotRaw(
            List(OppijaOidHaku(oids)), None, "oids=" + oids.take(3).mkString(",") + ",..."
          )
          streamResponse[JValue](observable.map(t => serialize(OidHenkilö(t._1), t._2)), koskiSession)
        case Some(status) => haltWithStatus(status)
      }
    }()
  }
}
